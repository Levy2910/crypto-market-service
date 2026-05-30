package com.levy.crypto.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetricsDto {
    private String symbol;
    private double movingAverage1Min;
    private double movingAverage5Min;
}
