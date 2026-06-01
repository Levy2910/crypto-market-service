package com.levy.crypto.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MarketTickerMover {
    private String symbol;
    private int currentRank;
    private int previousRank;
    private int rankJumped;
}
