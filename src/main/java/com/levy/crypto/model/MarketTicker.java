package com.levy.crypto.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Data
@NoArgsConstructor
@Getter
@Setter
@Entity
public class MarketTicker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol;
    private double price;
    private double changePercent;
    private double volume;
    private long openTime;
    private long timestamp;
    public MarketTicker(String symbol, double price, double changePercent, double volume, long openTime, long timestamp){
        this.symbol = symbol;
        this.price = price;
        this.changePercent = changePercent;
        this.volume = volume;
        this.openTime = openTime;
        this.timestamp = timestamp;
    }
}
