package com.example.berijalanassesment.repository;

import com.example.berijalanassesment.models.CheckOut;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckOutRepository extends JpaRepository<CheckOut, UUID> {

    Optional<CheckOut> findBySessionSessionId(UUID sessionId);

    boolean existsBySessionSessionId(UUID sessionId);
}
