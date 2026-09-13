package com.levy.crypto.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.levy.crypto.model.CryptoAnalytics;
import com.levy.crypto.repository.CryptoAnalyticsRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AnalyticsConsumerService {

    private final ObjectMapper objectMapper;
    private final CryptoAnalyticsRepository cryptoAnalyticsRepository;

    public AnalyticsConsumerService(
            ObjectMapper objectMapper,
            CryptoAnalyticsRepository cryptoAnalyticsRepository) {

        this.objectMapper = objectMapper;
        this.cryptoAnalyticsRepository = cryptoAnalyticsRepository;
    }

    @KafkaListener(
            topics = "price-updates",
            groupId = "analytics-group"
    )
    public void consume(String message) throws JsonProcessingException {

        CryptoPriceUpdate update =
                objectMapper.readValue(message, CryptoPriceUpdate.class);

        Optional<CryptoAnalytics> cryptoAnalytics =
                cryptoAnalyticsRepository.findBySymbol(update.symbol());

        if (cryptoAnalytics.isEmpty()) {

            CryptoAnalytics analytics = new CryptoAnalytics();

            analytics.setSymbol(update.symbol());
            analytics.setCurrentPrice(update.price());
            analytics.setAveragePrice(update.price());
            analytics.setHighestPrice(update.price());
            analytics.setLowestPrice(update.price());
            analytics.setEventCount(1);

            cryptoAnalyticsRepository.save(analytics);
            return;
        }

        CryptoAnalytics curr = cryptoAnalytics.get();

        double currentPrice = update.price();
        long oldCount = curr.getEventCount();

        double newAverage =
                (curr.getAveragePrice() * oldCount + currentPrice)
                        / (oldCount + 1);

        curr.setCurrentPrice(currentPrice);
        curr.setAveragePrice(newAverage);
        curr.setHighestPrice(
                Math.max(curr.getHighestPrice(), currentPrice)
        );
        curr.setLowestPrice(
                Math.min(curr.getLowestPrice(), currentPrice)
        );
        curr.setEventCount(oldCount + 1);

        cryptoAnalyticsRepository.save(curr);
    }

    public CryptoAnalytics getCryptoAnalytics(String symbol) {

        return cryptoAnalyticsRepository
                .findBySymbol(symbol.toUpperCase())
                .orElse(null);
    }
}