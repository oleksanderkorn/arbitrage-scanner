package com.lkskrn.arbitrage.service;

import com.lkskrn.arbitrage.dto.BasePointsEventData;
import com.lkskrn.arbitrage.dto.BinanceSymbol;
import com.lkskrn.arbitrage.dto.CoinbaseProduct;
import com.lkskrn.arbitrage.events.BasePointsEvent;
import com.lkskrn.arbitrage.model.TradingAsset;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.lkskrn.arbitrage.util.Constants.*;

@Service
@Slf4j
public class ExchangeService {

    private final TradingAssetService tradingPairService;
    private final APIService apiService;
    private final BasePointsEventPublisher notificationService;

    @Autowired
    public ExchangeService(TradingAssetService tradingPairService, APIService apiService,
                           BasePointsEventPublisher notificationService) {
        this.tradingPairService = tradingPairService;
        this.apiService = apiService;
        this.notificationService = notificationService;
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
        tradingPairService.findAll().forEach(asset -> apiService.getCoinbaseTicker(asset.getName())
                .ifPresent(coinbaseTicker -> apiService.getBinanceTicker(asset.getName()).ifPresent(binanceTicker -> {
                    BigDecimal binancePrice = binanceTicker.price();
                    BigDecimal coinbasePrice = coinbaseTicker.price();
                    if (binancePrice != null && coinbasePrice != null) {
                        BigDecimal binanceBips = toBasePoints(binancePrice);
                        BigDecimal coinbaseBips = toBasePoints(coinbasePrice);
                        BigDecimal delta = coinbaseBips.subtract(binanceBips).abs();
                        if (delta.compareTo(BIPS_TRESHOLD) >= 0) {
                            BasePointsEventData data = new BasePointsEventData(asset.getName(), delta, binancePrice,
                                    binanceBips, coinbasePrice, coinbaseBips);
                            notificationService.notifyBasePointsDifference(new BasePointsEvent(data));
                        }
                    }
                    rateLimitDelay();
                })));
    }

    protected BigDecimal toBasePoints(BigDecimal assetPrice) {
        if (assetPrice != null && assetPrice.compareTo(BigDecimal.ZERO) > 0) {
            return assetPrice.divide(BIPS_MULTIPLIER, MathContext.DECIMAL128);
        } else {
            return BigDecimal.ZERO;
        }
    }

    protected Stream<String> findAssetsIntersection(List<CoinbaseProduct> coinbase, List<BinanceSymbol> binance) {
        return binance.stream().filter(s -> coinbase.stream().anyMatch(c -> isAssetSupported(s, c)))
                .map(BinanceSymbol::baseAsset);
    }

    protected boolean isAssetSupported(BinanceSymbol s, CoinbaseProduct c) {
        return Objects.equals(c.baseCurrency(), s.baseAsset()) && Objects.equals(c.quoteCurrency(), USD)
                && Objects.equals(s.quoteAsset(), USDT);
    }

    protected List<String> getSupportedAssets() {
        List<CoinbaseProduct> coinbasePairs = fetchSupportedCoinbaseProducts();
        return apiService.getBinanceExchangeInfo()
                .map(binance -> findAssetsIntersection(coinbasePairs, binance.symbols())).orElse(Stream.empty())
                .collect(Collectors.toList());
    }

    protected List<CoinbaseProduct> fetchSupportedCoinbaseProducts() {
        return Arrays.stream(apiService.getCoinbaseProducts().orElse(new CoinbaseProduct[0])).toList().stream()
                .filter(p -> p.quoteCurrency().equals(USD)).collect(Collectors.toList());
    }

    private void rateLimitDelay() {
        try {
            Thread.sleep(300);
        } catch (InterruptedException ex) {
            log.error(ex.getMessage(), ex);
        }
    }

}
