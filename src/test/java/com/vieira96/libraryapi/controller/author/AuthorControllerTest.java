package com.vieira96.libraryapi.controller.author;

import com.vieira96.libraryapi.dto.auth.LoginUserDTO;
import com.vieira96.libraryapi.dto.auth.RegisterUserDTO;
import com.vieira96.libraryapi.model.role.RoleModel;
import com.vieira96.libraryapi.model.role.RoleName;
import com.vieira96.libraryapi.model.user.UserModel;
import com.vieira96.libraryapi.repository.auth.RefreshTokenRepository;
import com.vieira96.libraryapi.repository.author.AuthorRepository;
import com.vieira96.libraryapi.repository.role.RoleRepository;
import com.vieira96.libraryapi.repository.user.UserRepository;
import com.vieira96.libraryapi.service.auth.AuthService;
import com.vieira96.libraryapi.support.IntegrationTestContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.UUID;

import com.vieira96.libraryapi.model.author.AuthorModel;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthorControllerTest extends IntegrationTestContainer {

    @LocalServerPort
    private int port;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private AuthorRepository authorRepository;

    private UUID createdUserId;
    private UUID adminUserId;
    private UUID createdAuthorId;

    @BeforeEach
    void setUp() {
        String email = "author-test." + UUID.randomUUID() + "@example.com";
        UserModel user = authService.register(new RegisterUserDTO("Maria Silva", email, "strong-password", "strong-password"));
        createdUserId = user.getId();
        var login = authService.login(new LoginUserDTO(email, "strong-password"), "127.0.0.1");
        userToken = login.response().accessToken();

        String adminEmail = "admin-test." + UUID.randomUUID() + "@example.com";
        UserModel admin = authService.register(new RegisterUserDTO("Admin User", adminEmail, "strong-password", "strong-password"));
        adminUserId = admin.getId();
        RoleModel adminRole = roleRepository.findByName(RoleName.ADMIN).orElseThrow();
        admin.getRoles().add(adminRole);
        userRepository.save(admin);
        var adminLogin = authService.login(new LoginUserDTO(adminEmail, "strong-password"), "127.0.0.1");
        adminToken = adminLogin.response().accessToken();
    }

    @AfterEach
    void cleanUpCreatedUser() {
        if (createdAuthorId != null) {
            authorRepository.deleteById(createdAuthorId);
        }
        if (adminUserId != null) {
            refreshTokenRepository.deleteAllByUser_Id(adminUserId);
            userRepository.deleteById(adminUserId);
        }
        if (createdUserId != null) {
            refreshTokenRepository.deleteAllByUser_Id(createdUserId);
            userRepository.deleteById(createdUserId);
        }
    }

    private String userToken;
    private String adminToken;

    @Test
    void shouldRejectUnauthenticatedRequestToListAuthors() throws Exception {
        HttpResponse<String> response = sendGet("/api/authors?page=1&size=10", null);

        assertThat(response.statusCode()).isEqualTo(401);
    }

    @Test
    void shouldAllowUserToListAuthors() throws Exception {
        HttpResponse<String> response = sendGet("/api/authors?page=1&size=10", userToken);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"content\"");
    }

    @Test
    void shouldAllowAdminToListAuthors() throws Exception {
        HttpResponse<String> response = sendGet("/api/authors?page=1&size=10", adminToken);

        assertThat(response.statusCode()).isEqualTo(200);
    }

    @Test
    void shouldRejectUnauthenticatedRequestToGetAuthor() throws Exception {
        HttpResponse<String> response = sendGet("/api/authors/" + UUID.randomUUID(), null);

        assertThat(response.statusCode()).isEqualTo(401);
    }

    @Test
    void shouldAllowUserToGetAuthor() throws Exception {
        AuthorModel author = createAuthor();
        HttpResponse<String> response = sendGet("/api/authors/" + author.getId(), userToken);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains(author.getName());
    }

    @Test
    void shouldRejectUserWhenCreatingAuthor() throws Exception {
        HttpResponse<String> response = sendPost("/api/authors", userToken,
                "{\"name\":\"Machado\",\"birthdate\":\"1839-06-21\",\"nationality\":\"Brasileira\"}");

        assertThat(response.statusCode()).isEqualTo(403);
    }

    @Test
    void shouldAllowAdminToCreateAuthor() throws Exception {
        HttpResponse<String> response = sendPost("/api/authors", adminToken,
                "{\"name\":\"Machado\",\"birthdate\":\"1839-06-21\",\"nationality\":\"Brasileira\"}");

        assertThat(response.statusCode()).isEqualTo(201);
    }

    @Test
    void shouldRejectUserWhenUpdatingAuthor() throws Exception {
        HttpResponse<String> response = sendPut("/api/authors/" + UUID.randomUUID(), userToken,
                "{\"name\":\"Updated\",\"birthdate\":\"1839-06-21\",\"nationality\":\"Brasileira\"}");

        assertThat(response.statusCode()).isEqualTo(403);
    }

    @Test
    void shouldRejectUserWhenDeletingAuthor() throws Exception {
        HttpResponse<String> response = sendDelete("/api/authors/" + UUID.randomUUID(), userToken);

        assertThat(response.statusCode()).isEqualTo(403);
    }

    private AuthorModel createAuthor() {
        AuthorModel author = new AuthorModel();
        author.setName("Author " + UUID.randomUUID());
        author.setBirthdate(LocalDate.of(1839, 6, 21));
        author.setNationality("Brasileira");

        AuthorModel savedAuthor = authorRepository.save(author);
        createdAuthorId = savedAuthor.getId();
        return savedAuthor;
    }

    private HttpResponse<String> sendGet(String path, String token) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .GET();
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        return HttpClient.newHttpClient().send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendPost(String path, String token, String body) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body));
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        return HttpClient.newHttpClient().send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendPut(String path, String token, String body) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(body));
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        return HttpClient.newHttpClient().send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> sendDelete(String path, String token) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .DELETE();
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        return HttpClient.newHttpClient().send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
}
