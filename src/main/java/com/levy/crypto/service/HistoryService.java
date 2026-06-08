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
    private final MarketTickerRepository marketTickerRepository;
    public HistoryService(MarketTickerRepository marketTickerRepository){
        this.marketTickerRepository = marketTickerRepository;
    }
    public List<MarketTicker> getHistoryBySymbol(String symbol) {
        List<MarketTicker> marketTickerList = marketTickerRepository.findBySymbol(symbol);

        return marketTickerList
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

        List<MarketTicker> marketTickerList = marketTickerRepository.findBySymbol(normalizedSymbol);

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
    public List<VolatilityRankingDto> getMostVolatile(long windows) {

        return marketTickerRepository.findDistinctSymbols().stream()
                .map(symbol -> {
                    double volatility = getVolatility(symbol, windows);
                    return new VolatilityRankingDto(symbol, volatility);
                })
                .filter(dto -> dto.getVolatility() != 0.0)
                .sorted(Comparator.comparing(VolatilityRankingDto::getVolatility).reversed())
                .limit(10)
                .toList();
    }
    private double getVolatility(String symbol, long windowMs) {
        String normalizedSymbol = symbol.toUpperCase();

        List<MarketTicker> marketTickerList = marketTickerRepository.findBySymbol(normalizedSymbol);

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
