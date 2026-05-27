package com.levy.crypto.service;

import com.levy.crypto.model.MarketTicker;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;

@Service
public class MarketService {
    private final BinanceService binanceService;
    private List<MarketTicker> latestData = new ArrayList<>();
    private List<MarketTicker> historyData = new ArrayList<>();
    public MarketService(BinanceService binanceService){
        this.binanceService = binanceService;
    }
    @Scheduled(fixedRate = 5000)
    public List<MarketTicker> fetchPrices() {
       List<MarketTicker> marketTickerList =  binanceService.fetchData();
       historyData.addAll(marketTickerList);
       latestData = marketTickerList.stream()
                .filter(ticker -> ticker.getSymbol().endsWith("USDT"))
                .sorted((a, b) -> Double.compare(b.getChangePercent(), a.getChangePercent()))
                .toList();
       return marketTickerList;
    }
}
