package com.example.berijalanassesment.repository;

import com.example.berijalanassesment.models.VisitIntent;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitIntentRepository extends JpaRepository<VisitIntent, UUID> {

    List<VisitIntent> findByIsActiveTrueOrderBySortOrderAsc();

    Optional<VisitIntent> findByCode(String code);
}
