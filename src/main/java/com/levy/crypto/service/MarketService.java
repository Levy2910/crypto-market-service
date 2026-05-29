package com.levy.crypto.service;

import com.levy.crypto.model.MarketTicker;
import lombok.Getter;
import lombok.Setter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.error.Mark;


import java.util.*;

@Service
@Getter
@Setter
public class MarketService {
    private final BinanceService binanceService;
    private List<MarketTicker> latestData = new ArrayList<>();
    private Map<String, List<MarketTicker>> historyData = new HashMap<>();
    public MarketService(BinanceService binanceService){
        this.binanceService = binanceService;
    }
    @Scheduled(fixedRate = 5000)
    public void fetchPrices() {
       List<MarketTicker> marketTickerList =  binanceService.fetchData();
       latestData = marketTickerList.stream()
                .filter(ticker -> ticker.getSymbol().endsWith("USDT"))
                .sorted(Comparator.comparing(MarketTicker::getChangePercent).reversed())
                .toList();
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
}
