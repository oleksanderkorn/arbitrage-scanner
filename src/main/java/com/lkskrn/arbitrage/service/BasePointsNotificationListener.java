package com.lkskrn.arbitrage.service;

import com.lkskrn.arbitrage.events.BasePointsEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BasePointsNotificationListener implements ApplicationListener<BasePointsEvent> {

    @Override
    public void onApplicationEvent(BasePointsEvent event) {
        log.info(event.getMessage());
    }
}
