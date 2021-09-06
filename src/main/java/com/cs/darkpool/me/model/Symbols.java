package com.cs.darkpool.me.model;

import com.cs.darkpool.me.util.PriceScaler;

import java.util.Map;

public class Symbols {
    private final Map<CharSequence, PriceScaler> symbolsMap;

    public Symbols(Map<CharSequence, PriceScaler> symbolsMap) {
        this.symbolsMap = symbolsMap;
    }

    public String validate(String symbolIn) {
        if (symbolsMap.containsKey(symbolIn)) {
            return symbolIn;
        } else {
            throw new IllegalArgumentException("Invalid symbol [" + symbolIn + "]");
        }
    }
}
