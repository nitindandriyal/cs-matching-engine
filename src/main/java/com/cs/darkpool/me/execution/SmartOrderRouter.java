package com.cs.darkpool.me.execution;

import com.cs.darkpool.me.model.Order;
import com.cs.darkpool.me.model.OrderType;
import com.cs.darkpool.me.trade.TradePublisher;
import com.cs.darkpool.me.util.PriceScaler;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class SmartOrderRouter {
    private static final int MAX_TICKET_SIZE = 10_000_000;

    private final Map<CharSequence, PriceTimePriorityExecution> executionMap = new HashMap<>();
    private final Deque<Order> incoming = new ArrayDeque<>();
    private final Map<CharSequence, PriceScaler> symbols;

    public SmartOrderRouter(Map<CharSequence, PriceScaler> symbols, TradePublisher tradePublisher) {
        for (CharSequence symbol : symbols.keySet()) {
            executionMap.put(symbol, new PriceTimePriorityExecution(tradePublisher, symbols.get(symbol)));
        }
        this.symbols = symbols;
    }

    public void add(Order order) {
        StringBuilder sb;
        if (validate(order)) {
            incoming.add(order);
            sb = new StringBuilder()
                    .append("Ack");
        } else {
            sb = new StringBuilder()
                    .append("Reject");
        }
        sb.append(",").append(order.getId())
                .append(",").append(order.getSymbol())
                .append(",").append(order.getType() == OrderType.LIMIT ? symbols.get(order.getSymbol()).unscale(order.getPrice()) : "MKT")
                .append(",").append(order.getSide())
                .append(",").append(order.getQuantity());
        System.out.println(sb);
    }

    private boolean validate(Order order) {
        return order.getQuantity() < MAX_TICKET_SIZE;
    }

    public void execute() {
        Order order;
        while (incoming.size() > 0) {
            order = incoming.removeFirst();
            if (order.getType() == OrderType.MARKET) {
                executionMap.get(order.getSymbol()).executeMarket(order);
            } else if (order.getType() == OrderType.LIMIT) {
                executionMap.get(order.getSymbol()).executeLimit(order);
            }
        }
    }
}
