package com.example.berijalanassesment.security;

import com.example.berijalanassesment.models.Permission;
import com.example.berijalanassesment.models.Role;
import com.example.berijalanassesment.models.RolePermission;
import com.example.berijalanassesment.models.User;
import com.example.berijalanassesment.models.UserRole;
import com.example.berijalanassesment.repository.PermissionRepository;
import com.example.berijalanassesment.repository.RolePermissionRepository;
import com.example.berijalanassesment.repository.RoleRepository;
import com.example.berijalanassesment.repository.UserRepository;
import com.example.berijalanassesment.repository.UserRoleRepository;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(0)
public class AuthBootstrapDataLoader implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    public AuthBootstrapDataLoader(
        RoleRepository roleRepository,
        PermissionRepository permissionRepository,
        RolePermissionRepository rolePermissionRepository,
        UserRepository userRepository,
        UserRoleRepository userRoleRepository
    ) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Role adminRole = getOrCreateRole("ADMIN", "Administrator");
        Role securityRole = getOrCreateRole("SECURITY", "Security Operator");

        List<Permission> permissions = AuthPermissions.ALL.stream()
            .map(this::getOrCreatePermission)
            .toList();

        attachPermissions(adminRole, permissions);
        attachPermissions(securityRole, permissions);
        assignAdminRoleToUsersWithoutRole(adminRole);
    }

    private Role getOrCreateRole(String code, String name) {
        return roleRepository.findByCode(code)
            .orElseGet(() -> roleRepository.save(
                Role.builder()
                    .code(code)
                    .name(name)
                    .description(name)
                    .build()
            ));
    }

    private Permission getOrCreatePermission(String code) {
        return permissionRepository.findByCode(code)
            .orElseGet(() -> permissionRepository.save(
                Permission.builder()
                    .code(code)
                    .description(code)
                    .build()
            ));
    }

    private void attachPermissions(Role role, List<Permission> permissions) {
        List<String> existing = rolePermissionRepository.findByRoleRoleId(role.getRoleId()).stream()
            .map(entry -> entry.getPermission().getCode())
            .toList();

        Instant now = Instant.now();
        permissions.stream()
            .filter(permission -> !existing.contains(permission.getCode()))
            .map(permission -> RolePermission.builder()
                .role(role)
                .permission(permission)
                .assignedAt(now)
                .build())
            .forEach(rolePermissionRepository::save);
    }

    private void assignAdminRoleToUsersWithoutRole(Role adminRole) {
        Instant now = Instant.now();
        List<User> users = userRepository.findAll().stream()
            .sorted(Comparator.comparing(User::getCreatedAt))
            .toList();

        users.stream()
            .filter(user -> userRoleRepository.findByUserUserId(user.getUserId()).isEmpty())
            .map(user -> UserRole.builder()
                .user(user)
                .role(adminRole)
                .assignedAt(now)
                .build())
            .forEach(userRoleRepository::save);
    }
}
