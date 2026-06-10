package com.levy.crypto.service;

import com.levy.crypto.dto.MetricsDto;
import com.levy.crypto.dto.VolatilityDto;
import com.levy.crypto.dto.VolatilityRankingDto;
import com.levy.crypto.model.MarketTicker;
import com.levy.crypto.repository.MarketTickerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
@Service
public class HistoryService {
    private final MarketTickerRepository marketTickerRepository;
    public HistoryService(MarketTickerRepository marketTickerRepository){
        this.marketTickerRepository = marketTickerRepository;
    }
    public Page<MarketTicker> getHistoryBySymbol(String symbol, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return marketTickerRepository.findBySymbol(symbol, pageable);
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
        LocalDateTime cutoff =
                LocalDateTime.now().minusNanos(windowMs * 1_000_000);
        Double average = marketTickerRepository
                .getAveragePrice(normalizedSymbol, cutoff);
        return average != null ? average : 0.0;
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
    private double getVolatility(String symbol, long windowMs){
        String normalizedSymbol = symbol.toUpperCase();
        LocalDateTime cutoff = LocalDateTime.now().minusNanos(windowMs*1_000_000);
        Double volatility = marketTickerRepository.getVolatility(normalizedSymbol, cutoff);
        return volatility != null ? volatility : 0.0;
    }
}
