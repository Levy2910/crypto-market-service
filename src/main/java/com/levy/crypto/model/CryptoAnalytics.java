package com.levy.crypto.model;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class CryptoAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String symbol;

    private double currentPrice;
    private double averagePrice;
    private double highestPrice;
    private double lowestPrice;
    private long eventCount;
}