package com.levy.crypto.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
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

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime timestamp;

    public MarketTicker(String symbol,
                        double price,
                        double changePercent,
                        double volume,
                        long openTime) {
        this.symbol = symbol;
        this.price = price;
        this.changePercent = changePercent;
        this.volume = volume;
        this.openTime = openTime;
    }
}