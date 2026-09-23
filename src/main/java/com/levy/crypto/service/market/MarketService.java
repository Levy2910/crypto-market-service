package com.levy.crypto.service.market;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.levy.crypto.dto.HealthDto;
import com.levy.crypto.dto.MarketSentimentDto;
import com.levy.crypto.dto.MarketSummaryDto;
import com.levy.crypto.model.MarketTicker;
import com.levy.crypto.repository.MarketTickerRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class MarketService {

    private final LatestMarketState latestMarketState;
    private final MarketTickerRepository marketTickerRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public MarketService(
            LatestMarketState latestMarketState,
            MarketTickerRepository marketTickerRepository,
            StringRedisTemplate stringRedisTemplate,
            ObjectMapper objectMapper
    ) {
        this.latestMarketState = latestMarketState;
        this.marketTickerRepository = marketTickerRepository;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Get the latest market state from the Kafka consumer.
     */
    private List<MarketTicker> getLatestData() {

        return latestMarketState.getAll()
                .values()
                .stream()
                .filter(ticker -> ticker.getSymbol().endsWith("USDT"))
                .sorted(
                        Comparator.comparing(
                                MarketTicker::getChangePercent
                        ).reversed()
                )
                .toList();
    }

    public HealthDto getHealth() {

        List<MarketTicker> latestData = getLatestData();

        String status = latestData.isEmpty()
                ? "Down"
                : "Up";

        return new HealthDto(
                status,
                latestData.size(),
                0L,
                !latestData.isEmpty()
        );
    }

    public boolean containsCoin(String symbol) {

        String normalizedSymbol = symbol.toUpperCase();

        return getLatestData()
                .stream()
                .anyMatch(data ->
                        data.getSymbol().equals(normalizedSymbol)
                );
    }

    public List<MarketTicker> getTopPerformers() {

        return getLatestData()
                .stream()
                .limit(10)
                .toList();
    }

    public List<MarketTicker> getBottomPerformers() {

        List<MarketTicker> latestData = getLatestData();

        return latestData.stream()
                .skip(
                        Math.max(
                                0,
                                latestData.size() - 10
                        )
                )
                .toList();
    }

    public List<MarketTicker> getTopVolumes() {

        return getLatestData()
                .stream()
                .sorted(
                        Comparator.comparing(
                                MarketTicker::getVolume
                        ).reversed()
                )
                .limit(10)
                .toList();
    }

    public MarketSentimentDto getMarketSentiment() {

        List<MarketTicker> latestData = getLatestData();

        if (latestData.isEmpty()) {
            return new MarketSentimentDto();
        }

        int total = latestData.size();

        double bullish =
                latestData.stream()
                        .filter(data ->
                                data.getChangePercent() > 0
                        )
                        .count() * 100.0 / total;

        double bearish =
                latestData.stream()
                        .filter(data ->
                                data.getChangePercent() < 0
                        )
                        .count() * 100.0 / total;

        double neutral =
                latestData.stream()
                        .filter(data ->
                                data.getChangePercent() == 0
                        )
                        .count() * 100.0 / total;

        return new MarketSentimentDto(
                bullish,
                bearish,
                neutral,
                total
        );
    }

    public MarketTicker getCoin(String symbol) {

        String normalizedSymbol =
                symbol.toUpperCase();

        return getLatestData()
                .stream()
                .filter(data ->
                        data.getSymbol().equals(normalizedSymbol)
                )
                .findFirst()
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Coin not found"
                        )
                );
    }

    public MarketSummaryDto getMarketSummary() {

        String key = "market-summary";

        String cached =
                stringRedisTemplate.opsForValue()
                        .get(key);

        if (cached != null) {

            try {

                return objectMapper.readValue(
                        cached,
                        MarketSummaryDto.class
                );

            } catch (Exception e) {

                throw new RuntimeException(
                        "Failed to deserialize cache",
                        e
                );
            }
        }

        List<MarketTicker> latestData =
                getLatestData();

        MarketSummaryDto marketSummary =
                new MarketSummaryDto();

        int totalPairs =
                latestData.size();

        double average =
                latestData.stream()
                        .mapToDouble(
                                MarketTicker::getChangePercent
                        )
                        .average()
                        .orElse(0.0);

        List<MarketTicker> topGainers =
                latestData.stream()
                        .limit(10)
                        .toList();

        List<MarketTicker> topLosers =
                latestData.stream()
                        .skip(
                                Math.max(
                                        0,
                                        latestData.size() - 10
                                )
                        )
                        .toList();

        marketSummary.setTotalPairs(
                totalPairs
        );

        marketSummary.setAverageChange(
                average
        );

        marketSummary.setTopGainers(
                topGainers
        );

        marketSummary.setTopLosers(
                topLosers
        );

        try {

            String json =
                    objectMapper.writeValueAsString(
                            marketSummary
                    );

            stringRedisTemplate.opsForValue().set(
                    key,
                    json,
                    Duration.ofSeconds(30)
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to serialize cache",
                    e
            );
        }

        return marketSummary;
    }

    public String getPriceBySymbol(String symbol) {

        String normalizedSymbol =
                symbol.toUpperCase();

        String cached =
                stringRedisTemplate.opsForValue()
                        .get(normalizedSymbol);

        if (cached != null) {
            return cached;
        }

        MarketTicker ticker =
                marketTickerRepository
                        .findFirstBySymbolOrderByTimestampDesc(
                                normalizedSymbol
                        );

        if (ticker != null) {

            stringRedisTemplate.opsForValue().set(
                    normalizedSymbol,
                    String.valueOf(
                            ticker.getPrice()
                    ),
                    Duration.ofSeconds(30)
            );

            return String.valueOf(
                    ticker.getPrice()
            );
        }

        return null;
    }
}
