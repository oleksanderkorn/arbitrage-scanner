package com.lkskrn.arbitrage.service;

import com.lkskrn.arbitrage.dto.*;
import com.lkskrn.arbitrage.events.BasePointsEvent;
import com.lkskrn.arbitrage.model.TradingAsset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ExchangeServiceTest {

    private final BasePointsEventPublisher basePointsPublisher = mock(BasePointsEventPublisher.class);
    private final APIService apiService = mock(APIService.class);
    private final TradingAssetService tradingAssetService = mock(TradingAssetService.class);
    private final ExchangeService exchangeService = new ExchangeService(tradingAssetService, apiService,
            basePointsPublisher);
    private final LinkedBlockingQueue<BasePointsEvent> events = new LinkedBlockingQueue<>();

    @BeforeEach
    public void setup() {
        when(apiService.getBinanceExchangeInfo()).thenReturn(Optional.of(testBinanceSymbols()));
        when(apiService.getCoinbaseProducts()).thenReturn(Optional.of(testCoinbaseProducts()));
        when(tradingAssetService.findAll()).thenReturn(testTradingAssets());
        when(apiService.getBinanceTicker("BTC")).thenReturn(tickerOf(65000D));
        when(apiService.getCoinbaseTicker("BTC")).thenReturn(tickerOf(65001D));
        when(apiService.getBinanceTicker("ETH")).thenReturn(tickerOf(4777D));
        when(apiService.getCoinbaseTicker("ETH")).thenReturn(tickerOf(4776D));
        when(apiService.getBinanceTicker("SHIB")).thenReturn(tickerOf(0.000052));
        when(apiService.getCoinbaseTicker("SHIB")).thenReturn(tickerOf(0.000059));
    }

    @Test
    public void shouldPublishEventWhenBasePointsDifferenceIsHigherThanThreshold() {
        doAnswer(invocation -> {
            BasePointsEvent event = invocation.getArgument(0);
            events.add(event);
            return null;
        }).when(basePointsPublisher).notifyBasePointsDifference(any(BasePointsEvent.class));
        exchangeService.compareTradingPairs();
        assertEquals(2, events.size());
        assertTrue(events.stream().anyMatch(e -> Objects.equals(((BasePointsEventData) e.getSource()).id(), "BTC")));
        assertTrue(events.stream().anyMatch(e -> Objects.equals(((BasePointsEventData) e.getSource()).id(), "ETH")));
        assertFalse(events.stream().anyMatch(e -> Objects.equals(((BasePointsEventData) e.getSource()).id(), "SHIB")));
    }

    @Test
    public void shouldCalculateBasePointsFromAssetPrice() {
        assertEquals("100000", exchangeService.toBasePoints(new BigDecimal("10.0")).toPlainString());
        assertEquals("53245", exchangeService.toBasePoints(new BigDecimal("5.3245")).toPlainString());
        assertEquals("1", exchangeService.toBasePoints(new BigDecimal("0.0001")).toPlainString());
    }

    @Test
    public void shouldFindIntersectionBetweenBinanceAndCoinbasePairs() {
        List<BinanceSymbol> binanceSymbols = testSymbols();
        List<CoinbaseProduct> coinbaseProducts = Arrays.stream(testCoinbaseProducts()).toList();
        List<String> assets = exchangeService.findAssetsIntersection(coinbaseProducts, binanceSymbols)
                .collect(Collectors.toList());
        assertEquals(2, assets.size());
        assertTrue(assets.contains("BTC"));
        assertTrue(assets.contains("ETH"));
        assertFalse(assets.contains("SHIB"));
    }

    @Test
    public void shouldCheckSupportedAsset() {
        assertTrue(exchangeService.isAssetSupported(symbolOf("BTC", "USDT"), productOf("BTC", "USD")));
        assertFalse(exchangeService.isAssetSupported(symbolOf("USDT", "LINK"), productOf("BTC", "USD")));
        assertFalse(exchangeService.isAssetSupported(symbolOf("BTC", "USDT"), productOf("SHIB", "BTC")));
    }

    @Test
    public void shouldGetSupportedAssetsFromApiService() {
        List<String> assets = exchangeService.getSupportedAssets();
        assertEquals(2, assets.size());
        assertTrue(assets.contains("BTC"));
        assertTrue(assets.contains("ETH"));
        assertFalse(assets.contains("SHIB"));
    }

    private BinanceSymbols testBinanceSymbols() {
        return new BinanceSymbols(testSymbols());
    }

    private List<BinanceSymbol> testSymbols() {
        return List.of(symbolOf("BTC", "USDT"), symbolOf("ETH", "USDT"), symbolOf("BTC", "ETH"));
    }

    private CoinbaseProduct[] testCoinbaseProducts() {
        return new CoinbaseProduct[] { productOf("BTC", "USD"), productOf("ETH", "USD"), productOf("SHIB", "BTC") };
    }

    private BinanceSymbol symbolOf(String base, String quote) {
        return new BinanceSymbol(base.concat(quote), base, quote);
    }

    private CoinbaseProduct productOf(String base, String quote) {
        return new CoinbaseProduct(base.concat(quote), base, quote, "");
    }

    private List<TradingAsset> testTradingAssets() {
        return List.of(assetOf("BTC"), assetOf("ETH"), assetOf("SHIB"));
    }

    private TradingAsset assetOf(String btc) {
        return new TradingAsset(btc);
    }

    private Optional<ProductTicker> tickerOf(Double lastPrice) {
        return Optional.of(new ProductTicker(BigDecimal.valueOf(lastPrice)));
    }

}
