package com.example.berijalanassesment.repository;

import com.example.berijalanassesment.models.AuthRefreshToken;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthRefreshTokenRepository extends JpaRepository<AuthRefreshToken, UUID> {

    Optional<AuthRefreshToken> findByTokenHashAndRevokedAtIsNull(String tokenHash);

    List<AuthRefreshToken> findByUserUserIdAndRevokedAtIsNull(UUID userId);

    long deleteByExpiresAtBefore(Instant cutoff);
}
