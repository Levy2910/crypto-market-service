package com.levy.crypto.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MarketSentimentDto {

    private double bullishPercentage;
    private double bearishPercentage;
    private double neutralPercentage;
    private int totalCoins;
}
