package com.cursojava.libraryapi.service.auth;

import com.cursojava.libraryapi.dto.auth.LoginUserDTO;
import com.cursojava.libraryapi.dto.auth.RegisterUserDTO;
import com.cursojava.libraryapi.exception.auth.InvalidCredentialsException;
import com.cursojava.libraryapi.exception.auth.InvalidRefreshTokenException;
import com.cursojava.libraryapi.exception.auth.UserAlreadyExistsException;
import com.cursojava.libraryapi.mapper.auth.AuthMapper;
import com.cursojava.libraryapi.model.role.RoleName;
import com.cursojava.libraryapi.model.user.UserModel;
import com.cursojava.libraryapi.repository.role.RoleRepository;
import com.cursojava.libraryapi.repository.auth.RefreshTokenRepository;
import com.cursojava.libraryapi.repository.user.UserRepository;
import com.cursojava.libraryapi.support.IntegrationTestContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class AuthServiceTest extends IntegrationTestContainer {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    void shouldRegisterUserWithNormalizedDataAndEncodedPassword() {
        RegisterUserDTO request = new RegisterUserDTO(
                "  Maria Silva  ",
                "  MARIA.SILVA@example.com  ",
                "strong-password",
                "strong-password"
        );

        UserModel registeredUser = authService.register(request);

        assertThat(registeredUser.getId()).isNotNull();
        assertThat(registeredUser.getName()).isEqualTo("Maria Silva");
        assertThat(registeredUser.getEmail()).isEqualTo("maria.silva@example.com");
        assertThat(registeredUser.getPassword()).isNotEqualTo(request.password());
        assertThat(passwordEncoder.matches(request.password(), registeredUser.getPassword())).isTrue();
        assertThat(registeredUser.getRoles())
                .extracting(role -> role.getName())
                .containsExactly(RoleName.USER);
        assertThat(AuthMapper.toRegisteredUserResponseDTO(registeredUser).roles())
                .containsExactly("USER");
        assertThat(userRepository.findById(registeredUser.getId())).isPresent();
    }

    @Test
    void shouldLoadDefaultRoles() {
        assertThat(roleRepository.findByName(RoleName.ADMIN)).isPresent();
        assertThat(roleRepository.findByName(RoleName.USER)).isPresent();
    }

    @Test
    void shouldNotRegisterUsersWithSameEmailIgnoringCase() {
        authService.register(new RegisterUserDTO(
                "Maria Silva",
                "maria.silva@example.com",
                "strong-password",
                "strong-password"
        ));

        RegisterUserDTO duplicate = new RegisterUserDTO(
                "Outra Maria",
                "MARIA.SILVA@EXAMPLE.COM",
                "another-password",
                "another-password"
        );

        assertThatThrownBy(() -> authService.register(duplicate))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Já existe um usuário cadastrado com este e-mail.");
    }

    @Test
    void shouldLoginRegisteredUserAndReturnJwt() {
        authService.register(new RegisterUserDTO(
                "Maria Silva",
                "maria.silva@example.com",
                "strong-password",
                "strong-password"
        ));

        AuthSession session = authService.login(new LoginUserDTO(
                "  MARIA.SILVA@EXAMPLE.COM  ",
                "strong-password"
        ), "127.0.0.1");
        var response = session.response();

        assertThat(response.accessToken().split("\\.")).hasSize(3);
        assertThat(session.refreshToken()).isNotBlank();
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(900L);
        assertThat(refreshTokenRepository.findAll())
                .singleElement()
                .satisfies(token -> assertThat(token.getTokenHash()).doesNotContain(session.refreshToken()));
    }

    @Test
    void shouldRejectInvalidLoginCredentials() {
        authService.register(new RegisterUserDTO(
                "Maria Silva",
                "maria.silva@example.com",
                "strong-password",
                "strong-password"
        ));

        assertThatThrownBy(() -> authService.login(new LoginUserDTO(
                "maria.silva@example.com",
                "wrong-password"
        ), "127.0.0.1"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("E-mail ou senha inválidos.");
    }

    @Test
    void shouldRotateRefreshTokenAndRejectItsReuse() {
        authService.register(new RegisterUserDTO(
                "Maria Silva",
                "maria.silva@example.com",
                "strong-password",
                "strong-password"
        ));
        AuthSession login = authService.login(new LoginUserDTO(
                "maria.silva@example.com",
                "strong-password"
        ), "127.0.0.1");

        AuthSession refreshed = authService.refresh(login.refreshToken());

        assertThat(refreshed.response().accessToken()).isNotEqualTo(login.response().accessToken());
        assertThat(refreshed.refreshToken()).isNotEqualTo(login.refreshToken());
        assertThat(refreshed.response().expiresIn()).isEqualTo(900L);
        assertThatThrownBy(() -> authService.refresh(login.refreshToken()))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessage("Refresh token inválido ou expirado.");
        assertThatThrownBy(() -> authService.refresh(refreshed.refreshToken()))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessage("Refresh token inválido ou expirado.");
    }
}
