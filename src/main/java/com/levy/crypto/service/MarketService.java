package com.levy.crypto.service;

import com.levy.crypto.dto.MarketSummaryDto;
import com.levy.crypto.dto.MetricsDto;
import com.levy.crypto.dto.VolatilityDto;
import com.levy.crypto.model.MarketTicker;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


import java.util.*;

@Service
@Getter
@Setter
public class MarketService {
    private final BinanceService binanceService;
    private List<MarketTicker> latestData = new ArrayList<>();
    private Map<String, List<MarketTicker>> historyData = new HashMap<>();
    private long lastUpdated;
    private static final Logger log =
            LoggerFactory.getLogger(MarketService.class);
    public MarketService(BinanceService binanceService){
        this.binanceService = binanceService;
    }
    @Scheduled(fixedRate = 5000)
    public void fetchPrices() {
       List<MarketTicker> marketTickerList =  binanceService.fetchData();
        if (marketTickerList.isEmpty()) {
            log.warn("No data fetched from Binance. Keeping previous market data.");
            return;
        }
       latestData = marketTickerList.stream()
                .filter(ticker -> ticker.getSymbol().endsWith("USDT"))
                .sorted(Comparator.comparing(MarketTicker::getChangePercent).reversed())
                .toList();
        lastUpdated = System.currentTimeMillis();
        log.info("Fetched {} USDT coins", latestData.size());
       storeHistory(latestData);
    }

    private void storeHistory(List<MarketTicker> data) {
        for (MarketTicker marketTicker : data) {
            String symbol = marketTicker.getSymbol();

            historyData.putIfAbsent(symbol, new ArrayList<>());
            historyData.get(symbol).add(marketTicker);

            if (historyData.get(symbol).size() > 100) {
                historyData.get(symbol).remove(0);
            }
        }
    }
    public boolean containsCoin(String symbol) {
        String normalizedSymbol = symbol.toUpperCase();

        return latestData.stream()
                .anyMatch(data -> data.getSymbol().equals(normalizedSymbol));
    }
    public List<MarketTicker> getTopPerformers() {
        return latestData.stream().limit(10).toList();
    }
    public List<MarketTicker> getBottomPerformers() {
        return latestData.stream()
                .skip(Math.max(0, latestData.size() - 10))
                .toList();
    }

    public MarketTicker getCoin(String symbol) {
        String normalizedSymbol = symbol.toUpperCase();

        return latestData.stream()
                .filter(data -> data.getSymbol().equals(normalizedSymbol))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Coin not found"));
    }

    public List<MarketTicker> getHistoryBySymbol(String symbol) {
        String normalizedSymbol = symbol.toUpperCase();

        return historyData.getOrDefault(normalizedSymbol, Collections.emptyList())
                .stream()
                .limit(100)
                .toList();
    }

    public double getAverage(String symbol, long windowMs) {
        String normalizedSymbol = symbol.toUpperCase();

        List<MarketTicker> marketTickerList = historyData.get(normalizedSymbol);

        if (marketTickerList == null || marketTickerList.isEmpty()) {
            return 0.0;
        }

        long currentTime = System.currentTimeMillis();

        List<MarketTicker> filteredList = marketTickerList.stream()
                .filter(data -> data.getTimestamp() >= currentTime - windowMs)
                .toList();

        if (filteredList.isEmpty()) {
            return 0.0;
        }

        return filteredList.stream()
                .mapToDouble(MarketTicker::getPrice)
                .average()
                .orElse(0.0);
    }

    public double getVolatility(String symbol, long windowMs) {
        String normalizedSymbol = symbol.toUpperCase();

        List<MarketTicker> marketTickerList = historyData.get(normalizedSymbol);

        if (marketTickerList == null || marketTickerList.size() < 2) {
            return 0.0;
        }

        long currentTime = System.currentTimeMillis();

        List<Double> prices = marketTickerList.stream()
                .filter(data -> data.getTimestamp() >= currentTime - windowMs)
                .map(MarketTicker::getPrice)
                .sorted()
                .toList();

        if (prices.size() < 2) {
            return 0.0;
        }

        double minPrice = prices.getFirst();
        double maxPrice = prices.getLast();

        if (minPrice == 0.0) {
            return 0.0;
        }

        return ((maxPrice - minPrice) / minPrice) * 100;
    }

    public MarketSummaryDto getMarketSummary() {
        if (latestData.isEmpty()) {
            return new MarketSummaryDto(0, 0.0, List.of(), List.of(), lastUpdated);
        }
        MarketSummaryDto marketSummary = new MarketSummaryDto();
        int totalPairs = latestData.size();
        double average = latestData.stream().mapToDouble(MarketTicker::getChangePercent).average().orElse(0.0);
        List<MarketTicker> topGainers = latestData.stream().limit(10).toList();
        List<MarketTicker> topLosers = latestData.stream().skip(Math.max(0, latestData.size() - 10)).toList();
        marketSummary.setTotalPairs(totalPairs);
        marketSummary.setAverageChange(average);
        marketSummary.setTopGainers(topGainers);
        marketSummary.setTopLosers(topLosers);
        marketSummary.setLastUpdated(lastUpdated);

        return marketSummary;
    }

    public MetricsDto getMetrics(String symbol) {
        String normalizedSymbol = symbol.toUpperCase();
        double averageAfter1Min = getAverage(normalizedSymbol, 60*1000);
        double averageAfter5Min = getAverage(normalizedSymbol, 5*60*1000);
        MetricsDto metricsDto = new MetricsDto();
        metricsDto.setMovingAverage1Min(averageAfter1Min);
        metricsDto.setMovingAverage5Min(averageAfter5Min);
        metricsDto.setSymbol(normalizedSymbol);
        return metricsDto;
    }

    public VolatilityDto getVolatilityBySymbol(String symbol) {
        String normalizedSymbol = symbol.toUpperCase();
        double volatilityAfter1Min = getVolatility(normalizedSymbol, 60*1000);
        double volatilityAfter5Min = getVolatility(normalizedSymbol, 5*60*1000);
        VolatilityDto volatilityDto = new VolatilityDto();
        volatilityDto.setSymbol(normalizedSymbol);
        volatilityDto.setVolatility1Min(volatilityAfter1Min);
        volatilityDto.setVolatility5Min(volatilityAfter5Min);
        return volatilityDto;
    }
}
