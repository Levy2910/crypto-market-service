package com.levy.crypto.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    @KafkaListener(topics = "price-updates", groupId = "crypto-market-group")
    public void consume(String message) {
        System.out.println("Received message: " + message);
    }
}