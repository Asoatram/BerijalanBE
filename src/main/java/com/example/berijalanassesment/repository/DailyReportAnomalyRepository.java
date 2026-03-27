package com.example.berijalanassesment.repository;

import com.example.berijalanassesment.models.DailyReportAnomaly;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyReportAnomalyRepository extends JpaRepository<DailyReportAnomaly, UUID> {

    List<DailyReportAnomaly> findByDailyReportDailyReportIdOrderBySortOrderAsc(UUID dailyReportId);

    long deleteByDailyReportDailyReportId(UUID dailyReportId);
}
