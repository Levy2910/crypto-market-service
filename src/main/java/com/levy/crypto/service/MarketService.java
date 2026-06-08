package com.levy.crypto.service;

import com.levy.crypto.dto.*;
import com.levy.crypto.model.MarketTicker;
import com.levy.crypto.repository.MarketTickerRepository;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.jackson.autoconfigure.JacksonProperties;
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
    private long lastUpdated;
    private Map<String, Integer> previousRanks = new HashMap<>();
    private Map<String, Integer> currentRanks = new HashMap<>();
    private static final Logger log =
            LoggerFactory.getLogger(MarketService.class);
    private boolean binanceConnected = false;
    private final MarketTickerRepository marketTickerRepository;
    public MarketService(BinanceService binanceService, MarketTickerRepository marketTickerRepository){
        this.binanceService = binanceService;
        this.marketTickerRepository = marketTickerRepository;
    }
    @Scheduled(fixedRate = 5000)
    public void fetchPrices() {
        try {
            List<MarketTicker> marketTickerList =  binanceService.fetchData();
            if (marketTickerList.isEmpty()) {
                log.warn("No data fetched from Binance. Keeping previous market data.");
                return;
            }
            previousRanks = currentRanks;
            latestData = marketTickerList.stream()
                    .filter(ticker -> ticker.getSymbol().endsWith("USDT"))
                    .sorted(Comparator.comparing(MarketTicker::getChangePercent).reversed())
                    .toList();
            marketTickerRepository.saveAll(latestData);
            currentRanks = buildRank(latestData);
            lastUpdated = System.currentTimeMillis();
            binanceConnected = true;
            log.info("Fetched {} USDT coins", latestData.size());
        } catch (Exception e) {
            binanceConnected = false;
            throw new RuntimeException(e);
        }
    }
    public HealthDto getHealth(){
        String status = "Down";
       if (binanceConnected){
           status = "Up";
       }
       return new HealthDto(status, latestData.size(), lastUpdated, binanceConnected);
    }

    private Map<String, Integer> buildRank(List<MarketTicker> latestData) {
        Map<String, Integer> map = new HashMap<>();
        for (int i=0; i < latestData.size(); i++){
            map.put(latestData.get(i).getSymbol(), i+1);
        }
        return map;
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
    public List<MarketTicker> getTopVolumes(){
        return latestData.stream()
                .sorted(Comparator.comparing(MarketTicker::getVolume))
                .limit(10)
                .toList();
    }
    public MarketSentimentDto getMarketSentiment(){
        if (latestData.isEmpty()){
            return new MarketSentimentDto();
        }
        int total = latestData.size();
        double bullish =
                latestData.stream()
                        .filter(data -> data.getChangePercent() > 0)
                        .count() * 100.0 / total;
        double bearish =
                latestData.stream()
                        .filter(data -> data.getChangePercent() < 0)
                        .count() * 100.0 / total;
        double neutral =
                latestData.stream()
                        .filter(data -> data.getChangePercent() == 0)
                        .count() * 100.0 / total;
        return new MarketSentimentDto(bullish, bearish, neutral, total);
    }

    public MarketTicker getCoin(String symbol) {
        String normalizedSymbol = symbol.toUpperCase();

        return latestData.stream()
                .filter(data -> data.getSymbol().equals(normalizedSymbol))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Coin not found"));
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

    public List<MarketTickerMover> getBiggestMovers() {
        List<MarketTickerMover> marketTickerMovers = new ArrayList<>();
        for (MarketTicker marketTicker : latestData){
            String symbol = marketTicker.getSymbol();
            if (!currentRanks.containsKey(symbol) || !previousRanks.containsKey(symbol)){
                continue;
            }
            int prevRank = previousRanks.get(symbol);
            int currRank = currentRanks.get(symbol);
            MarketTickerMover marketTickerMover = new MarketTickerMover(symbol, currRank, prevRank, prevRank - currRank);
            marketTickerMovers.add(marketTickerMover);
        }
        return marketTickerMovers.stream().filter(mover -> mover.getRankJumped() > 0).sorted(Comparator.comparing(MarketTickerMover::getRankJumped).reversed()).limit(10).toList();

    }
}
