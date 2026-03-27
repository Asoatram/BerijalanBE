package com.example.berijalanassesment.repository;

import com.example.berijalanassesment.models.RiskAlert;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RiskAlertRepository extends JpaRepository<RiskAlert, UUID> {

    Page<RiskAlert> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    List<RiskAlert> findTop5ByStatusOrderByCreatedAtDesc(String status);

    long countByStatusAndSeverity(String status, String severity);

    boolean existsBySessionSessionIdAndStatusAndSeverity(UUID sessionId, String status, String severity);

    boolean existsByVisitorVisitorIdAndStatusAndSeverity(UUID visitorId, String status, String severity);

    List<RiskAlert> findByCreatedAtBetween(Instant start, Instant end);
}
