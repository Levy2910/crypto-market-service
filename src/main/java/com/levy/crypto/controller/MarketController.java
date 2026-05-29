package com.levy.crypto.controller;

import com.levy.crypto.model.MarketTicker;
import com.levy.crypto.service.MarketService;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public MarketTicker getCoin(@PathVariable String symbol){
        return marketService.getCoin(symbol);
    }
    @GetMapping("/history/{symbol}")
    public ResponseEntity<List<MarketTicker>> getHistory(@PathVariable String symbol){
        List<MarketTicker> marketTickerList = marketService.getHistoryBySymbol(symbol);
        if(marketTickerList  == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(marketTickerList);
    }
    @GetMapping("/metrics/{symbol}")
    public ResponseEntity<Map<String, Object>> getAveragePriceChange(@PathVariable String symbol){
        double averageAfter1Min = marketService.getAverage(symbol, 60*1000);
        double averageAfter5Min = marketService.getAverage(symbol, 5*60*1000);
        Map<String, Object> map = new HashMap<>();
        map.put("status", "success");
        map.put("code", 200);
        map.put("symbol", symbol);
        map.put("movingAverage1Min", averageAfter1Min);
        map.put("movingAverage5Min", averageAfter5Min);
        return ResponseEntity.ok().body(map);
    }
    @GetMapping("/volatility/{symbol}")
    public ResponseEntity<Map<String, Object>> getVolatility(@PathVariable String symbol){
        double volatilityAfter1Min = marketService.getVolatility(symbol, 60*1000);
        double volatilityAfter5Min = marketService.getVolatility(symbol, 5*60*1000);
        Map<String, Object> map = new HashMap<>();
        map.put("status", "success");
        map.put("code", 200);
        map.put("symbol", symbol);
        map.put("volatility1Min", volatilityAfter1Min);
        map.put("volatility5Min", volatilityAfter5Min);
        return ResponseEntity.ok().body(map);
    }
}
