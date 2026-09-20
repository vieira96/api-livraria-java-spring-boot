package com.vieira96.libraryapi.controller.auth;

import com.vieira96.libraryapi.dto.auth.LoginUserDTO;
import com.vieira96.libraryapi.dto.auth.RegisterUserDTO;
import com.vieira96.libraryapi.service.auth.RefreshTokenCookieService;
import com.vieira96.libraryapi.service.auth.AuthService;
import com.vieira96.libraryapi.support.IntegrationTestContainer;
import com.vieira96.libraryapi.repository.auth.RefreshTokenRepository;
import com.vieira96.libraryapi.repository.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerTest extends IntegrationTestContainer {

    @LocalServerPort
    private int port;

    @Autowired
    private AuthService authService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    private UUID createdUserId;

    @AfterEach
    void cleanUpCreatedUser() {
        if (createdUserId != null) {
            refreshTokenRepository.deleteAllByUser_Id(createdUserId);
            userRepository.deleteById(createdUserId);
        }
    }

    @Test
    void shouldRejectMeWithoutAccessToken() throws Exception {
        HttpResponse<String> response = getMe(null);

        assertThat(response.statusCode()).isEqualTo(401);
    }

    @Test
    void shouldReturnAuthenticatedUserFromAccessToken() throws Exception {
        String email = "me." + UUID.randomUUID() + "@example.com";
        var registeredUser = authService.register(new RegisterUserDTO(
                "Maria Silva",
                email,
                "strong-password",
                "strong-password"
        ));
        createdUserId = registeredUser.getId();
        var login = authService.login(new LoginUserDTO(email, "strong-password"), "127.0.0.1");

        HttpResponse<String> response = getMe(login.response().accessToken());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body())
                .contains("\"id\":\"" + registeredUser.getId() + "\"")
                .contains("\"name\":\"Maria Silva\"")
                .contains("\"email\":\"" + email + "\"")
                .contains("\"roles\":[\"USER\"]")
                .doesNotContain("password");
    }

    @Test
    void shouldSetSecureRefreshTokenCookieOnLogin() throws Exception {
        HttpResponse<String> response = login(registerTestUser());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body())
                .contains("\"accessToken\"")
                .doesNotContain("refreshToken");
        assertThat(refreshCookie(response))
                .contains(RefreshTokenCookieService.COOKIE_NAME + "=")
                .contains("Path=/")
                .contains("Secure")
                .contains("HttpOnly")
                .contains("SameSite=Strict");
    }

    @Test
    void shouldRotateRefreshTokenCookie() throws Exception {
        HttpResponse<String> loginResponse = login(registerTestUser());
        String oldRefreshToken = refreshToken(loginResponse);

        HttpResponse<String> refreshResponse = postWithRefreshCookie("/api/auth/refresh", oldRefreshToken);
        String newRefreshToken = refreshToken(refreshResponse);

        assertThat(refreshResponse.statusCode()).isEqualTo(200);
        assertThat(refreshResponse.body())
                .contains("\"accessToken\"")
                .doesNotContain("refreshToken");
        assertThat(newRefreshToken).isNotEqualTo(oldRefreshToken);
        assertThat(refreshCookie(refreshResponse))
                .contains("Secure")
                .contains("HttpOnly")
                .contains("SameSite=Strict");
    }

    @Test
    void shouldRejectReusedRefreshToken() throws Exception {
        HttpResponse<String> loginResponse = login(registerTestUser());
        String refreshToken = refreshToken(loginResponse);

        HttpResponse<String> firstRefreshResponse = postWithRefreshCookie("/api/auth/refresh", refreshToken);
        HttpResponse<String> reusedRefreshResponse = postWithRefreshCookie("/api/auth/refresh", refreshToken);

        assertThat(firstRefreshResponse.statusCode()).isEqualTo(200);
        assertThat(reusedRefreshResponse.statusCode()).isEqualTo(401);
    }

    @Test
    void shouldRevokeSessionAndClearRefreshTokenCookieOnLogout() throws Exception {
        HttpResponse<String> loginResponse = login(registerTestUser());
        String refreshToken = refreshToken(loginResponse);

        HttpResponse<String> logoutResponse = postWithRefreshCookie("/api/auth/logout", refreshToken);

        assertThat(logoutResponse.statusCode()).isEqualTo(204);
        assertThat(refreshCookie(logoutResponse))
                .contains(RefreshTokenCookieService.COOKIE_NAME + "=")
                .contains("Path=/")
                .contains("Max-Age=0")
                .contains("Secure")
                .contains("HttpOnly")
                .contains("SameSite=Strict");
    }

    private String registerTestUser() {
        String email = "auth-controller." + UUID.randomUUID() + "@example.com";
        var registeredUser = authService.register(new RegisterUserDTO(
                "Maria Silva",
                email,
                "strong-password",
                "strong-password"
        ));
        createdUserId = registeredUser.getId();
        return email;
    }

    private HttpResponse<String> login(String email) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"email\":\"" + email + "\",\"password\":\"strong-password\"}"))
                .build();

        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> postWithRefreshCookie(String path, String refreshToken) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .header("Cookie", RefreshTokenCookieService.COOKIE_NAME + "=" + refreshToken)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String refreshCookie(HttpResponse<String> response) {
        return response.headers()
                .firstValue("Set-Cookie")
                .orElseThrow(() -> new AssertionError("Cabeçalho Set-Cookie não encontrado."));
    }

    private String refreshToken(HttpResponse<String> response) {
        String cookie = refreshCookie(response);
        String prefix = RefreshTokenCookieService.COOKIE_NAME + "=";
        int start = cookie.indexOf(prefix);
        int end = cookie.indexOf(';', start);

        if (start < 0 || end < 0) {
            throw new AssertionError("Cookie de refresh inválido: " + cookie);
        }

        return cookie.substring(start + prefix.length(), end);
    }

    private HttpResponse<String> getMe(String accessToken) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/auth/me"))
                .GET();
        if (accessToken != null) {
            request.header("Authorization", "Bearer " + accessToken);
        }

        return HttpClient.newHttpClient().send(request.build(), HttpResponse.BodyHandlers.ofString());
    }
}
