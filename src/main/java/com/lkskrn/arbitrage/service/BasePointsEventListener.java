package com.lkskrn.arbitrage.service;

import com.lkskrn.arbitrage.events.BasePointsEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BasePointsEventListener {

    @EventListener
    public void onApplicationEvent(BasePointsEvent event) {
        log.info(event.getMessage());
    }
}
