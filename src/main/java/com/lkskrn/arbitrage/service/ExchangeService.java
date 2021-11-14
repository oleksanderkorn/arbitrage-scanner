package com.lkskrn.arbitrage.service;

import com.lkskrn.arbitrage.dto.BinanceSymbol;
import com.lkskrn.arbitrage.dto.CoinbaseProduct;
import com.lkskrn.arbitrage.dto.Exchange;
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

    @Autowired
    public ExchangeService(TradingAssetService tradingPairService, APIService apiService) {
        this.tradingPairService = tradingPairService;
        this.apiService = apiService;
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
        tradingPairService.findAll().forEach(asset -> apiService.getCoinbaseTicker(asset).ifPresent(coinbaseTicker ->
                apiService.getBinanceTicker(asset).ifPresent(binanceTicker -> {
                    BigDecimal binancePrice = binanceTicker.price();
                    BigDecimal coinbasePrice = coinbaseTicker.price();
                    BigDecimal binanceBips = toBasePoints(binancePrice);
                    BigDecimal coinbaseBips = toBasePoints(coinbasePrice);
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
                })));
    }

    protected BigDecimal toBasePoints(BigDecimal assetPrice) {
        return assetPrice.divide(BIPS_MULTIPLIER, MathContext.DECIMAL128);
    }

    protected Stream<String> findAssetsIntersection(List<CoinbaseProduct> coinbase, List<BinanceSymbol> binance) {
        return binance.stream()
                .filter(s -> coinbase.stream().anyMatch(c -> isAssetSupported(s, c)))
                .map(BinanceSymbol::baseAsset);
    }

    protected boolean isAssetSupported(BinanceSymbol s, CoinbaseProduct c) {
        return Objects.equals(c.baseCurrency(), s.baseAsset()) &&
                Objects.equals(c.quoteCurrency(), USD) &&
                Objects.equals(s.quoteAsset(), USDT);
    }

    protected List<String> getSupportedAssets() {
        List<CoinbaseProduct> coinbasePairs = fetchSupportedCoinbaseProducts();
        return apiService.getBinanceExchangeInfo()
                .map(binance -> findAssetsIntersection(coinbasePairs, binance.symbols()))
                .orElse(Stream.empty()).collect(Collectors.toList());
    }

    protected List<CoinbaseProduct> fetchSupportedCoinbaseProducts() {
        return Arrays.stream(apiService.getCoinbaseProducts().orElse(new CoinbaseProduct[0]))
                .toList().stream()
                .filter(p -> p.quoteCurrency().equals(USD))
                .collect(Collectors.toList());
    }

    private void rateLimitDelay() {
        try {
            Thread.sleep(300);
        } catch (InterruptedException ex) {
            log.error(ex.getMessage(), ex);
        }
    }

}
