package com.levy.crypto.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.levy.crypto.event.CryptoPriceUpdate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class BigChangeAlertService {

    private final ObjectMapper objectMapper;

    public BigChangeAlertService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "price-updates",
            groupId = "alert-group"
    )
    public void consume(String message) throws JsonProcessingException {

        CryptoPriceUpdate update =
                objectMapper.readValue(message, CryptoPriceUpdate.class);

        if (Math.abs(update.changePercent()) >= 5) {
            System.out.println(
                    "ALERT: " + update.symbol()
                            + " moved " + update.changePercent() + "%"
            );
        }
    }
}