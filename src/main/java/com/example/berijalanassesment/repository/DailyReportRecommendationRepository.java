package com.example.berijalanassesment.repository;

import com.example.berijalanassesment.models.DailyReportRecommendation;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyReportRecommendationRepository extends JpaRepository<DailyReportRecommendation, UUID> {

    List<DailyReportRecommendation> findByDailyReportDailyReportIdOrderBySortOrderAsc(UUID dailyReportId);
}
