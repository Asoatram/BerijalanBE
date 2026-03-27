package com.example.berijalanassesment.security;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final SecurityJwtProperties properties;

    public JwtService(
        JwtEncoder jwtEncoder,
        JwtDecoder jwtDecoder,
        SecurityJwtProperties properties
    ) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.properties = properties;
    }

    public String issueAccessToken(
        UUID userId,
        String email,
        String roleCode,
        List<String> permissions
    ) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(properties.getIssuer())
            .issuedAt(now)
            .expiresAt(now.plusSeconds(properties.getAccessTtlSeconds()))
            .subject(userId.toString())
            .claim("email", email)
            .claim("role", roleCode)
            .claim("permissions", permissions)
            .build();

        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    public Jwt decodeAccessToken(String token) {
        return jwtDecoder.decode(token);
    }
}
