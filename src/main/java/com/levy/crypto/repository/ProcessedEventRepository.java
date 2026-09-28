package com.levy.crypto.repository;

import com.levy.crypto.model.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long> {
    boolean existsByEventIdAndConsumer(
            String eventId,
            String consumer
    );
}
