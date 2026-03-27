package com.example.berijalanassesment.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "host_contacts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HostContact {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "host_contact_id", nullable = false, updatable = false)
    private UUID hostContactId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "department")
    private String department;

    @Column(name = "floor_label")
    private String floorLabel;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
