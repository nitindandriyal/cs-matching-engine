package com.cs.darkpool.me.model;

public class OrderEntry {
    private Order order;

    private OrderEntry next = null;

    public OrderEntry(Order o) {
        this.order = o;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public OrderEntry getNext() {
        return next;
    }

    public void setNext(OrderEntry next) {
        this.next = next;
    }
}
