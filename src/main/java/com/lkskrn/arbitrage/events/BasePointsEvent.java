package com.lkskrn.arbitrage.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

public class BasePointsEvent extends ApplicationEvent {
    @Getter
    String message;

    public BasePointsEvent(Object source) {
        super(source);
        this.message = (String) source;
    }
}
