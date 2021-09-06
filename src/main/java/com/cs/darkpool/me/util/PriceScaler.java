package com.cs.darkpool.me.util;

public class PriceScaler {

    private final int scale;

    public PriceScaler(int scale) {
        this.scale = scale;
    }

    public int scale(double priceAsDouble) {
        return (int) (priceAsDouble * scale);
    }

    public double unscale(int priceAsInt) {
        return (double) priceAsInt / scale;
    }
}
