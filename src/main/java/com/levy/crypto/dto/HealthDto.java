package com.levy.crypto.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HealthDto {
    private String status;
    private int latestDataSize;
    private long lastUpdated;
    private boolean binanceConnected;
}
