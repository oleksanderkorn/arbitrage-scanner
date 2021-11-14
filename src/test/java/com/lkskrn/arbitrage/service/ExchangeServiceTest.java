package com.lkskrn.arbitrage.service;

import com.lkskrn.arbitrage.dto.BinanceSymbol;
import com.lkskrn.arbitrage.dto.BinanceSymbols;
import com.lkskrn.arbitrage.dto.CoinbaseProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ExchangeServiceTest {

    @Autowired
    private TradingAssetService tradingAssetService;

    @Autowired
    private BasePointsEventPublisher notificationService;

    private final APIService apiService = Mockito.mock(APIService.class);
    private final ExchangeService exchangeService = new ExchangeService(tradingAssetService, apiService, notificationService);

    @BeforeEach
    public void setup() {
        Mockito.when(apiService.getBinanceExchangeInfo()).thenReturn(Optional.of(testBinanceSymbols()));
        Mockito.when(apiService.getCoinbaseProducts()).thenReturn(Optional.of(testCoinbaseProducts()));

    }

    @Test
    public void shouldCalculateBase4PointsFromAssetPrice() {
        assertEquals("100000", exchangeService.toBasePoints(new BigDecimal("10.0")).toPlainString());
        assertEquals("53245", exchangeService.toBasePoints(new BigDecimal("5.3245")).toPlainString());
        assertEquals("1", exchangeService.toBasePoints(new BigDecimal("0.0001")).toPlainString());
    }

    @Test
    public void shouldFindIntersectionBetweenBinanceAndCoinbasePairs() {
        List<BinanceSymbol> binanceSymbols = testSymbols();

        List<CoinbaseProduct> coinbaseProducts = Arrays.stream(testCoinbaseProducts()).toList();

        List<String> assets = exchangeService.findAssetsIntersection(coinbaseProducts, binanceSymbols).collect(Collectors.toList());
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
        return List.of(
                symbolOf("BTC", "USDT"),
                symbolOf("ETH", "USDT"),
                symbolOf("BTC", "ETH")
        );
    }

    private CoinbaseProduct[] testCoinbaseProducts() {
        return new CoinbaseProduct[]{
                productOf("BTC", "USD"),
                productOf("ETH", "USD"),
                productOf("SHIB", "BTC")
        };
    }

    private BinanceSymbol symbolOf(String base, String quote) {
        return new BinanceSymbol(base.concat(quote), base, quote);
    }

    private CoinbaseProduct productOf(String base, String quote) {
        return new CoinbaseProduct(base.concat(quote), base, quote, "");
    }

}
