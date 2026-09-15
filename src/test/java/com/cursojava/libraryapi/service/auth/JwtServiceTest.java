package com.cursojava.libraryapi.service.auth;

import com.cursojava.libraryapi.model.role.RoleModel;
import com.cursojava.libraryapi.model.role.RoleName;
import com.cursojava.libraryapi.model.user.UserModel;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "a-test-secret-with-at-least-32-bytes";

    @Test
    void shouldGenerateSignedJwtWithUserAndExpirationClaims() throws Exception {
        JwtService jwtService = new JwtService(SECRET, Duration.ofHours(1), "library-api");
        UserModel user = new UserModel();
        user.setId(UUID.randomUUID());
        RoleModel role = new RoleModel();
        role.setName(RoleName.USER);
        user.getRoles().add(role);

        String token = jwtService.generateToken(user);

        String[] parts = token.split("\\.");
        assertThat(parts).hasSize(3);
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        assertThat(payload).contains("\"sub\":\"" + user.getId() + "\"");
        assertThat(payload).contains("\"roles\":[\"USER\"]");
        assertThat(payload).contains("\"iat\":");
        assertThat(payload).contains("\"exp\":");
        assertThat(jwtService.getExpirationSeconds()).isEqualTo(3600L);

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        String expectedSignature = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(mac.doFinal((parts[0] + "." + parts[1]).getBytes(StandardCharsets.UTF_8)));
        assertThat(parts[2]).isEqualTo(expectedSignature);
    }

    @Test
    void shouldRejectWeakSecret() {
        assertThatThrownBy(() -> new JwtService("short-secret", Duration.ofHours(1), "library-api"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("JWT_SECRET deve possuir pelo menos 32 bytes.");
    }
}
