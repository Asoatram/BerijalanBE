package com.example.berijalanassesment.repository;

import com.example.berijalanassesment.models.SessionFlag;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionFlagRepository extends JpaRepository<SessionFlag, UUID> {

    List<SessionFlag> findBySessionSessionIdOrderByCreatedAtDesc(UUID sessionId);

    boolean existsBySessionSessionIdAndStatus(UUID sessionId, String status);
}
