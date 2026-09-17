package com.levy.crypto.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.levy.crypto.event.CryptoPriceUpdate;
import com.levy.crypto.model.MarketTicker;
import com.levy.crypto.repository.MarketTickerRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class MarketConsumerService {
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final MarketTickerRepository marketTickerRepository;
    private final WebSocketService webSocketService;

    public MarketConsumerService(ObjectMapper objectMapper, StringRedisTemplate stringRedisTemplate, MarketTickerRepository marketTickerRepository, WebSocketService webSocketService) {
        this.objectMapper = objectMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.marketTickerRepository = marketTickerRepository;
        this.webSocketService = webSocketService;
    }

    @KafkaListener(
            topics = "price-updates",
            groupId = "crypto-market-group"
    )
    public void consume(String message) throws JsonProcessingException {

        CryptoPriceUpdate update =
                objectMapper.readValue(message, CryptoPriceUpdate.class);

        webSocketService.sendPriceUpdate(
                update.symbol() + " price: " + update.price()
        );

        stringRedisTemplate.opsForValue().set(
                update.symbol(),
                String.valueOf(update.price()),
                Duration.ofSeconds(30)
        );
        MarketTicker marketTicker = new MarketTicker(update.symbol(), update.price(), update.changePercent(), update.volume(), update.openTime());
        marketTickerRepository.save(marketTicker);
    }
}