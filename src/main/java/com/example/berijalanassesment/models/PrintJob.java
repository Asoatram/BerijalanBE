package com.example.berijalanassesment.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "print_jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrintJob {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "job_id", nullable = false, updatable = false)
    private UUID jobId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private VisitSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pass_id")
    private DigitalPass pass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by_user_id")
    private User requestedByUser;

    @Column(name = "printer_id", nullable = false)
    private String printerId;

    @Column(name = "copies", nullable = false)
    private Integer copies;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "queued_at", nullable = false)
    private Instant queuedAt;

    @Column(name = "completed_at")
    private Instant completedAt;
}
