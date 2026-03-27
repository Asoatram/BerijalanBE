package com.example.berijalanassesment.repository;

import com.example.berijalanassesment.models.PortraitMedia;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortraitMediaRepository extends JpaRepository<PortraitMedia, UUID> {

    Optional<PortraitMedia> findByPhotoIdAndProcessingStatus(UUID photoId, String processingStatus);

    List<PortraitMedia> findByCapturedDeviceDeviceIdAndCreatedAtBetween(String deviceId, Instant start, Instant end);
}
