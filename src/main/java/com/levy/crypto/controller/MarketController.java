package com.levy.crypto.controller;

import com.levy.crypto.model.MarketTicker;
import com.levy.crypto.service.MarketService;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

public class MarketController {

    private final MarketService marketService;
    public MarketController(MarketService marketService){
        this.marketService = marketService;
    }
//    @GetMapping("/top-performers")
//    public List<MarketTicker> getTopPerformers() {
//        return marketService.getTopPerformers();
//    }
}
