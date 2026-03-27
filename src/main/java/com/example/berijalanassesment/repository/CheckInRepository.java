package com.example.berijalanassesment.repository;

import com.example.berijalanassesment.models.CheckIn;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckInRepository extends JpaRepository<CheckIn, UUID> {

    Optional<CheckIn> findTopByVisitorVisitorIdAndStatusOrderByCheckinAtDesc(UUID visitorId, String status);

    boolean existsByVisitorVisitorIdAndStatus(UUID visitorId, String status);

    Optional<CheckIn> findByPhotoPhotoId(UUID photoId);

    Page<CheckIn> findByStatus(String status, Pageable pageable);

    long countByVisitorVisitorIdAndCheckinAtBetween(UUID visitorId, Instant start, Instant end);

    List<CheckIn> findByCheckinAtBetween(Instant start, Instant end);
}
