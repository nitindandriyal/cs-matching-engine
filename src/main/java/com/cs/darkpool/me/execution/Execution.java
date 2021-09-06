package com.cs.darkpool.me.execution;

import com.cs.darkpool.me.model.Order;

public interface Execution {
    void executeMarket(Order order);

    void executeLimit(Order order);
}
