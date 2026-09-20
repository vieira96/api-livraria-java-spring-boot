package com.vieira96.libraryapi.controller.book;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vieira96.libraryapi.dto.auth.LoginUserDTO;
import com.vieira96.libraryapi.dto.auth.RegisterUserDTO;
import com.vieira96.libraryapi.model.author.AuthorModel;
import com.vieira96.libraryapi.model.book.BookGender;
import com.vieira96.libraryapi.model.book.BookModel;
import com.vieira96.libraryapi.model.role.RoleModel;
import com.vieira96.libraryapi.model.role.RoleName;
import com.vieira96.libraryapi.model.user.UserModel;
import com.vieira96.libraryapi.repository.auth.RefreshTokenRepository;
import com.vieira96.libraryapi.repository.author.AuthorRepository;
import com.vieira96.libraryapi.repository.book.BookRepository;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookControllerTest extends IntegrationTestContainer {

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

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID createdUserId;
    private UUID adminUserId;
    private UUID createdAuthorId;
    private UUID createdBookId;

    @BeforeEach
    void setUp() {
        String email = "book-test." + UUID.randomUUID() + "@example.com";
        UserModel user = authService.register(new RegisterUserDTO("Maria Silva", email, "strong-password", "strong-password"));
        createdUserId = user.getId();
        var login = authService.login(new LoginUserDTO(email, "strong-password"), "127.0.0.1");
        userToken = login.response().accessToken();

        String adminEmail = "admin-book-test." + UUID.randomUUID() + "@example.com";
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
        if (createdBookId != null) {
            bookRepository.deleteById(createdBookId);
        }
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
    void shouldRejectUnauthenticatedRequestToListBooks() throws Exception {
        HttpResponse<String> response = sendGet("/api/books?page=1&size=10", null);

        assertThat(response.statusCode()).isEqualTo(401);
    }

    @Test
    void shouldAllowUserToListBooks() throws Exception {
        HttpResponse<String> response = sendGet("/api/books?page=1&size=10", userToken);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"content\"");
    }

    @Test
    void shouldAllowAdminToListBooks() throws Exception {
        HttpResponse<String> response = sendGet("/api/books?page=1&size=10", adminToken);

        assertThat(response.statusCode()).isEqualTo(200);
    }

    @Test
    void shouldRejectUnauthenticatedRequestToGetBook() throws Exception {
        HttpResponse<String> response = sendGet("/api/books/" + UUID.randomUUID(), null);

        assertThat(response.statusCode()).isEqualTo(401);
    }

    @Test
    void shouldAllowUserToGetBook() throws Exception {
        BookModel book = createBook(createAuthor());
        HttpResponse<String> response = sendGet("/api/books/" + book.getId(), userToken);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains(book.getTitle());
    }

    @Test
    void shouldRejectUserWhenCreatingBook() throws Exception {
        HttpResponse<String> response = sendPost("/api/books", userToken,
                "{\"title\":\"O Cortiço\",\"isbn\":\"978-85-7326-109-7\",\"publishDate\":\"1890-03-15\",\"gender\":\"ROMANCE\",\"price\":49.90,\"authorId\":\"" + UUID.randomUUID() + "\"}");

        assertThat(response.statusCode()).isEqualTo(403);
    }

    @Test
    void shouldAllowAdminToCreateBook() throws Exception {
        AuthorModel author = createAuthor();
        HttpResponse<String> response = sendPost("/api/books", adminToken,
                "{\"title\":\"O Cortiço\",\"isbn\":\"978-85-7326-109-7\",\"publishDate\":\"1890-03-15\",\"gender\":\"ROMANCE\",\"price\":49.90,\"authorId\":\"" + author.getId() + "\"}");

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.body()).contains("O Cortiço");
        createdBookId = UUID.fromString(objectMapper.readTree(response.body()).get("id").asText());
    }

    @Test
    void shouldRejectUserWhenUpdatingBook() throws Exception {
        HttpResponse<String> response = sendPut("/api/books/" + UUID.randomUUID(), userToken,
                "{\"title\":\"O Cortiço\",\"isbn\":\"978-85-7326-109-7\",\"publishDate\":\"1890-03-15\",\"gender\":\"ROMANCE\",\"price\":49.90,\"authorId\":\"" + UUID.randomUUID() + "\"}");

        assertThat(response.statusCode()).isEqualTo(403);
    }

    @Test
    void shouldRejectUserWhenDeletingBook() throws Exception {
        HttpResponse<String> response = sendDelete("/api/books/" + UUID.randomUUID(), userToken);

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

    private BookModel createBook(AuthorModel author) {
        BookModel book = new BookModel();
        book.setTitle("Book " + UUID.randomUUID());
        book.setIsbn("isbn-" + UUID.randomUUID());
        book.setPublishDate(LocalDate.of(1890, 3, 15));
        book.setGender(BookGender.ROMANCE);
        book.setPrice(new BigDecimal("49.90"));
        book.setAuthor(author);

        BookModel savedBook = bookRepository.save(book);
        createdBookId = savedBook.getId();
        return savedBook;
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
