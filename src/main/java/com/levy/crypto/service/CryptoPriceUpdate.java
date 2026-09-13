package com.levy.crypto.service;

public record CryptoPriceUpdate(
        String symbol,
        double price,
        double changePercent,
        double volume,
        long openTime
) {}