package com.example.berijalanassesment.repository;

import com.example.berijalanassesment.models.ReportGenerationJob;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportGenerationJobRepository extends JpaRepository<ReportGenerationJob, UUID> {

    boolean existsByReportDateAndTimezoneAndStatusIn(LocalDate reportDate, String timezone, Collection<String> statuses);

    Optional<ReportGenerationJob> findTopByReportDateAndTimezoneOrderByQueuedAtDesc(LocalDate reportDate, String timezone);

    List<ReportGenerationJob> findByStatusOrderByQueuedAtAsc(String status);
}
