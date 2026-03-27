package com.example.berijalanassesment.repository;

import com.example.berijalanassesment.models.DailyReport;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyReportRepository extends JpaRepository<DailyReport, UUID> {

    Optional<DailyReport> findByReportDateAndTimezone(LocalDate reportDate, String timezone);

    List<DailyReport> findByReportDateBetween(LocalDate startDate, LocalDate endDate);
}
