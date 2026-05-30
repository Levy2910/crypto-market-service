package com.levy.crypto.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VolatilityDto {
    private String symbol;
    private double volatility1Min;
    private double volatility5Min;
}
