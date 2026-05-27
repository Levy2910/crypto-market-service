package com.levy.crypto.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MarketTicker {
    private String symbol;
    private double price;
    private double changePercent;
    private double volume;
    private long openTime;
    private long timestamp;
}
