package com.levy.crypto.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class KafkaConsumerService {
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate stringRedisTemplate;

    public KafkaConsumerService(ObjectMapper objectMapper, StringRedisTemplate stringRedisTemplate) {
        this.objectMapper = objectMapper;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @KafkaListener(
            topics = "price-updates",
            groupId = "crypto-market-group"
    )
    public void consume(String message) throws JsonProcessingException {

        CryptoPriceUpdate update =
                objectMapper.readValue(message, CryptoPriceUpdate.class);

        stringRedisTemplate.opsForValue().set(
                update.symbol(),
                update.price(),
                Duration.ofSeconds(30)
        );
    }
}