package com.example.berijalanassesment.repository;

import com.example.berijalanassesment.models.ReportExport;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportExportRepository extends JpaRepository<ReportExport, UUID> {

    Optional<ReportExport> findTopByDailyReportDailyReportIdAndFormatOrderByCreatedAtDesc(UUID dailyReportId, String format);
}
