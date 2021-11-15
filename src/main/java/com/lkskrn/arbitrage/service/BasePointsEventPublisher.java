package com.lkskrn.arbitrage.service;

import com.lkskrn.arbitrage.events.BasePointsEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class BasePointsEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public BasePointsEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void notifyBasePointsDifference(BasePointsEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
