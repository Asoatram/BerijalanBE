package com.example.berijalanassesment.repository;

import com.example.berijalanassesment.models.DailyReportSummary;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyReportSummaryRepository extends JpaRepository<DailyReportSummary, UUID> {

    Optional<DailyReportSummary> findByDailyReportDailyReportId(UUID dailyReportId);
}
