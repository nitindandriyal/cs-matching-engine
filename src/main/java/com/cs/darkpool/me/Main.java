package com.cs.darkpool.me;

import com.cs.darkpool.me.execution.SmartOrderRouter;
import com.cs.darkpool.me.model.Order;
import com.cs.darkpool.me.model.Symbols;
import com.cs.darkpool.me.trade.TradePublisher;
import com.cs.darkpool.me.util.Parser;
import com.cs.darkpool.me.util.PriceScaler;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] a) {
        Scanner reader = new Scanner(System.in);
        Map<CharSequence, PriceScaler> symbolsMap = initSymbolsMap();
        Symbols symbols = new Symbols(initSymbolsMap());
        Parser parser = new Parser(symbols, symbolsMap);
        SmartOrderRouter router = new SmartOrderRouter(symbolsMap, new TradePublisher());
        while (reader.hasNextLine()) {
            String[] tokens = reader.nextLine().split(",");
            Order order = parser.validateAndParseOrder(tokens);
            router.add(order);
        }
        router.execute();
    }

    private static Map<CharSequence, PriceScaler> initSymbolsMap() {
        Map<CharSequence, PriceScaler> symbolsMap = new HashMap<>();
        symbolsMap.put("0700.HK", new PriceScaler(10));
        return symbolsMap;
    }
}
