package com.example.berijalanassesment.repository;

import com.example.berijalanassesment.models.VisitSession;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VisitSessionRepository extends JpaRepository<VisitSession, UUID> {

    Optional<VisitSession> findByVisitorVisitorIdAndStatus(UUID visitorId, String status);

    Optional<VisitSession> findTopByVisitorVisitorIdOrderByCheckinAtDesc(UUID visitorId);

    Optional<VisitSession> findByCheckInCheckinId(UUID checkinId);

    boolean existsByVisitorVisitorIdAndStatus(UUID visitorId, String status);

    Page<VisitSession> findByStatus(String status, Pageable pageable);

    Page<VisitSession> findByStatusAndCheckinAtBetween(String status, Instant start, Instant end, Pageable pageable);

    @Query("""
        SELECT s
        FROM VisitSession s
        JOIN s.visitor v
        LEFT JOIN RiskAnalysis ra ON ra.session = s
        WHERE s.checkinAt = (
            SELECT MAX(s2.checkinAt)
            FROM VisitSession s2
            WHERE s2.visitor.visitorId = v.visitorId
        )
        AND (
            :query IS NULL OR :query = ''
            OR LOWER(v.fullName) LIKE LOWER(CONCAT('%', :query, '%'))
            OR v.nik LIKE CONCAT('%', :query, '%')
            OR LOWER(COALESCE(v.email, '')) LIKE LOWER(CONCAT('%', :query, '%'))
        )
        AND (:status = 'ALL' OR UPPER(s.status) = :status)
        AND (
            :riskLevel = 'ALL'
            OR (
                CASE
                    WHEN UPPER(s.status) = 'ACTIVE' THEN COALESCE(UPPER(ra.riskLevel), 'UNKNOWN')
                    ELSE 'UNKNOWN'
                END
            ) = :riskLevel
        )
        """)
    Page<VisitSession> searchLatestSessions(
        @Param("query") String query,
        @Param("status") String status,
        @Param("riskLevel") String riskLevel,
        Pageable pageable
    );

    @Query("""
        SELECT s
        FROM VisitSession s
        JOIN s.visitor v
        LEFT JOIN RiskAnalysis ra ON ra.session = s
        WHERE s.checkinAt = (
            SELECT MAX(s2.checkinAt)
            FROM VisitSession s2
            WHERE s2.visitor.visitorId = v.visitorId
        )
        AND (
            :query IS NULL OR :query = ''
            OR LOWER(v.fullName) LIKE LOWER(CONCAT('%', :query, '%'))
            OR v.nik LIKE CONCAT('%', :query, '%')
            OR LOWER(COALESCE(v.email, '')) LIKE LOWER(CONCAT('%', :query, '%'))
        )
        AND (:status = 'ALL' OR UPPER(s.status) = :status)
        AND (
            :riskLevel = 'ALL'
            OR (
                CASE
                    WHEN UPPER(s.status) = 'ACTIVE' THEN COALESCE(UPPER(ra.riskLevel), 'UNKNOWN')
                    ELSE 'UNKNOWN'
                END
            ) = :riskLevel
        )
        AND s.checkinAt >= :startAt
        AND s.checkinAt < :endAt
        """)
    Page<VisitSession> searchLatestSessionsInRange(
        @Param("query") String query,
        @Param("status") String status,
        @Param("riskLevel") String riskLevel,
        @Param("startAt") Instant startAt,
        @Param("endAt") Instant endAt,
        Pageable pageable
    );
}
