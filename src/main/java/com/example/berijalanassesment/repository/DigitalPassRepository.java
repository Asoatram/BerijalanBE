package com.example.berijalanassesment.repository;

import com.example.berijalanassesment.models.DigitalPass;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DigitalPassRepository extends JpaRepository<DigitalPass, UUID> {

    Optional<DigitalPass> findBySessionSessionId(UUID sessionId);

    Optional<DigitalPass> findByPassNumber(String passNumber);
}
