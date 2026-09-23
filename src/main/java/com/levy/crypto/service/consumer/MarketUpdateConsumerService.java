package com.levy.crypto.service.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.levy.crypto.event.CryptoPriceUpdate;
import com.levy.crypto.model.MarketTicker;
import com.levy.crypto.repository.MarketTickerRepository;
import com.levy.crypto.service.market.LatestMarketState;
import com.levy.crypto.service.websocket.WebSocketService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class MarketUpdateConsumerService {

    private final ObjectMapper objectMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final MarketTickerRepository marketTickerRepository;
    private final WebSocketService webSocketService;
    private final LatestMarketState latestMarketState;

    public MarketUpdateConsumerService(
            ObjectMapper objectMapper,
            StringRedisTemplate stringRedisTemplate,
            MarketTickerRepository marketTickerRepository,
            WebSocketService webSocketService,
            LatestMarketState latestMarketState) {

        this.objectMapper = objectMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        this.marketTickerRepository = marketTickerRepository;
        this.webSocketService = webSocketService;
        this.latestMarketState = latestMarketState;
    }

    @KafkaListener(
            topics = "price-updates",
            groupId = "crypto-market-group"
    )
    public void consume(String message)
            throws JsonProcessingException {

        CryptoPriceUpdate update =
                objectMapper.readValue(
                        message,
                        CryptoPriceUpdate.class
                );

        MarketTicker marketTicker =
                new MarketTicker(
                        update.symbol(),
                        update.price(),
                        update.changePercent(),
                        update.volume(),
                        update.openTime()
                );

        latestMarketState.addTicker(marketTicker);

        webSocketService.sendPriceUpdate(
                update.symbol()
                        + " price: "
                        + update.price()
        );

        stringRedisTemplate.opsForValue().set(
                update.symbol(),
                String.valueOf(update.price()),
                Duration.ofSeconds(30)
        );

        marketTickerRepository.save(marketTicker);
    }
}