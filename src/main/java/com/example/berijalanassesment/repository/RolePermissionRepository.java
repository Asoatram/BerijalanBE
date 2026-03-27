package com.example.berijalanassesment.repository;

import com.example.berijalanassesment.models.RolePermission;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolePermissionRepository extends JpaRepository<RolePermission, UUID> {

    List<RolePermission> findByRoleRoleId(UUID roleId);
}
