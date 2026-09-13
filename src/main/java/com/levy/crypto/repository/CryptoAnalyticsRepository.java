package com.levy.crypto.repository;

import com.levy.crypto.model.CryptoAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CryptoAnalyticsRepository
        extends JpaRepository<CryptoAnalytics, Long> {

    Optional<CryptoAnalytics> findBySymbol(String symbol);
}