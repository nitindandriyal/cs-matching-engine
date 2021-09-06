package com.cs.darkpool.me.execution;

import com.cs.darkpool.me.model.Order;
import com.cs.darkpool.me.model.OrderType;
import com.cs.darkpool.me.model.Side;
import com.cs.darkpool.me.trade.Trade;
import com.cs.darkpool.me.trade.TradePublisher;
import com.cs.darkpool.me.trade.TradeStatus;
import com.cs.darkpool.me.util.PriceScaler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SmartOrderRouterTest {

    private SmartOrderRouter smartOrderRouter;

    private final Map<CharSequence, PriceScaler> symbolsMap = new HashMap<>();

    @Spy
    private final TradePublisher tradePublisher = new TradePublisher();

    @Captor
    private ArgumentCaptor<Trade> tradeArgumentCaptor;


    @Before
    public void setup() {
        symbolsMap.put("0700.HK", new PriceScaler(10));
        symbolsMap.put("0005.HK", new PriceScaler(10));
        smartOrderRouter = new SmartOrderRouter(symbolsMap, tradePublisher);
    }

    @Test
    public void sampleA() {
        Order order1 = new Order("Order1", "0700.HK", OrderType.LIMIT, symbolsMap.get("0700.HK").scale(610), Side.Sell, 20000);
        Order order2 = new Order("Order2", "0700.HK", OrderType.LIMIT, symbolsMap.get("0700.HK").scale(610), Side.Sell, 10000);
        Order order3 = new Order("Order3", "0700.HK", OrderType.LIMIT, symbolsMap.get("0700.HK").scale(610), Side.Buy, 10000);
        smartOrderRouter.add(order1);
        smartOrderRouter.add(order2);
        smartOrderRouter.add(order3);
        smartOrderRouter.execute();
        verify(tradePublisher, times(2)).publish(tradeArgumentCaptor.capture());
        List<Trade> trade = tradeArgumentCaptor.getAllValues();
        assertEquals(TradeStatus.Fill, trade.get(0).getStatus());
        assertEquals("Order1", trade.get(0).getId());
        assertEquals("0700.HK", trade.get(0).getSymbol());
        assertEquals(610.0, trade.get(0).getPrice(), 0.0001);
        assertEquals(Side.Sell, trade.get(0).getSide());
        assertEquals(20_000, trade.get(0).getQuantity());
        assertEquals(610.0, trade.get(0).getFillPrice(), 0.0001);
        assertEquals(10_000, trade.get(0).getFillQuantity());

        assertEquals(TradeStatus.Fill, trade.get(1).getStatus());
        assertEquals("Order3", trade.get(1).getId());
        assertEquals("0700.HK", trade.get(1).getSymbol());
        assertEquals(610.0, trade.get(1).getPrice(), 0.0001);
        assertEquals(Side.Buy, trade.get(1).getSide());
        assertEquals(10_000, trade.get(1).getQuantity());
        assertEquals(610.0, trade.get(1).getFillPrice(), 0.0001);
        assertEquals(10_000, trade.get(1).getFillQuantity());
    }

    @Test
    public void sampleB() {
        Order order1 = new Order("Order1", "0700.HK", OrderType.LIMIT, symbolsMap.get("0700.HK").scale(610), Side.Sell, 20000);
        Order order2 = new Order("Order2", "0700.HK", OrderType.MARKET, -1, Side.Sell, 10000);
        Order order3 = new Order("Order3", "0700.HK", OrderType.LIMIT, symbolsMap.get("0700.HK").scale(610), Side.Buy, 10000);
        smartOrderRouter.add(order1);
        smartOrderRouter.add(order2);
        smartOrderRouter.add(order3);
        smartOrderRouter.execute();
        verify(tradePublisher, times(2)).publish(tradeArgumentCaptor.capture());
        List<Trade> trade = tradeArgumentCaptor.getAllValues();
        assertEquals(TradeStatus.Fill, trade.get(0).getStatus());
        assertEquals("Order2", trade.get(0).getId());
        assertEquals("0700.HK", trade.get(0).getSymbol());
        assertEquals(610.0, trade.get(0).getPrice(), 0.0001);
        assertEquals(Side.Sell, trade.get(0).getSide());
        assertEquals(10_000, trade.get(0).getQuantity());
        assertEquals(610.0, trade.get(0).getFillPrice(), 0.0001);
        assertEquals(10_000, trade.get(0).getFillQuantity());

        assertEquals(TradeStatus.Fill, trade.get(1).getStatus());
        assertEquals("Order3", trade.get(1).getId());
        assertEquals("0700.HK", trade.get(1).getSymbol());
        assertEquals(610.0, trade.get(1).getPrice(), 0.0001);
        assertEquals(Side.Buy, trade.get(1).getSide());
        assertEquals(10_000, trade.get(1).getQuantity());
        assertEquals(610.0, trade.get(1).getFillPrice(), 0.0001);
        assertEquals(10_000, trade.get(1).getFillQuantity());
    }

    @Test
    public void sampleC() {
        Order order1 = new Order("Order1", "0700.HK", OrderType.LIMIT, symbolsMap.get("0700.HK").scale(610), Side.Sell, 10000);
        Order order2 = new Order("Order2", "0700.HK", OrderType.LIMIT, symbolsMap.get("0700.HK").scale(610), Side.Buy, 10_000_000);

        smartOrderRouter.add(order1);
        smartOrderRouter.add(order2);

        smartOrderRouter.execute();

        verify(tradePublisher, times(0)).publish(tradeArgumentCaptor.capture());
    }

    @Test
    public void sampleD() {
        Order order1 = new Order("Order1", "0700.HK", OrderType.LIMIT, symbolsMap.get("0700.HK").scale(610), Side.Sell, 10000);
        Order order2 = new Order("Order2", "0005.HK", OrderType.LIMIT, symbolsMap.get("0700.HK").scale(49.8), Side.Sell, 10000);
        Order order3 = new Order("Order3", "0005.HK", OrderType.LIMIT, symbolsMap.get("0700.HK").scale(49.8), Side.Buy, 10000);
        smartOrderRouter.add(order1);
        smartOrderRouter.add(order2);
        smartOrderRouter.add(order3);
        smartOrderRouter.execute();

        verify(tradePublisher, times(2)).publish(tradeArgumentCaptor.capture());

        List<Trade> trade = tradeArgumentCaptor.getAllValues();
        assertEquals(TradeStatus.Fill, trade.get(0).getStatus());
        assertEquals("Order2", trade.get(0).getId());
        assertEquals("0005.HK", trade.get(0).getSymbol());
        assertEquals(49.8, trade.get(0).getPrice(), 0.0001);
        assertEquals(Side.Sell, trade.get(0).getSide());
        assertEquals(10_000, trade.get(0).getQuantity());
        assertEquals(49.8, trade.get(0).getFillPrice(), 0.0001);
        assertEquals(10_000, trade.get(0).getFillQuantity());

        assertEquals(TradeStatus.Fill, trade.get(1).getStatus());
        assertEquals("Order3", trade.get(1).getId());
        assertEquals("0005.HK", trade.get(1).getSymbol());
        assertEquals(49.8, trade.get(1).getPrice(), 0.0001);
        assertEquals(Side.Buy, trade.get(1).getSide());
        assertEquals(10_000, trade.get(1).getQuantity());
        assertEquals(49.8, trade.get(1).getFillPrice(), 0.0001);
        assertEquals(10_000, trade.get(1).getFillQuantity());
    }

    @Test
    public void sampleE() {
        Order order1 = new Order("Order1", "0700.HK", OrderType.MARKET, -1, Side.Sell, 20000);
        Order order2 = new Order("Order2", "0700.HK", OrderType.MARKET, -1, Side.Buy, 10000);
        Order order3 = new Order("Order3", "0700.HK", OrderType.LIMIT, symbolsMap.get("0700.HK").scale(610.0), Side.Buy, 10000);
        smartOrderRouter.add(order1);
        smartOrderRouter.add(order2);
        smartOrderRouter.add(order3);
        smartOrderRouter.execute();
    }

    @Test
    public void sampleF() {
        Order order1 = new Order("Order1", "0700.HK", OrderType.MARKET, -1, Side.Sell, 10000);
        Order order2 = new Order("Order2", "0700.HK", OrderType.MARKET, -1, Side.Sell, 10000);
        Order order3 = new Order("Order3", "0700.HK", OrderType.LIMIT, symbolsMap.get("0700.HK").scale(49.8), Side.Buy, 20000);

        smartOrderRouter.add(order1);
        smartOrderRouter.add(order2);
        smartOrderRouter.add(order3);
        smartOrderRouter.execute();
    }
}