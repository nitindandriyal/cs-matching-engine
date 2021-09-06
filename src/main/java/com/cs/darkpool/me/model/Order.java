package com.cs.darkpool.me.model;

public class Order {
    private CharSequence id;
    private CharSequence symbol;
    private OrderType type;
    private int price;
    private Side side;
    private int quantity;
    private int originalQuantity;

    public Order(CharSequence id, CharSequence symbol, OrderType type, int price, Side side, int quantity) {
        this.id = id;
        this.symbol = symbol;
        this.type = type;
        this.price = price;
        this.side = side;
        this.quantity = quantity;
        this.originalQuantity = quantity;
    }

    public CharSequence getId() {
        return id;
    }

    public void setId(CharSequence id) {
        this.id = id;
    }

    public CharSequence getSymbol() {
        return symbol;
    }

    public void setSymbol(CharSequence symbol) {
        this.symbol = symbol;
    }

    public OrderType getType() {
        return type;
    }

    public void setType(OrderType type) {
        this.type = type;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public Side getSide() {
        return side;
    }

    public void setSide(Side side) {
        this.side = side;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getOriginalQuantity() {
        return originalQuantity;
    }

    public void recycle() {
        this.id = null;
        this.symbol = null;
        this.type = null;
        this.price = -1;
        this.side = null;
        this.quantity = -1;
        this.originalQuantity = -1;
    }
}
