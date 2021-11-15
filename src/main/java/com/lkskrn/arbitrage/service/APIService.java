package com.lkskrn.arbitrage.service;

import com.lkskrn.arbitrage.dto.BinanceSymbols;
import com.lkskrn.arbitrage.dto.CoinbaseProduct;
import com.lkskrn.arbitrage.dto.ProductTicker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static com.lkskrn.arbitrage.util.Constants.USD;
import static com.lkskrn.arbitrage.util.Constants.USDT;

@Service
@Slf4j
public class APIService {

    @Value("${coinbase.endpoints.products}")
    private String coinbaseProductsEndpoint;
    @Value("${coinbase.endpoints.productTicker}")
    private String coinbaseProductTicker;
    @Value("${binance.endpoints.exchangeInfo}")
    private String binanceExchangeInfo;
    @Value("${binance.endpoints.tickerPrice}")
    private String binanceTickerPrice;

    private final RestTemplate restTemplate;

    @Autowired
    public APIService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Optional<CoinbaseProduct[]> getCoinbaseProducts() {
        try {
            return Optional.ofNullable(restTemplate.getForObject(coinbaseProductsEndpoint, CoinbaseProduct[].class));
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return Optional.empty();
        }
    }

    public Optional<BinanceSymbols> getBinanceExchangeInfo() {
        try {
            return Optional.ofNullable(restTemplate.getForObject(binanceExchangeInfo, BinanceSymbols.class));
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return Optional.empty();
        }
    }

    public Optional<ProductTicker> getCoinbaseTicker(String asset) {
        try {
            String endpoint = coinbaseProductTicker.formatted(asset.concat("-").concat(USD));
            return Optional.ofNullable(restTemplate.getForObject(endpoint, ProductTicker.class));
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return Optional.empty();
        }
    }

    public Optional<ProductTicker> getBinanceTicker(String asset) {
        try {
            String endpoint = binanceTickerPrice.concat("?symbol=").concat(asset).concat(USDT);
            return Optional.ofNullable(restTemplate.getForObject(endpoint, ProductTicker.class));
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return Optional.empty();
        }
    }
}
