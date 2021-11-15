package com.lkskrn.arbitrage.service;

import com.lkskrn.arbitrage.dto.BasePointsEventData;
import com.lkskrn.arbitrage.dto.Exchange;
import com.lkskrn.arbitrage.events.BasePointsEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BasePointsEventListener {

    @EventListener
    public void onApplicationEvent(BasePointsEvent event) {
        BasePointsEventData data = (BasePointsEventData) event.getSource();
        String message = "%s [%s] | Exchange: [%s] Price: [%s] Bips [%s]| Exchange: [%s] Price: [%s] Bips [%s]"
                .formatted(data.id(), data.basePoints().toBigInteger(), Exchange.BINANCE.name(),
                        data.binancePrice().toPlainString(), data.binanceBips().toPlainString(),
                        Exchange.COINBASE.name(), data.coinbasePrice().toPlainString(),
                        data.coinbaseBips().toPlainString());
        log.info(message);
    }
}
