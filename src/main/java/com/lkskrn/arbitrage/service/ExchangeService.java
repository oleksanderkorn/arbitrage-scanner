package com.lkskrn.arbitrage.service;

import com.lkskrn.arbitrage.dto.*;
import com.lkskrn.arbitrage.model.TradingAsset;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class ExchangeService {

    private static final String USDT = "USDT";
    private static final String USD = "USD";
    public static final BigDecimal BIPS_MULTIPLIER = new BigDecimal("0.0001");
    public static final BigDecimal BIPS_TRESHOLD = BigDecimal.valueOf(50);

    @Value("${coinbase.endpoints.products}")
    private String coinbaseProductsEndpoint;
    @Value("${coinbase.endpoints.productTicker}")
    private String coinbaseProductTicker;
    @Value("${binance.endpoints.exchangeInfo}")
    private String binanceExchangeInfo;
    @Value("${binance.endpoints.tickerPrice}")
    private String binanceTickerPrice;

    private final TradingAssetService tradingPairService;
    private final RestTemplate restTemplate;

    @Autowired
    public ExchangeService(TradingAssetService tradingPairService, RestTemplate restTemplate) {
        this.tradingPairService = tradingPairService;
        this.restTemplate = restTemplate;
    }

    public void fetchSupportedAssets() {
        getSupportedAssets().forEach(baseAsset -> {
            if (!tradingPairService.assetExists(baseAsset)) {
                log.info("Adding new asset [%s]".formatted(baseAsset));
                tradingPairService.save(new TradingAsset(baseAsset));
            }
        });
    }

    public void compareTradingPairs() {
        tradingPairService.findAll().forEach(asset -> {
            getCoinbaseTicker(asset).ifPresent(coinbaseTicker -> getBinanceTicker(asset).ifPresent(binanceTicker -> {
                BigDecimal binancePrice = binanceTicker.price();
                BigDecimal coinbasePrice = coinbaseTicker.price();
                BigDecimal binanceBips = binancePrice.divide(BIPS_MULTIPLIER, MathContext.DECIMAL128);
                BigDecimal coinbaseBips = coinbasePrice.divide(BIPS_MULTIPLIER, MathContext.DECIMAL128);
                BigDecimal delta = coinbaseBips.subtract(binanceBips).abs();
                if (delta.compareTo(BIPS_TRESHOLD) >= 0) {
                    log.info("%s [%s] | Exchange: [%s] Price: [%s] Bips [%s]| Exchange: [%s] Price: [%s] Bips [%s]".formatted(
                            asset.getName(),
                            delta.toBigInteger(),
                            Exchange.BINANCE.name(),
                            binancePrice.toPlainString(),
                            binanceBips.toPlainString(),
                            Exchange.COINBASE.name(),
                            coinbasePrice.toPlainString(),
                            coinbaseBips.toPlainString())
                    );
                }
                rateLimitDelay();
            }));
        });
    }


    private void rateLimitDelay() {
        try {
            Thread.sleep(300);
        } catch (InterruptedException ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    private List<String> getSupportedAssets() {
        List<Product> coinbasePairs = fetchSupportedCoinbaseProducts();
        return getBinanceExchangeInfo()
                .map(binanceSymbols -> getAssetsIntersection(coinbasePairs, binanceSymbols).map(BinanceSymbol::baseAsset))
                .orElse(Stream.empty()).collect(Collectors.toList());
    }

    private Stream<BinanceSymbol> getAssetsIntersection(List<Product> coinbase, BinanceSymbols binance) {
        return binance.symbols().stream().filter(s -> coinbase.stream().anyMatch(c -> isAssetSupported(s, c)));
    }

    private boolean isAssetSupported(BinanceSymbol s, Product c) {
        return Objects.equals(c.baseCurrency(), s.baseAsset()) &&
                Objects.equals(c.quoteCurrency(), USD) &&
                Objects.equals(s.quoteAsset(), USDT);
    }

    private List<Product> fetchSupportedCoinbaseProducts() {
        return Arrays.stream(getCoinbaseProducts().orElse(new Product[0])).toList().stream().filter(p -> p.quoteCurrency().equals(USD)).collect(Collectors.toList());
    }

    private Optional<Product[]> getCoinbaseProducts() {
        try {
            return Optional.ofNullable(restTemplate.getForObject(coinbaseProductsEndpoint, Product[].class));
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return Optional.empty();
        }
    }

    private Optional<BinanceSymbols> getBinanceExchangeInfo() {
        try {
            return Optional.ofNullable(restTemplate.getForObject(binanceExchangeInfo, BinanceSymbols.class));
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return Optional.empty();
        }
    }

    private Optional<ProductTicker> getCoinbaseTicker(TradingAsset asset) {
        try {
            String endpoint = coinbaseProductTicker.formatted(asset.getName().concat("-").concat(USD));
            return Optional.ofNullable(restTemplate.getForObject(endpoint, ProductTicker.class));
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return Optional.empty();
        }
    }

    private Optional<ProductTicker> getBinanceTicker(TradingAsset asset) {
        try {
            String endpoint = binanceTickerPrice.concat("?symbol=").concat(asset.getName()).concat(USDT);
            return Optional.ofNullable(restTemplate.getForObject(endpoint, ProductTicker.class));
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return Optional.empty();
        }
    }

}
