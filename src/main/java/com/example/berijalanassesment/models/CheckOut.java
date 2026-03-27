package com.example.berijalanassesment.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "checkouts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckOut {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "checkout_id", nullable = false, updatable = false)
    private UUID checkoutId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false, unique = true)
    private VisitSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by_user_id")
    private User performedByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by_device_id")
    private Device performedByDevice;

    @Column(name = "reason_code")
    private String reasonCode;

    @Column(name = "reason_note")
    private String reasonNote;

    @Column(name = "is_forced", nullable = false)
    private Boolean isForced;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "checkout_at", nullable = false)
    private Instant checkoutAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
