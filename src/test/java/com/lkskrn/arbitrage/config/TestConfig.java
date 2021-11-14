package com.lkskrn.arbitrage.config;

import com.lkskrn.arbitrage.service.APIService;
import com.lkskrn.arbitrage.service.BasePointsEventPublisher;
import com.lkskrn.arbitrage.service.TradingAssetService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    ApplicationEventPublisher publisher() {
        return mock(ApplicationEventPublisher.class);
    }

    @Bean
    @Primary
    BasePointsEventPublisher basePointsEventPublisher() {
        return new BasePointsEventPublisher(publisher());
    }

    @Bean
    @Primary
    TradingAssetService tradingAssetService() {
        return mock(TradingAssetService.class);
    }

    @Bean
    @Primary
    APIService apiService() {
        return mock(APIService.class);
    }

}
