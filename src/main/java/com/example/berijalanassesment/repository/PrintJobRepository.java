package com.example.berijalanassesment.repository;

import com.example.berijalanassesment.models.PrintJob;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrintJobRepository extends JpaRepository<PrintJob, UUID> {

    List<PrintJob> findBySessionSessionIdOrderByQueuedAtDesc(UUID sessionId);

    long countByStatus(String status);
}
