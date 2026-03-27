package com.example.berijalanassesment.repository;

import com.example.berijalanassesment.models.RiskAlertAction;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RiskAlertActionRepository extends JpaRepository<RiskAlertAction, UUID> {

    List<RiskAlertAction> findByAlertAlertIdOrderByActedAtDesc(UUID alertId);
}
