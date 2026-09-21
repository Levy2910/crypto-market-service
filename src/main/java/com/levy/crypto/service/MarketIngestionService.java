package com.levy.crypto.service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.levy.crypto.dto.BinanceTickerDto;
import com.levy.crypto.event.CryptoPriceUpdate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MarketIngestionService {

    private final BinanceService binanceService;
    private final KafkaProducerService kafkaProducerService;

    public MarketIngestionService(
            BinanceService binanceService,
            KafkaProducerService kafkaProducerService) {

        this.binanceService = binanceService;
        this.kafkaProducerService = kafkaProducerService;
    }

    @Scheduled(fixedRate = 5000)
    public void publishMarketUpdates() throws JsonProcessingException {

        // testing with 1 ticker first
        List<BinanceTickerDto> data = binanceService.fetchData();

        if (!data.isEmpty()) {
            BinanceTickerDto dto = data.get(0);

            CryptoPriceUpdate event = new CryptoPriceUpdate(
                    dto.getSymbol(),
                    Double.parseDouble(dto.getLastPrice()),
                    Double.parseDouble(dto.getPriceChangePercent()),
                    Double.parseDouble(dto.getVolume()),
                    dto.getOpenTime()
            );

            kafkaProducerService.sendPriceUpdate(event);
        }

//        List<BinanceTickerDto> data =
//                binanceService.fetchData();
//
//        for (BinanceTickerDto dto : data) {
//
//            CryptoPriceUpdate event = new CryptoPriceUpdate(
//                    dto.getSymbol(),
//                    Double.parseDouble(dto.getLastPrice()),
//                    Double.parseDouble(dto.getPriceChangePercent()),
//                    Double.parseDouble(dto.getVolume()),
//                    dto.getOpenTime()
//            );
//
//            kafkaProducerService.sendPriceUpdate(event);
//        }

    }
}