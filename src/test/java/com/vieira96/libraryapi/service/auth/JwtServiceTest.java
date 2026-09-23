package com.vieira96.libraryapi.service.auth;

import com.vieira96.libraryapi.model.role.RoleModel;
import com.vieira96.libraryapi.model.role.RoleName;
import com.vieira96.libraryapi.model.user.UserModel;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    @Test
    void shouldGenerateJwtWithUserClaims() {
        JwtEncoder encoder = parameters -> new Jwt(
                "encoded-token", Instant.now(), Instant.now().plus(Duration.ofHours(1)),
                Map.of("alg", "RS256"), parameters.getClaims().getClaims());
        JwtService jwtService = new JwtService(encoder, Duration.ofHours(1), "library-api", "library-services", "key-1");
        UserModel user = new UserModel();
        user.setId(UUID.randomUUID());
        RoleModel role = new RoleModel();
        role.setName(RoleName.USER);
        user.getRoles().add(role);

        assertThat(jwtService.generateToken(user)).isEqualTo("encoded-token");
        assertThat(jwtService.getExpirationSeconds()).isEqualTo(3600L);
    }

    @Test
    void shouldRejectNonPositiveExpiration() {
        JwtEncoder encoder = parameters -> null;
        assertThatThrownBy(() -> new JwtService(encoder, Duration.ZERO, "library-api", "library-services", "key-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("JWT_EXPIRATION deve ser maior que zero.");
    }
}
