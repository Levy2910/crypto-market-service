package com.levy.crypto.repository;


import com.levy.crypto.model.MarketTicker;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MarketTickerRepository
        extends JpaRepository<MarketTicker, Long> {
    Page<MarketTicker> findBySymbol(String symbol, Pageable pageable);
    List<MarketTicker> findBySymbol(String symbol);
    @Query(value = "SELECT DISTINCT symbol FROM market_ticker", nativeQuery = true)
    List<String> findDistinctSymbols();
}