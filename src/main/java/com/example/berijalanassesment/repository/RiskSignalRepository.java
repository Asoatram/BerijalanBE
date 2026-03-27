package com.example.berijalanassesment.repository;

import com.example.berijalanassesment.models.RiskSignal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RiskSignalRepository extends JpaRepository<RiskSignal, UUID> {

    List<RiskSignal> findByRiskAnalysisRiskAnalysisId(UUID riskAnalysisId);

    void deleteByRiskAnalysisRiskAnalysisId(UUID riskAnalysisId);
}
