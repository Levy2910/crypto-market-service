package com.levy.crypto.repository;


import com.levy.crypto.model.MarketTicker;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MarketTickerRepository
        extends JpaRepository<MarketTicker, Long> {
    Page<MarketTicker> findBySymbol(String symbol, Pageable pageable);
    List<MarketTicker> findBySymbol(String symbol);
    @Query(value = "SELECT DISTINCT symbol FROM market_ticker", nativeQuery = true)
    List<String> findDistinctSymbols();

    @Query(value = "SELECT AVG(m.price) FROM market_ticker m WHERE m.symbol = :symbol AND m.timestamp >= :cutoff", nativeQuery = true)
    Double getAveragePrice(@Param("symbol") String normalizedSymbol, @Param("cutoff") LocalDateTime cutoff);

    @Query(value = """
    SELECT ((MAX(m.price) - MIN(m.price)) / NULLIF(MIN(m.price), 0)) * 100
    FROM market_ticker m
    WHERE m.symbol = :symbol
    AND m.timestamp >= :cutoff
    """, nativeQuery = true)
    Double getVolatility(
            @Param("symbol") String symbol,
            @Param("cutoff") LocalDateTime cutoff
    );
}