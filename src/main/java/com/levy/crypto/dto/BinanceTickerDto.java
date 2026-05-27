package com.levy.crypto.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BinanceTickerDto {

    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("lastPrice")
    private String lastPrice;

    @JsonProperty("priceChangePercent")
    private String priceChangePercent;

    @JsonProperty("volume")
    private String volume;

    private long openTime;
}

