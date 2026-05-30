package com.levy.crypto.dto;

import com.levy.crypto.model.MarketTicker;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MarketSummaryDto {
    private int totalPairs;
    private double averageChange;
    private List<MarketTicker> topGainers;
    private List<MarketTicker> topLosers;
    private long lastUpdated;
}
