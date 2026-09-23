package com.vieira96.libraryapi.service.auth;

import com.vieira96.libraryapi.model.role.RoleModel;
import com.vieira96.libraryapi.model.user.UserModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final Duration expiration;
    private final String issuer;
    private final String audience;
    private final String keyId;

    public JwtService(
            JwtEncoder jwtEncoder,
            @Value("${security.jwt.access-expiration}") Duration expiration,
            @Value("${security.jwt.issuer}") String issuer,
            @Value("${security.jwt.audience}") String audience,
            @Value("${security.jwt.key-id}") String keyId
    ) {
        if (expiration.isZero() || expiration.isNegative()) {
            throw new IllegalArgumentException("JWT_EXPIRATION deve ser maior que zero.");
        }

        this.jwtEncoder = jwtEncoder;
        this.expiration = expiration;
        this.issuer = issuer;
        this.audience = audience;
        this.keyId = keyId;
    }

    public String generateToken(UserModel user) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(expiration);
        var roles = user.getRoles().stream()
                .map(RoleModel::getName)
                .map(Enum::name)
                .sorted()
                .toList();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(user.getId().toString())
                .audience(List.of(audience))
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .id(UUID.randomUUID().toString())
                .claim("roles", roles)
                .build();
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256)
                .type("JWT")
                .keyId(keyId)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public long getExpirationSeconds() {
        return expiration.toSeconds();
    }

}
