package com.levy.crypto.service;

import com.levy.crypto.dto.MetricsDto;
import com.levy.crypto.dto.VolatilityDto;
import com.levy.crypto.model.MarketTicker;
import org.springframework.stereotype.Service;

import java.util.*;
@Service
public class HistoryService {
    private final Map<String, List<MarketTicker>> historyData = new HashMap<>();

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
    //TODO: fix it tomorrow 3rd June
//    public List<VolatilityDto> getMostVolatile(){
//        List<VolatilityDto> volatilityDtos = new ArrayList<>();
//        for (String coin : historyData.keySet()){
//            List<MarketTicker> marketTickerList = historyData.get(coin);
//            if (marketTickerList == null || marketTickerList.size() < 2) {
//                volatilityDtos.add(new VolatilityDto(coin, 0.0, 0.0));
//                continue;
//            }
//            long currTime = System.currentTimeMillis();
//            long oneMin = 60*1000;
//            long fiveMins = 5*60*1000;
//            List<Double> pricesIn1Min = marketTickerList.stream()
//                    .filter(data -> data.getTimestamp() >= currTime - oneMin)
//                    .map(MarketTicker::getPrice)
//                    .sorted()
//                    .toList();
//            double minPriceIn1Min = pricesIn1Min.getFirst();
//            double maxPriceIn1Mins = pricesIn1Min.getLast();
//            double volatilityAfter1Min = ((maxPriceIn1Mins - minPriceIn1Min) / minPriceIn1Min) * 100;
//
//            List<Double> pricesIn5Min = marketTickerList.stream()
//                    .filter(data -> data.getTimestamp() >= currTime - fiveMins)
//                    .map(MarketTicker::getPrice)
//                    .sorted()
//                    .toList();
//            double minPriceIn5Min = pricesIn1Min.getFirst();
//            double maxPriceIn5Mins = pricesIn1Min.getLast();
//            double volatilityAfter5Min = ((maxPriceIn5Mins - minPriceIn5Min) / minPriceIn5Min) * 100;
//            volatilityDtos.add(new VolatilityDto(coin, volatilityAfter1Min, volatilityAfter5Min));
//        }
//         return volatilityDtos.stream()
//                 .sorted(Comparator.comparing(VolatilityDto::getVolatility1Min)).toList();
//    }
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
