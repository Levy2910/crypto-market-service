package com.levy.crypto.controller;

import com.levy.crypto.model.MarketTicker;
import com.levy.crypto.service.MarketService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
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
    public MarketTicker getCoin(@PathVariable String symbol){
        return marketService.getCoin(symbol);
    }
}
