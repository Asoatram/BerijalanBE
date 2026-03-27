package com.example.berijalanassesment.repository;

import com.example.berijalanassesment.models.HostContact;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HostContactRepository extends JpaRepository<HostContact, UUID> {

    List<HostContact> findByIsActiveTrue();

    Optional<HostContact> findByEmailIgnoreCase(String email);
}
