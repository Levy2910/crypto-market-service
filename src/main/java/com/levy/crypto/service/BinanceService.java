package com.levy.crypto.service;

import com.levy.crypto.config.RestClientConfig;
import com.levy.crypto.dto.BinanceTickerDto;
import com.levy.crypto.model.MarketTicker;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class BinanceService {
    private final RestClient restClient;

    public BinanceService(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<MarketTicker> fetchData() {
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
    }
}