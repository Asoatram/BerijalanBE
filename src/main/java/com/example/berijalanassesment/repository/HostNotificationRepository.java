package com.example.berijalanassesment.repository;

import com.example.berijalanassesment.models.HostNotification;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HostNotificationRepository extends JpaRepository<HostNotification, UUID> {

    List<HostNotification> findBySessionSessionIdOrderByCreatedAtDesc(UUID sessionId);
}
