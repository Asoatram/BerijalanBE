package com.example.berijalanassesment.repository;

import com.example.berijalanassesment.models.Device;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<Device, String> {

    List<Device> findByStatus(String status);
}
