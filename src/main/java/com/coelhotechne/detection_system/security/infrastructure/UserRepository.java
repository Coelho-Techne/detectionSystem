package com.coelhotechne.detection_system.security.infrastructure;

import com.coelhotechne.detection_system.security.domain.User;
import com.coelhotechne.detection_system.security.domain.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    long countByRoleAndEnableTrue(Role role);
}
