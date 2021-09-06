package com.cs.darkpool.me.trade;

import com.cs.darkpool.me.model.OrderType;
import com.cs.darkpool.me.model.Side;

public class Trade {
    private final TradeStatus status;
    private final CharSequence id;
    private final CharSequence symbol;
    private final OrderType type;
    private final double price;
    private final Side side;
    private final int quantity;
    private final double fillPrice;
    private final int fillQuantity;

    public Trade(TradeStatus status, CharSequence id, CharSequence symbol, OrderType type, double price, Side side, int quantity, double fillPrice, int fillQuantity) {
        this.status = status;
        this.id = id;
        this.symbol = symbol;
        this.type = type;
        this.price = price;
        this.side = side;
        this.quantity = quantity;
        this.fillPrice = fillPrice;
        this.fillQuantity = fillQuantity;
    }

    public TradeStatus getStatus() {
        return status;
    }

    public CharSequence getId() {
        return id;
    }

    public CharSequence getSymbol() {
        return symbol;
    }

    public OrderType getType() {
        return type;
    }

    public double getPrice() {
        return price;
    }

    public Side getSide() {
        return side;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getFillPrice() {
        return fillPrice;
    }

    public int getFillQuantity() {
        return fillQuantity;
    }

    @Override
    public String toString() {
        if (type == OrderType.MARKET) {
            return status + "," + id + "," + symbol + "," + "MKT" + "," + side + "," + quantity + "," + fillPrice + "," + fillQuantity;
        } else {
            return status + "," + id + "," + symbol + "," + price + "," + side + "," + quantity + "," + fillPrice + "," + fillQuantity;
        }
    }
}
