package com.example.berijalanassesment.repository;

import com.example.berijalanassesment.models.SessionReport;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionReportRepository extends JpaRepository<SessionReport, UUID> {

    Optional<SessionReport> findTopBySessionSessionIdAndFormatOrderByGeneratedAtDesc(UUID sessionId, String format);
}
