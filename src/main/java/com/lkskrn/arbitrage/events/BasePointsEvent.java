package com.lkskrn.arbitrage.events;

import org.springframework.context.ApplicationEvent;

public class BasePointsEvent extends ApplicationEvent {

    public BasePointsEvent(Object source) {
        super(source);
    }
}
