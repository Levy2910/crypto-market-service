package com.levy.crypto.controller;

import com.levy.crypto.dto.*;
import com.levy.crypto.exception.CoinNotFoundException;
import com.levy.crypto.model.MarketTicker;
import com.levy.crypto.service.HistoryService;
import com.levy.crypto.service.MarketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class MarketController {

    private final MarketService marketService;
    private final HistoryService historyService;
    public MarketController(MarketService marketService, HistoryService historyService){
        this.marketService = marketService;
        this.historyService = historyService;
    }
    @GetMapping("/top-performers")
    public List<MarketTicker> getTopPerformers() {
        return marketService.getTopPerformers();
    }
    @GetMapping("/bottom-performers")
    public List<MarketTicker> getBottomPerformers() {
        return marketService.getBottomPerformers();
    }
    @GetMapping("/top-volumes")
    public List<MarketTicker> getTopVolumes(){
        return marketService.getTopVolumes();
    }
    @GetMapping("/coin/{symbol}")
    public MarketTicker getCoin(@PathVariable String symbol) {
        if (!marketService.containsCoin(symbol)) {
            throw new CoinNotFoundException(symbol);
        }
        return marketService.getCoin(symbol);
    }
    @GetMapping("/history/{symbol}")
    public ResponseEntity<List<MarketTicker>> getHistory(@PathVariable String symbol) {
        if (!marketService.containsCoin(symbol)) {
            throw new CoinNotFoundException(symbol);
        }
        List<MarketTicker> marketTickerList = historyService.getHistoryBySymbol(symbol);
        return ResponseEntity.ok().body(marketTickerList);
    }
    @GetMapping("/metrics/{symbol}")
    public ResponseEntity<MetricsDto> getAveragePriceChange(@PathVariable String symbol) {
        if (!marketService.containsCoin(symbol)) {
            throw new CoinNotFoundException(symbol);
        }
        MetricsDto metricsDto = historyService.getMetrics(symbol);
        return ResponseEntity.ok().body(metricsDto);
    }
    @GetMapping("/volatility/{symbol}")
    public ResponseEntity<VolatilityDto> getVolatility(@PathVariable String symbol) {
        if (!marketService.containsCoin(symbol)) {
            throw new CoinNotFoundException(symbol);
        }
        VolatilityDto volatilityDto = historyService.getVolatilityBySymbol(symbol);
        return ResponseEntity.ok().body(volatilityDto);
    }
    @GetMapping("/market-summary")
    public ResponseEntity<MarketSummaryDto> getMarketSummary(){
        MarketSummaryDto summary = marketService.getMarketSummary();
        return ResponseEntity.ok().body(summary);
    }
    @GetMapping("/biggest-movers")
    public ResponseEntity<List<MarketTickerMover>> getBiggestMovers(){
        List<MarketTickerMover> marketTickerMovers = marketService.getBiggestMovers();
        return ResponseEntity.ok().body(marketTickerMovers);
    }
    @GetMapping("/health")
    public ResponseEntity<HealthDto> getHealth(){
        HealthDto healthDto = marketService.getHealth();
        return ResponseEntity.ok().body(healthDto);
    }
}
