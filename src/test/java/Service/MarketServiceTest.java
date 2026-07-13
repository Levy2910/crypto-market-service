package Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.levy.crypto.dto.HealthDto;
import com.levy.crypto.dto.MarketSentimentDto;
import com.levy.crypto.model.MarketTicker;
import com.levy.crypto.repository.MarketTickerRepository;
import com.levy.crypto.service.BinanceService;
import com.levy.crypto.service.MarketService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MarketServiceTest {

    @Mock
    private BinanceService binanceService;

    @Mock
    private MarketTickerRepository repository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private MarketService marketService;

    @Test
    void shouldFetchPricesSuccessfully() {
        List<MarketTicker> data = List.of(
                ticker("BTCUSDT", 5, 1000),
                ticker("ETHUSDT", 10, 900),
                ticker("DOGEBTC", 20, 500)
        );

        when(binanceService.fetchData()).thenReturn(data);

        marketService.fetchPrices();

        assertEquals(2, marketService.getLatestData().size());
        assertEquals("ETHUSDT", marketService.getLatestData().get(0).getSymbol());

        verify(repository).saveAll(anyList());
    }
    @Test
    void shouldKeepPreviousDataWhenEmptyReturned() {

        marketService.setLatestData(List.of(ticker("BTCUSDT", 5, 100)));

        when(binanceService.fetchData()).thenReturn(Collections.emptyList());

        marketService.fetchPrices();

        assertEquals(1, marketService.getLatestData().size());

        verify(repository, never()).saveAll(any());
    }
    @Test
    void shouldThrowExceptionWhenBinanceFails() {

        when(binanceService.fetchData())
                .thenThrow(new RuntimeException("API error"));

        assertThrows(RuntimeException.class,
                () -> marketService.fetchPrices());

        assertFalse(marketService.isBinanceConnected());
    }
    @Test
    void shouldContainCoinIgnoringCase() {

        marketService.setLatestData(List.of(
                ticker("BTCUSDT", 5, 100)
        ));

        assertTrue(marketService.containsCoin("btcusdt"));
    }
    @Test
    void shouldReturnFalseWhenCoinNotFound() {

        marketService.setLatestData(List.of(
                ticker("BTCUSDT", 5, 100)
        ));

        assertFalse(marketService.containsCoin("ETHUSDT"));
    }
    @Test
    void shouldReturnCoin() {

        MarketTicker btc = ticker("BTCUSDT", 10, 100);

        marketService.setLatestData(List.of(btc));

        assertEquals(btc, marketService.getCoin("btcusdt"));
    }
    @Test
    void shouldThrowWhenCoinNotFound() {

        marketService.setLatestData(List.of());

        assertThrows(NoSuchElementException.class,
                () -> marketService.getCoin("BTCUSDT"));
    }
    @Test
    void shouldReturnTop10Performers() {

        List<MarketTicker> list = IntStream.range(0,15)
                .mapToObj(i -> ticker("COIN"+i, i, 100))
                .toList();

        marketService.setLatestData(list);

        assertEquals(10,
                marketService.getTopPerformers().size());
    }
    @Test
    void shouldReturnBottom10Performers() {

        List<MarketTicker> list = IntStream.range(0,15)
                .mapToObj(i -> ticker("COIN"+i, i, 100))
                .toList();

        marketService.setLatestData(list);

        assertEquals(10,
                marketService.getBottomPerformers().size());
    }
    @Test
    void shouldReturnLowestVolumeCoins() {

        List<MarketTicker> list = List.of(
                ticker("BTCUSDT",5,500),
                ticker("ETHUSDT",5,100),
                ticker("SOLUSDT",5,300)
        );

        marketService.setLatestData(list);

        List<MarketTicker> result = marketService.getTopVolumes();

        assertEquals("ETHUSDT", result.get(0).getSymbol());
    }
    @Test
    void shouldReturnHealthStatus() {

        marketService.setBinanceConnected(true);
        marketService.setLatestData(List.of(
                ticker("BTCUSDT",5,100)
        ));

        HealthDto dto = marketService.getHealth();

        assertEquals("Up", dto.getStatus());
        assertEquals(1, dto.getLatestDataSize());
    }
    @Test
    void shouldCalculateSentiment() {

        marketService.setLatestData(List.of(
                ticker("BTCUSDT",10,100),
                ticker("ETHUSDT",-5,100),
                ticker("SOLUSDT",0,100)
        ));

        MarketSentimentDto dto = marketService.getMarketSentiment();

        assertEquals(33.333333333333336,
                dto.getBullishPercentage());

        assertEquals(3, dto.getTotalCoins());
    }
    @Test
    void shouldReturnEmptySentimentWhenNoData() {

        marketService.setLatestData(List.of());

        MarketSentimentDto dto = marketService.getMarketSentiment();

        assertEquals(0, dto.getTotalCoins());
    }

    private MarketTicker ticker(String symbol, double changePercent, double volume) {
        MarketTicker ticker = new MarketTicker();
        ticker.setSymbol(symbol);
        ticker.setChangePercent(changePercent);
        ticker.setVolume(volume);
        return ticker;
    }
}