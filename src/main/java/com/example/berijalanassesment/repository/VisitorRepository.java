package com.example.berijalanassesment.repository;

import com.example.berijalanassesment.models.Visitor;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VisitorRepository extends JpaRepository<Visitor, UUID> {

    Optional<Visitor> findByNik(String nik);

    boolean existsByNik(String nik);

    @Query("""
        SELECT v
        FROM Visitor v
        WHERE (:query IS NULL OR :query = ''
            OR LOWER(v.fullName) LIKE LOWER(CONCAT('%', :query, '%'))
            OR v.nik LIKE CONCAT('%', :query, '%')
            OR LOWER(COALESCE(v.email, '')) LIKE LOWER(CONCAT('%', :query, '%')))
        """)
    Page<Visitor> search(@Param("query") String query, Pageable pageable);
}
