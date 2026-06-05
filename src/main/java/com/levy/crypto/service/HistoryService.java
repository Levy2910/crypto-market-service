package com.levy.crypto.service;

import com.levy.crypto.dto.MarketSentimentDto;
import com.levy.crypto.dto.MetricsDto;
import com.levy.crypto.dto.VolatilityDto;
import com.levy.crypto.dto.VolatilityRankingDto;
import com.levy.crypto.model.MarketTicker;
import com.levy.crypto.repository.MarketTickerRepository;
import org.springframework.stereotype.Service;

import java.util.*;
@Service
public class HistoryService {
    private final Map<String, List<MarketTicker>> historyData = new HashMap<>();
    private final MarketTickerRepository marketTickerRepository;
    public HistoryService(MarketTickerRepository marketTickerRepository){
        this.marketTickerRepository = marketTickerRepository;
    }

    public void store(List<MarketTicker> data) {
        for (MarketTicker marketTicker : data) {
            String symbol = marketTicker.getSymbol();

            historyData.putIfAbsent(symbol, new ArrayList<>());
            historyData.get(symbol).add(marketTicker);

            if (historyData.get(symbol).size() > 100) {
                historyData.get(symbol).removeFirst();
            }
        }
    }
    public List<MarketTicker> getHistoryBySymbol(String symbol) {
        String normalizedSymbol = symbol.toUpperCase();

        return historyData.getOrDefault(normalizedSymbol, Collections.emptyList())
                .stream()
                .limit(100)
                .toList();
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
    private double getAverage(String symbol, long windowMs) {
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
    public List<VolatilityRankingDto> getMostVolatile(long windows){
        if (historyData.isEmpty()){
            return new ArrayList<>();
        }
        List<VolatilityRankingDto> volatilityRankingDtos = new ArrayList<>();
        for (String coin : historyData.keySet()){
            double volatilityCal = getVolatility(coin, windows);
            if (volatilityCal == 0.0){
                continue;
            }
            volatilityRankingDtos.add(new VolatilityRankingDto(coin, volatilityCal));
        }
        return volatilityRankingDtos.stream().sorted(Comparator.comparing(VolatilityRankingDto::getVolatility).reversed()).limit(10).toList();
    }
    private double getVolatility(String symbol, long windowMs) {
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
