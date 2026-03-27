package com.example.berijalanassesment.repository;

import com.example.berijalanassesment.models.RiskAnalysis;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RiskAnalysisRepository extends JpaRepository<RiskAnalysis, UUID> {

    Optional<RiskAnalysis> findBySessionSessionId(UUID sessionId);

    @Query("""
        SELECT COUNT(ra)
        FROM RiskAnalysis ra
        JOIN ra.session s
        WHERE ra.riskLevel = :riskLevel AND s.status = :sessionStatus
        """)
    long countByRiskLevelAndSessionStatus(String riskLevel, String sessionStatus);
}
