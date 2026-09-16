package com.cursojava.libraryapi.controller.auth;

import com.cursojava.libraryapi.dto.auth.LoginUserDTO;
import com.cursojava.libraryapi.dto.auth.RegisterUserDTO;
import com.cursojava.libraryapi.service.auth.AuthService;
import com.cursojava.libraryapi.support.IntegrationTestContainer;
import com.cursojava.libraryapi.repository.auth.RefreshTokenRepository;
import com.cursojava.libraryapi.repository.user.UserRepository;
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
                "strong-password"
        ));
        createdUserId = registeredUser.getId();
        var login = authService.login(new LoginUserDTO(email, "strong-password"), "127.0.0.1");

        HttpResponse<String> response = getMe(login.accessToken());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body())
                .contains("\"id\":\"" + registeredUser.getId() + "\"")
                .contains("\"name\":\"Maria Silva\"")
                .contains("\"email\":\"" + email + "\"")
                .contains("\"roles\":[\"USER\"]")
                .doesNotContain("password");
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
