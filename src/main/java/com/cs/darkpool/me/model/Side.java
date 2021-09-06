package com.cs.darkpool.me.model;

public enum Side {
    Buy,
    Sell;

    public static Side from(String side) {
        if (side.equals("Buy")) {
            return Buy;
        } else if (side.equals("Sell")) {
            return Sell;
        } else {
            throw new IllegalArgumentException("Side not valid" + side);
        }
    }
}
