package com.example.berijalanassesment.repository;

import com.example.berijalanassesment.models.AccessKey;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessKeyRepository extends JpaRepository<AccessKey, UUID> {

    List<AccessKey> findBySessionSessionIdAndStatus(UUID sessionId, String status);
}
