package com.levy.crypto.event;

public record CryptoPriceUpdate(
        String symbol,
        double price,
        double changePercent,
        double volume,
        long openTime
) {}