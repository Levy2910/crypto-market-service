package com.levy.crypto.service.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.levy.crypto.event.CryptoPriceUpdate;
import com.levy.crypto.model.CryptoAnalytics;
import com.levy.crypto.model.ProcessedEvent;
import com.levy.crypto.repository.CryptoAnalyticsRepository;
import com.levy.crypto.repository.ProcessedEventRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AnalyticsConsumerService {

    private final ObjectMapper objectMapper;
    private final CryptoAnalyticsRepository cryptoAnalyticsRepository;
    private final ProcessedEventRepository processedEventRepository;

    public AnalyticsConsumerService(
            ObjectMapper objectMapper,
            CryptoAnalyticsRepository cryptoAnalyticsRepository, ProcessedEventRepository processedEventRepository) {

        this.objectMapper = objectMapper;
        this.cryptoAnalyticsRepository = cryptoAnalyticsRepository;
        this.processedEventRepository = processedEventRepository;
    }

    @Transactional
    @KafkaListener(
            topics = "price-updates",
            groupId = "analytics-group"
    )
    public void consume(String message) throws JsonProcessingException {

        CryptoPriceUpdate update =
                objectMapper.readValue(message, CryptoPriceUpdate.class);

        // check if it already exists
        if (isUpdateProcessed(update)){
            return;
        }


        Optional<CryptoAnalytics> cryptoAnalytics =
                cryptoAnalyticsRepository.findBySymbol(update.symbol());

        if (cryptoAnalytics.isEmpty()) {

            CryptoAnalytics analytics = new CryptoAnalytics();

            analytics.setSymbol(update.symbol());
            analytics.setCurrentPrice(update.price());
            analytics.setAveragePrice(update.price());
            analytics.setHighestPrice(update.price());
            analytics.setLowestPrice(update.price());
            analytics.setLastUpdated(LocalDateTime.now());
            analytics.setEventCount(1);

            cryptoAnalyticsRepository.save(analytics);
            markAsProcessed(update);
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
        curr.setLastUpdated(LocalDateTime.now());

        cryptoAnalyticsRepository.save(curr);

        // save processed event
        markAsProcessed(update);
    }

    private void markAsProcessed(CryptoPriceUpdate update) {
        ProcessedEvent processedEvent = new ProcessedEvent();
        processedEvent.setConsumer("analytics-group");
        processedEvent.setEventId(update.eventId());
        processedEvent.setProcessedAt(LocalDateTime.now());
        processedEventRepository.save(processedEvent);
    }

    private boolean isUpdateProcessed(CryptoPriceUpdate update) {
        String eventId = update.eventId();
        String consumerId = "analytics-group";
        return processedEventRepository.existsByEventIdAndConsumer(eventId, consumerId);
    }

    public CryptoAnalytics getCryptoAnalytics(String symbol) {

        return cryptoAnalyticsRepository
                .findBySymbol(symbol.toUpperCase())
                .orElse(null);
    }
}