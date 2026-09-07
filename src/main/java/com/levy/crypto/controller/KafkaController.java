package com.levy.crypto.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.levy.crypto.service.CryptoPriceUpdate;
import com.levy.crypto.service.KafkaProducerService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kafka")
public class KafkaController {

    private final KafkaProducerService producer;

    public KafkaController(KafkaProducerService producer) {
        this.producer = producer;
    }

    @PostMapping("/price-update")
    public void send(
            @RequestBody CryptoPriceUpdate cryptoPriceUpdate
    ) throws JsonProcessingException {

        producer.sendPriceUpdate(cryptoPriceUpdate);
    }
}