package com.lkskrn.arbitrage.cron;

import com.lkskrn.arbitrage.service.ExchangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TradingPairScheduler {

    private final ExchangeService exchangeService;

    @Autowired
    public TradingPairScheduler(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    @Scheduled(fixedDelayString = "PT1H", initialDelay = 0)
    public void loadAvailablePairs() {
        exchangeService.fetchSupportedAssets();
    }

    @Scheduled(fixedDelayString = "PT30S", initialDelay = 0)
    public void scanPrices() {
        exchangeService.compareTradingPairs();
    }
}
