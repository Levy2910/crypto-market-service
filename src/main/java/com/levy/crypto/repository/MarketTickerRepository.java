package com.levy.crypto.repository;


import com.levy.crypto.model.MarketTicker;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketTickerRepository
        extends JpaRepository<MarketTicker, Long> {
    
}