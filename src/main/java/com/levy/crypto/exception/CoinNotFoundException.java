package com.levy.crypto.exception;


public class CoinNotFoundException extends RuntimeException {
    public CoinNotFoundException(String symbol) {
        super("Coin not found: " + symbol);
    }
}
