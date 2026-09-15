package com.levy.crypto.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.levy.crypto.model.CryptoAnalytics;
import com.levy.crypto.model.MarketTicker;
import com.levy.crypto.repository.CryptoAnalyticsRepository;
import com.levy.crypto.repository.MarketTickerRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;

import java.time.Duration;
import java.util.Optional;

public class BigChangeAlertService {
    private final ObjectMapper objectMapper;
    private final CryptoAnalyticsRepository cryptoAnalyticsRepository;

    public BigChangeAlertService(ObjectMapper objectMapper, CryptoAnalyticsRepository cryptoAnalyticsRepository) {
        this.objectMapper = objectMapper;
        this.cryptoAnalyticsRepository = cryptoAnalyticsRepository;
    }

    @KafkaListener(
            topics = "price-updates",
            groupId = "alert-group"
    )
    public void consume(String message) throws JsonProcessingException {
        CryptoPriceUpdate update =
                objectMapper.readValue(message, CryptoPriceUpdate.class);

        if (update.changePercent() >= 5 ){
            System.out.println("ALERT MAN, WAKE UP");
        }
    }
}
