package com.levy.crypto.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.levy.crypto.dto.BinanceTickerDto;
import com.levy.crypto.event.CryptoPriceUpdate;
import com.levy.crypto.model.MarketTicker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class BinanceService {
    private final RestClient restClient;
    private static final Logger log =
            LoggerFactory.getLogger(BinanceService.class);

    private final KafkaProducerService kafkaProducerService;

    public BinanceService(
            RestClient restClient,
            KafkaProducerService kafkaProducerService) {

        this.restClient = restClient;
        this.kafkaProducerService = kafkaProducerService;
    }

    public List<BinanceTickerDto> fetchData() {
        try {
            List<BinanceTickerDto> dtoList = restClient.get()
                    .uri("/api/v3/ticker/24hr")
                    .retrieve()
                    .body(
                            new ParameterizedTypeReference<
                                                                List<BinanceTickerDto>
                                                                >() {}
                    );

            return dtoList != null ? dtoList : List.of();

        } catch (Exception e) {
            log.error("Error fetching Binance data", e);
            return List.of();
        }
    }

}