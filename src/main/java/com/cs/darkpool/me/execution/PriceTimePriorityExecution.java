package com.cs.darkpool.me.execution;

import com.cs.darkpool.me.model.Order;
import com.cs.darkpool.me.model.OrderEntry;
import com.cs.darkpool.me.model.Side;
import com.cs.darkpool.me.trade.Trade;
import com.cs.darkpool.me.trade.TradePublisher;
import com.cs.darkpool.me.trade.TradeStatus;
import com.cs.darkpool.me.util.PriceScaler;

import java.util.ArrayDeque;
import java.util.Deque;

public class PriceTimePriorityExecution implements Execution {

    private final TradePublisher tradePublisher;
    private final PriceScaler priceScaler;

    private final Deque<Order> buyMarketOrders = new ArrayDeque<>();
    private final Deque<Order> sellMarketOrders = new ArrayDeque<>();

    private final OrderEntry[] bids = new OrderEntry[100000];
    private final OrderEntry[] asks = new OrderEntry[100000];

    private int bidTopOfBook = -1;
    private int askTopOfBook = -1;

    public PriceTimePriorityExecution(TradePublisher tradePublisher, PriceScaler priceScaler) {
        this.tradePublisher = tradePublisher;
        this.priceScaler = priceScaler;
    }

    @Override
    public void executeMarket(Order order) {
        if (order.getSide() == Side.Buy) {
            if (askTopOfBook == -1) {
                buyMarketOrders.add(order);
            } else {
                int quantityRemaining = sweepAsk(order);
                if (quantityRemaining > 0) {
                    order.setQuantity(quantityRemaining);
                    buyMarketOrders.add(order);
                }
            }
        } else {
            if (bidTopOfBook == -1) {
                sellMarketOrders.add(order);
            } else {
                int quantityRemaining = sweepBid(order);
                if (quantityRemaining > 0) {
                    order.setQuantity(quantityRemaining);
                    sellMarketOrders.add(order);
                }
            }
        }
    }

    @Override
    public void executeLimit(Order limitOrder) {
        matchMarketOrdersFirst(limitOrder);
        int quantityRemaining = limitOrder.getQuantity();
        int origLimitOrderQty = quantityRemaining;
        if (limitOrder.getSide() == Side.Buy) {
            while (quantityRemaining > 0) {
                if (sellMarketOrders.size() == 0) {
                    if ((sweepAsk(limitOrder)) > 0) {
                        addRemainingToOrderBook(limitOrder, bids[limitOrder.getPrice()], bids);
                        if (bidTopOfBook == -1 || limitOrder.getPrice() > bids[bidTopOfBook].getOrder().getPrice()) {
                            bidTopOfBook = limitOrder.getPrice();
                        }
                    }
                    break;
                }
                Order marketOrder = sellMarketOrders.getFirst();
                if (marketOrder.getQuantity() <= limitOrder.getQuantity()) {
                    quantityRemaining = quantityRemaining - marketOrder.getQuantity();
                    limitOrder.setQuantity(quantityRemaining);
                    sellMarketOrders.removeFirst();
                    tradePublisher.publish(new Trade(TradeStatus.Fill,
                            marketOrder.getId(), marketOrder.getSymbol(),
                            marketOrder.getType(),
                            priceScaler.unscale(limitOrder.getPrice()),
                            marketOrder.getSide(),
                            marketOrder.getOriginalQuantity(),
                            priceScaler.unscale(limitOrder.getPrice()),
                            marketOrder.getQuantity()));
                    tradePublisher.publish(new Trade(TradeStatus.Fill,
                            limitOrder.getId(),
                            limitOrder.getSymbol(),
                            limitOrder.getType(),
                            priceScaler.unscale(limitOrder.getPrice()),
                            limitOrder.getSide(),
                            origLimitOrderQty,
                            priceScaler.unscale(limitOrder.getPrice()),
                            marketOrder.getQuantity()));
                } else {
                    int origMarketOrderQty = marketOrder.getQuantity();
                    marketOrder.setQuantity(marketOrder.getQuantity() - quantityRemaining);
                    limitOrder.setQuantity(0);
                    tradePublisher.publish(new Trade(TradeStatus.Fill,
                            marketOrder.getId(),
                            marketOrder.getSymbol(),
                            marketOrder.getType(),
                            priceScaler.unscale(limitOrder.getPrice()),
                            marketOrder.getSide(),
                            origMarketOrderQty,
                            priceScaler.unscale(limitOrder.getPrice()),
                            quantityRemaining));
                    tradePublisher.publish(new Trade(TradeStatus.Fill,
                            limitOrder.getId(),
                            limitOrder.getSymbol(),
                            limitOrder.getType(),
                            priceScaler.unscale(limitOrder.getPrice()),
                            limitOrder.getSide(),
                            origLimitOrderQty,
                            priceScaler.unscale(limitOrder.getPrice()),
                            quantityRemaining));
                    quantityRemaining = 0;
                }
            }
        } else {
            while (quantityRemaining > 0) {
                if (buyMarketOrders.size() == 0) {
                    if ((sweepBid(limitOrder)) > 0) {
                        addRemainingToOrderBook(limitOrder, asks[limitOrder.getPrice()], asks);
                        if (askTopOfBook == -1 || limitOrder.getPrice() < asks[askTopOfBook].getOrder().getPrice()) {
                            askTopOfBook = limitOrder.getPrice();
                        }
                    }
                    break;
                }
                Order marketOrder = buyMarketOrders.getFirst();
                if (marketOrder.getQuantity() <= limitOrder.getQuantity()) {
                    quantityRemaining = quantityRemaining - marketOrder.getQuantity();
                    limitOrder.setQuantity(quantityRemaining);
                    buyMarketOrders.removeFirst();
                    tradePublisher.publish(new Trade(TradeStatus.Fill,
                            marketOrder.getId(),
                            marketOrder.getSymbol(),
                            marketOrder.getType(),
                            priceScaler.unscale(limitOrder.getPrice()),
                            marketOrder.getSide(),
                            marketOrder.getOriginalQuantity(),
                            priceScaler.unscale(limitOrder.getPrice()),
                            marketOrder.getQuantity()));
                    tradePublisher.publish(new Trade(TradeStatus.Fill,
                            limitOrder.getId(),
                            limitOrder.getSymbol(),
                            limitOrder.getType(),
                            priceScaler.unscale(limitOrder.getPrice()),
                            limitOrder.getSide(),
                            limitOrder.getOriginalQuantity(),
                            priceScaler.unscale(limitOrder.getPrice()),
                            marketOrder.getQuantity()));
                } else {
                    marketOrder.setQuantity(marketOrder.getQuantity() - quantityRemaining);
                    limitOrder.setQuantity(0);
                    tradePublisher.publish(new Trade(TradeStatus.Fill,
                            marketOrder.getId(),
                            marketOrder.getSymbol(),
                            marketOrder.getType(),
                            priceScaler.unscale(limitOrder.getPrice()),
                            marketOrder.getSide(),
                            marketOrder.getOriginalQuantity(),
                            priceScaler.unscale(limitOrder.getPrice()),
                            quantityRemaining));
                    tradePublisher.publish(new Trade(TradeStatus.Fill,
                            limitOrder.getId(), limitOrder.getSymbol(),
                            limitOrder.getType(),
                            priceScaler.unscale(limitOrder.getPrice()),
                            limitOrder.getSide(),
                            limitOrder.getOriginalQuantity(),
                            priceScaler.unscale(limitOrder.getPrice()),
                            quantityRemaining));
                    quantityRemaining = 0;
                }
            }
        }
    }

    private void matchMarketOrdersFirst(Order limitOrder) {
        while (buyMarketOrders.size() > 0 && sellMarketOrders.size() > 0) {
            Order buyMarketOrder = buyMarketOrders.getFirst();
            Order sellMarketOrder = sellMarketOrders.getFirst();
            int filledQty;
            if (buyMarketOrder.getQuantity() > sellMarketOrder.getQuantity()) {
                filledQty = sellMarketOrder.getQuantity();
                buyMarketOrder.setQuantity(buyMarketOrder.getQuantity() - filledQty);
                sellMarketOrders.removeFirst();
            } else if (buyMarketOrder.getQuantity() == sellMarketOrder.getQuantity()) {
                filledQty = sellMarketOrder.getQuantity();
                sellMarketOrders.removeFirst();
                buyMarketOrders.removeFirst();
            } else {
                filledQty = buyMarketOrder.getQuantity();
                sellMarketOrder.setQuantity(sellMarketOrder.getQuantity() - filledQty);
                buyMarketOrders.removeFirst();
            }
            tradePublisher.publish(new Trade(TradeStatus.Fill,
                    sellMarketOrder.getId(),
                    sellMarketOrder.getSymbol(),
                    sellMarketOrder.getType(),
                    priceScaler.unscale(limitOrder.getPrice()),
                    sellMarketOrder.getSide(),
                    sellMarketOrder.getOriginalQuantity(),
                    priceScaler.unscale(limitOrder.getPrice()),
                    filledQty));
            tradePublisher.publish(new Trade(TradeStatus.Fill,
                    buyMarketOrder.getId(),
                    buyMarketOrder.getSymbol(),
                    buyMarketOrder.getType(),
                    priceScaler.unscale(limitOrder.getPrice()),
                    buyMarketOrder.getSide(),
                    buyMarketOrder.getOriginalQuantity(),
                    priceScaler.unscale(limitOrder.getPrice()),
                    filledQty));
        }
    }

    private int sweepAsk(Order order) {
        int quantityRemaining = order.getQuantity();
        if (askTopOfBook == -1) {
            return quantityRemaining;
        }

        int matchIndex = askTopOfBook;
        while (null == asks[matchIndex] || asks[matchIndex].getOrder().getPrice() > order.getPrice()) {
            matchIndex++;
            if (matchIndex == asks.length) {
                return quantityRemaining;
            }
        }

        while (matchIndex < asks.length && quantityRemaining > 0) {
            OrderEntry matchedOrderNode = asks[matchIndex];
            if (matchedOrderNode != null) {
                if (asks[matchIndex].getOrder().getPrice() > order.getPrice()) {
                    break;
                }
                int fillQuantity;
                if (quantityRemaining > matchedOrderNode.getOrder().getQuantity()) {
                    quantityRemaining = quantityRemaining - matchedOrderNode.getOrder().getQuantity();
                    fillQuantity = matchedOrderNode.getOrder().getQuantity();
                } else {
                    fillQuantity = quantityRemaining;
                    quantityRemaining = 0;
                }

                int matchedOrderQty = matchedOrderNode.getOrder().getQuantity();
                matchedOrderNode.getOrder().setQuantity(matchedOrderQty - fillQuantity);
                removeAsksNode(matchIndex, matchedOrderNode, fillQuantity);
                Order matched = matchedOrderNode.getOrder();
                tradePublisher.publish(new Trade(TradeStatus.Fill,
                        matched.getId(),
                        matched.getSymbol(),
                        matched.getType(), priceScaler.unscale(matched.getPrice()),
                        matched.getSide(),
                        matched.getOriginalQuantity(),
                        priceScaler.unscale(matched.getPrice()),
                        fillQuantity));
                tradePublisher.publish(new Trade(TradeStatus.Fill,
                        order.getId(),
                        order.getSymbol(),
                        order.getType(),
                        priceScaler.unscale(order.getPrice()),
                        order.getSide(),
                        order.getOriginalQuantity(),
                        priceScaler.unscale(order.getPrice()),
                        fillQuantity));

                while (quantityRemaining > 0 && matchedOrderNode.getNext() != null) {
                    matchedOrderNode = matchedOrderNode.getNext();
                    quantityRemaining = quantityRemaining - matchedOrderNode.getOrder().getQuantity();
                    fillQuantity = quantityRemaining > 0 ? matchedOrderNode.getOrder().getQuantity() : order.getQuantity();
                    matchedOrderQty = matchedOrderNode.getOrder().getQuantity();
                    matchedOrderNode.getOrder().setQuantity(matchedOrderQty - fillQuantity);
                    removeAsksNode(matchIndex, matchedOrderNode, fillQuantity);
                    matched = matchedOrderNode.getOrder();
                    tradePublisher.publish(new Trade(TradeStatus.Fill,
                            matched.getId(),
                            matched.getSymbol(),
                            matched.getType(),
                            priceScaler.unscale(matched.getPrice()),
                            matched.getSide(),
                            matched.getOriginalQuantity(),
                            priceScaler.unscale(matched.getPrice()),
                            fillQuantity));
                    tradePublisher.publish(new Trade(TradeStatus.Fill,
                            order.getId(),
                            order.getSymbol(),
                            order.getType(),
                            priceScaler.unscale(order.getPrice()),
                            order.getSide(),
                            order.getOriginalQuantity(),
                            priceScaler.unscale(order.getPrice()),
                            fillQuantity));
                }
            }
            ++matchIndex;
        }

        return quantityRemaining;
    }

    private void removeAsksNode(int matchIndex, OrderEntry matchedOrderNode, int fillQuantity) {
        if (fillQuantity >= matchedOrderNode.getOrder().getQuantity()) {
            asks[matchIndex] = matchedOrderNode.getNext();
            while (askTopOfBook < asks.length && asks[askTopOfBook] == null) {
                ++askTopOfBook;
            }
        }
    }

    private int sweepBid(Order order) {
        int quantityRemaining = order.getQuantity();
        if (bidTopOfBook == -1) {
            return quantityRemaining;
        }
        int matchIndex = bidTopOfBook;
        while (null == bids[matchIndex] || bids[matchIndex].getOrder().getPrice() < order.getPrice()) {
            matchIndex--;
            if (matchIndex == 0) {
                return quantityRemaining;
            }
        }

        while (matchIndex > 0 && quantityRemaining > 0) {
            OrderEntry matchedOrderNode = bids[matchIndex];
            if (matchedOrderNode != null) {
                if (bids[matchIndex].getOrder().getPrice() < order.getPrice()) {
                    break;
                }
                int fillQuantity;
                if (quantityRemaining > matchedOrderNode.getOrder().getQuantity()) {
                    quantityRemaining = quantityRemaining - matchedOrderNode.getOrder().getQuantity();
                    fillQuantity = matchedOrderNode.getOrder().getQuantity();
                } else {
                    fillQuantity = quantityRemaining;
                    quantityRemaining = 0;
                }
                int matchedOrderQty = matchedOrderNode.getOrder().getQuantity();
                matchedOrderNode.getOrder().setQuantity(matchedOrderQty - fillQuantity);
                removeBidsNode(matchIndex, matchedOrderNode);
                Order matched = matchedOrderNode.getOrder();
                tradePublisher.publish(new Trade(TradeStatus.Fill,
                        matched.getId(),
                        matched.getSymbol(),
                        matched.getType(),
                        priceScaler.unscale(matched.getPrice()),
                        matched.getSide(),
                        matched.getOriginalQuantity(),
                        priceScaler.unscale(matched.getPrice()),
                        fillQuantity));
                tradePublisher.publish(new Trade(TradeStatus.Fill,
                        order.getId(),
                        order.getSymbol(),
                        order.getType(),
                        priceScaler.unscale(order.getPrice()),
                        order.getSide(),
                        order.getOriginalQuantity(),
                        priceScaler.unscale(order.getPrice()),
                        fillQuantity));

                while (quantityRemaining > 0 && matchedOrderNode.getNext() != null) {
                    matchedOrderNode = matchedOrderNode.getNext();
                    quantityRemaining = quantityRemaining - matchedOrderNode.getOrder().getQuantity();
                    fillQuantity = quantityRemaining > 0 ? matchedOrderNode.getOrder().getQuantity() : order.getQuantity();
                    matchedOrderQty = matchedOrderNode.getOrder().getQuantity();
                    matchedOrderNode.getOrder().setQuantity(matchedOrderQty - fillQuantity);
                    removeBidsNode(matchIndex, matchedOrderNode);
                    matched = matchedOrderNode.getOrder();
                    tradePublisher.publish(new Trade(TradeStatus.Fill,
                            matched.getId(),
                            matched.getSymbol(),
                            matched.getType(),
                            priceScaler.unscale(matched.getPrice()),
                            matched.getSide(),
                            matched.getOriginalQuantity(),
                            priceScaler.unscale(matched.getPrice()),
                            fillQuantity));
                    tradePublisher.publish(new Trade(TradeStatus.Fill,
                            order.getId(),
                            order.getSymbol(),
                            order.getType(),
                            priceScaler.unscale(order.getPrice()),
                            order.getSide(),
                            order.getOriginalQuantity(),
                            priceScaler.unscale(order.getPrice()),
                            fillQuantity));
                }
            }
            --matchIndex;
        }

        return quantityRemaining;
    }

    private void removeBidsNode(int matchIndex, OrderEntry matchedOrderNode) {
        if (matchedOrderNode.getOrder().getQuantity() == 0) {
            bids[matchIndex] = matchedOrderNode.getNext();
            while (bidTopOfBook >= 0 && bids[bidTopOfBook] == null) {
                --bidTopOfBook;
            }
        }
    }

    private void addRemainingToOrderBook(Order order, OrderEntry node, OrderEntry[] orderNodes) {
        if (node == null) {
            orderNodes[order.getPrice()] = new OrderEntry(order);
        } else {
            while (node.getNext() != null) {
                node = node.getNext();
            }
            if (node.getOrder().getId() != order.getId()) {
                node.setNext(new OrderEntry(order));
            }
        }
    }
}
