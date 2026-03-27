package com.example.berijalanassesment.repository;

import com.example.berijalanassesment.models.DailyReportDataSource;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyReportDataSourceRepository extends JpaRepository<DailyReportDataSource, UUID> {

    List<DailyReportDataSource> findByDailyReportDailyReportIdOrderBySortOrderAsc(UUID dailyReportId);
}
