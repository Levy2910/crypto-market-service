package com.levy.crypto.service;

import com.levy.crypto.dto.BinanceTickerDto;
import com.levy.crypto.model.MarketTicker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class BinanceService {
    private final RestClient restClient;
    private static final Logger log =
            LoggerFactory.getLogger(BinanceService.class);

    public BinanceService(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<MarketTicker> fetchData() {
        try {
            List<BinanceTickerDto> dtoList = restClient.get()
                    .uri("/api/v3/ticker/24hr")
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<List<BinanceTickerDto>>() {});

            if (dtoList == null) {
                return List.of();
            }

            return dtoList.stream()
                    .map(dto -> new MarketTicker(
                            dto.getSymbol(),
                            Double.parseDouble(dto.getLastPrice()),
                            Double.parseDouble(dto.getPriceChangePercent()),
                            Double.parseDouble(dto.getVolume()),
                            dto.getOpenTime(),
                            System.currentTimeMillis()
                    ))
                    .toList();
        }catch (Exception e){
            log.error("Error fetching Binance data", e);
        }
        return List.of();
    }
}