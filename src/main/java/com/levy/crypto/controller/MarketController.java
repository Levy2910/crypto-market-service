package com.levy.crypto.controller;

import com.levy.crypto.dto.MarketSummaryDto;
import com.levy.crypto.dto.MetricsDto;
import com.levy.crypto.dto.VolatilityDto;
import com.levy.crypto.exception.CoinNotFoundException;
import com.levy.crypto.model.MarketTicker;
import com.levy.crypto.service.MarketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class MarketController {

    private final MarketService marketService;
    public MarketController(MarketService marketService){
        this.marketService = marketService;
    }
    @GetMapping("/top-performers")
    public List<MarketTicker> getTopPerformers() {
        return marketService.getTopPerformers();
    }
    @GetMapping("/bottom-performers")
    public List<MarketTicker> getBottomPerformers() {
        return marketService.getBottomPerformers();
    }
    @GetMapping("/coin/{symbol}")
    public MarketTicker getCoin(@PathVariable String symbol) throws CoinNotFoundException {
        if (!marketService.containsCoin(symbol)) {
            throw new CoinNotFoundException(symbol);
        }
        return marketService.getCoin(symbol);
    }
    @GetMapping("/history/{symbol}")
    public ResponseEntity<List<MarketTicker>> getHistory(@PathVariable String symbol) throws CoinNotFoundException {
        if (!marketService.containsCoin(symbol)) {
            throw new CoinNotFoundException(symbol);
        }
        List<MarketTicker> marketTickerList = marketService.getHistoryBySymbol(symbol);
        if(marketTickerList  == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(marketTickerList);
    }
    @GetMapping("/metrics/{symbol}")
    public ResponseEntity<MetricsDto> getAveragePriceChange(@PathVariable String symbol) throws CoinNotFoundException {
        if (!marketService.containsCoin(symbol)) {
            throw new CoinNotFoundException(symbol);
        }
        MetricsDto metricsDto = marketService.getMetrics(symbol);
        return ResponseEntity.ok().body(metricsDto);
    }
    @GetMapping("/volatility/{symbol}")
    public ResponseEntity<VolatilityDto> getVolatility(@PathVariable String symbol) throws CoinNotFoundException {
        if (!marketService.containsCoin(symbol)) {
            throw new CoinNotFoundException(symbol);
        }
        VolatilityDto volatilityDto = marketService.getVolatilityBySymbol(symbol);
        return ResponseEntity.ok().body(volatilityDto);
    }
    @GetMapping("/market-summary")
    public ResponseEntity<MarketSummaryDto> getMarketSummary(){
        MarketSummaryDto summary = marketService.getMarketSummary();
        return ResponseEntity.ok().body(summary);
    }
}
