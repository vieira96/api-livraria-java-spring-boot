package com.cursojava.libraryapi.service.auth;

import com.cursojava.libraryapi.dto.auth.LoginUserDTO;
import com.cursojava.libraryapi.dto.auth.RegisterUserDTO;
import com.cursojava.libraryapi.exception.auth.InvalidCredentialsException;
import com.cursojava.libraryapi.exception.auth.TooManyLoginAttemptsException;
import com.cursojava.libraryapi.exception.auth.UserAlreadyExistsException;
import com.cursojava.libraryapi.model.role.RoleModel;
import com.cursojava.libraryapi.model.role.RoleName;
import com.cursojava.libraryapi.model.user.UserModel;
import com.cursojava.libraryapi.repository.role.RoleRepository;
import com.cursojava.libraryapi.repository.user.UserRepository;
import com.cursojava.libraryapi.validator.auth.AuthValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AuthValidator authValidator;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private LoginAttemptService loginAttemptService;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldRegisterUserWithNormalizedDataAndEncodedPassword() {
        RegisterUserDTO request = new RegisterUserDTO(
                "  Maria Silva  ",
                "  MARIA.SILVA@example.com  ",
                "strong-password",
                "strong-password"
        );
        RoleModel userRole = createUserRole();
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
        when(roleRepository.findByName(RoleName.USER)).thenReturn(Optional.of(userRole));

        when(userRepository.saveAndFlush(any(UserModel.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserModel registeredUser = authService.register(request);

        ArgumentCaptor<UserModel> userCaptor = ArgumentCaptor.forClass(UserModel.class);
        verify(authValidator).validateRegistration("maria.silva@example.com");
        verify(passwordEncoder).encode("strong-password");
        verify(userRepository).saveAndFlush(userCaptor.capture());

        UserModel savedUser = userCaptor.getValue();
        assertThat(savedUser.getName()).isEqualTo("Maria Silva");
        assertThat(savedUser.getEmail()).isEqualTo("maria.silva@example.com");
        assertThat(savedUser.getPassword()).isEqualTo("encoded-password");
        assertThat(savedUser.getRoles()).containsExactly(userRole);
        assertThat(registeredUser).isSameAs(savedUser);
    }

    @Test
    void shouldNotPersistUserWhenEmailIsAlreadyRegistered() {
        RegisterUserDTO request = new RegisterUserDTO(
                "Maria Silva",
                "MARIA.SILVA@example.com",
                "strong-password",
                "strong-password"
        );
        doThrow(new UserAlreadyExistsException())
                .when(authValidator).validateRegistration("maria.silva@example.com");

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Já existe um usuário cadastrado com este e-mail.");

        verifyNoInteractions(passwordEncoder, roleRepository, userRepository);
    }

    @Test
    void shouldTranslateDatabaseEmailConflictToDomainException() {
        RegisterUserDTO request = new RegisterUserDTO(
                "Maria Silva",
                "maria.silva@example.com",
                "strong-password",
                "strong-password"
        );
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
        when(roleRepository.findByName(RoleName.USER)).thenReturn(Optional.of(createUserRole()));
        when(userRepository.saveAndFlush(any(UserModel.class)))
                .thenThrow(new DataIntegrityViolationException("Unique constraint violation"));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Já existe um usuário cadastrado com este e-mail.");
    }

    @Test
    void shouldLoginWithNormalizedEmailAndReturnAccessToken() {
        LoginUserDTO request = new LoginUserDTO("  MARIA.SILVA@example.com  ", "strong-password");
        UserModel user = new UserModel();
        user.setEmail("maria.silva@example.com");
        user.setPassword("encoded-password");
        when(userRepository.findByEmailIgnoreCase("maria.silva@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("strong-password", "encoded-password")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("access-token");
        when(jwtService.getExpirationSeconds()).thenReturn(900L);
        when(refreshTokenService.issue(user))
                .thenReturn(new RefreshTokenService.IssuedRefreshToken("refresh-token"));

        AuthSession session = authService.login(request, "127.0.0.1");
        var response = session.response();

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(session.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(900L);
        verify(userRepository).findByEmailIgnoreCase("maria.silva@example.com");
        verify(passwordEncoder).matches("strong-password", "encoded-password");
        verify(jwtService).generateToken(user);
        verify(refreshTokenService).issue(user);
        verify(loginAttemptService).consumeAttempt("127.0.0.1");
        verify(loginAttemptService).releaseSuccessfulAttempt("127.0.0.1");
    }

    @Test
    void shouldRejectLoginWhenEmailDoesNotExist() {
        LoginUserDTO request = new LoginUserDTO("unknown@example.com", "strong-password");
        when(userRepository.findByEmailIgnoreCase("unknown@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.matches(org.mockito.ArgumentMatchers.eq("strong-password"), any(String.class)))
                .thenReturn(false);

        assertThatThrownBy(() -> authService.login(request, "127.0.0.2"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("E-mail ou senha inválidos.");

        verify(passwordEncoder).matches(org.mockito.ArgumentMatchers.eq("strong-password"), any(String.class));
        verifyNoInteractions(jwtService, refreshTokenService);
    }

    @Test
    void shouldRejectLoginWhenPasswordDoesNotMatch() {
        LoginUserDTO request = new LoginUserDTO("maria@example.com", "wrong-password");
        UserModel user = new UserModel();
        user.setPassword("encoded-password");
        when(userRepository.findByEmailIgnoreCase("maria@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request, "127.0.0.3"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("E-mail ou senha inválidos.");

        verifyNoInteractions(jwtService, refreshTokenService);
    }

    @Test
    void shouldRejectLoginBeforeCheckingCredentialsWhenIpAttemptLimitIsExceeded() {
        LoginUserDTO request = new LoginUserDTO("maria@example.com", "any-password");
        doThrow(new TooManyLoginAttemptsException(245))
                .when(loginAttemptService).consumeAttempt("203.0.113.10");

        assertThatThrownBy(() -> authService.login(request, "203.0.113.10"))
                .isInstanceOf(TooManyLoginAttemptsException.class)
                .satisfies(exception -> assertThat(
                        ((TooManyLoginAttemptsException) exception).getRetryAfterSeconds()
                ).isEqualTo(245));

        verifyNoInteractions(userRepository, passwordEncoder, jwtService, refreshTokenService);
    }

    @Test
    void shouldRotateRefreshTokenAndIssueNewTokenPair() {
        UserModel user = new UserModel();
        when(refreshTokenService.rotate("current-refresh-token"))
                .thenReturn(new RefreshTokenService.RotatedRefreshToken(user, "new-refresh-token"));
        when(jwtService.generateToken(user)).thenReturn("new-access-token");
        when(jwtService.getExpirationSeconds()).thenReturn(900L);

        AuthSession session = authService.refresh("current-refresh-token");
        var response = session.response();

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(session.refreshToken()).isEqualTo("new-refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(900L);
    }

    @Test
    void shouldReturnAuthenticatedUserWithoutPassword() {
        UUID userId = UUID.randomUUID();
        UserModel user = new UserModel();
        user.setId(userId);
        user.setName("Maria Silva");
        user.setEmail("maria@example.com");
        user.setPassword("encoded-password");
        user.getRoles().add(createUserRole());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        var response = authService.getAuthenticatedUser(userId.toString());

        assertThat(response.id()).isEqualTo(userId);
        assertThat(response.name()).isEqualTo("Maria Silva");
        assertThat(response.email()).isEqualTo("maria@example.com");
        assertThat(response.roles()).containsExactly("USER");
    }

    private RoleModel createUserRole() {
        RoleModel role = new RoleModel();
        role.setName(RoleName.USER);
        return role;
    }
}
