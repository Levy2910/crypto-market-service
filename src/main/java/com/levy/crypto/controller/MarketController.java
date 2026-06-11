package com.levy.crypto.controller;

import com.levy.crypto.dto.*;
import com.levy.crypto.exception.CoinNotFoundException;
import com.levy.crypto.model.MarketTicker;
import com.levy.crypto.service.HistoryService;
import com.levy.crypto.service.MarketService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
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
    @Operation(
            description = "Get the top performers, got limit up to 10 performers"
    )
    @GetMapping("/top-performers")
    public List<MarketTicker> getTopPerformers() {
        return marketService.getTopPerformers();
    }
    @Operation(
            description = "Get the bottom performers, got limit up to 10 performers"
    )
    @GetMapping("/bottom-performers")
    public List<MarketTicker> getBottomPerformers() {
        return marketService.getBottomPerformers();
    }
    @Operation(
            description = "Get the top volume performers, got limit up to 10 performers"
    )
    @GetMapping("/top-volumes")
    public List<MarketTicker> getTopVolumes(){
        return marketService.getTopVolumes();
    }
    @Operation(
            description = "Get the coin requested, this request needs a path variable which is a string: /coin/{symbol}"
    )
    @GetMapping("/coin/{symbol}")
    public MarketTicker getCoin(@PathVariable String symbol) {
        if (!marketService.containsCoin(symbol)) {
            throw new CoinNotFoundException(symbol);
        }
        return marketService.getCoin(symbol);
    }
    @Operation(
            description = "Get the history of a symbol, this request needs a symbol as a path variable and the request param which is the page and the size of the page"
    )
    @GetMapping("/history/{symbol}")
    public ResponseEntity<Page<MarketTicker>> getHistory(@PathVariable String symbol,  @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "5") int size) {
        if (!marketService.containsCoin(symbol)) {
            throw new CoinNotFoundException(symbol);
        }
        Page<MarketTicker> marketTickerList = historyService.getHistoryBySymbol(symbol, page, size);
        return ResponseEntity.ok().body(marketTickerList);
    }
    @Operation(
            description = "Get the average of a symbol over the period of 1 minute or 5 minutes"
    )
    @GetMapping("/metrics/{symbol}")
    public ResponseEntity<MetricsDto> getAveragePriceChange(@PathVariable String symbol) {
        if (!marketService.containsCoin(symbol)) {
            throw new CoinNotFoundException(symbol);
        }
        MetricsDto metricsDto = historyService.getMetrics(symbol);
        return ResponseEntity.ok().body(metricsDto);
    }
    @Operation(
            description = "Get the volatility of a symbol over the period of 1 minute or 5 minutes. The volatility is calculated by the ((max price - min price) / min price) * 100  "
    )
    @GetMapping("/volatility/{symbol}")
    public ResponseEntity<VolatilityDto> getVolatility(@PathVariable String symbol) {
        if (!marketService.containsCoin(symbol)) {
            throw new CoinNotFoundException(symbol);
        }
        VolatilityDto volatilityDto = historyService.getVolatilityBySymbol(symbol);
        return ResponseEntity.ok().body(volatilityDto);
    }
    @Operation(
            description = "Get the market summary, total symbols, top performers, bottom performers, average etc"
    )
    @GetMapping("/market-summary")
    public ResponseEntity<MarketSummaryDto> getMarketSummary(){
        MarketSummaryDto summary = marketService.getMarketSummary();
        return ResponseEntity.ok().body(summary);
    }
    @Operation(
            description = "Get the coins that has a significant jump over the period of 5 seconds, the fetch happens every 5 seconds"
    )
    @GetMapping("/biggest-movers")
    public ResponseEntity<List<MarketTickerMover>> getBiggestMovers(){
        List<MarketTickerMover> marketTickerMovers = marketService.getBiggestMovers();
        return ResponseEntity.ok().body(marketTickerMovers);
    }
    @Operation(
            description = "Get the health of the market service, if the service can fetch, it returns healthy, else it is unhealthy indicating failure or down time"
    )
    @GetMapping("/health")
    public ResponseEntity<HealthDto> getHealth(){
        HealthDto healthDto = marketService.getHealth();
        return ResponseEntity.ok().body(healthDto);
    }
    @Operation(
            description = "Get the coins that are the most volatile"
    )
    @GetMapping("/most-volatile")
    public  ResponseEntity<List<VolatilityRankingDto>> getMostVolatility(@RequestParam int min){
        long window = min * 60000L;
        List<VolatilityRankingDto> volatilityRankingDtos = historyService.getMostVolatile(window);
        return ResponseEntity.ok().body(volatilityRankingDtos);
    }
    @Operation(
            description = "Get the market sentiment including the bullish, bearish, neutral, total"
    )
    @GetMapping("/market-sentiment")
    public MarketSentimentDto getMarketSentiment(){
        return marketService.getMarketSentiment();
    }
}
