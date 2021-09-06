package com.cs.darkpool.me.util;

import com.cs.darkpool.me.model.Order;
import com.cs.darkpool.me.model.OrderType;
import com.cs.darkpool.me.model.Side;
import com.cs.darkpool.me.model.Symbols;

import java.util.Map;

public class Parser {

    private final Map<CharSequence, PriceScaler> symbolsMap;
    private final Symbols symbols;

    public Parser(Symbols symbols, Map<CharSequence, PriceScaler> symbolsMap) {
        this.symbols = symbols;
        this.symbolsMap = symbolsMap;
    }

    public Order validateAndParseOrder(String[] tokens) {
        String symbol = symbols.validate(tokens[1]);
        Side side = Side.from(tokens[3]);
        int price = tokens[2].equals("MKT") ? -1 : symbolsMap.get(symbol).scale(Double.parseDouble(tokens[2]));
        int quantity = Integer.parseInt(tokens[4]);
        OrderType orderType;
        if (price == -1) {
            orderType = OrderType.MARKET;
        } else {
            orderType = OrderType.LIMIT;
        }
        return new Order(tokens[0], symbol, orderType, price, side, quantity);
    }
}
