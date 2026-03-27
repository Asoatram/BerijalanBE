package com.example.berijalanassesment.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "devices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device {

    @Id
    @Column(name = "device_id", nullable = false, updatable = false)
    private String deviceId;

    @Column(name = "device_type", nullable = false)
    private String deviceType;

    @Column(name = "location_label")
    private String locationLabel;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "last_seen_at")
    private Instant lastSeenAt;
}
