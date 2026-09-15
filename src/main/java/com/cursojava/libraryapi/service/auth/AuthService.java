package com.cursojava.libraryapi.service.auth;

import com.cursojava.libraryapi.dto.auth.LoginResponseDTO;
import com.cursojava.libraryapi.dto.auth.LoginUserDTO;
import com.cursojava.libraryapi.dto.auth.RefreshTokenDTO;
import com.cursojava.libraryapi.dto.auth.RegisterUserDTO;
import com.cursojava.libraryapi.dto.auth.RegisteredUserResponseDTO;
import com.cursojava.libraryapi.exception.auth.InvalidAccessTokenException;
import com.cursojava.libraryapi.exception.auth.InvalidCredentialsException;
import com.cursojava.libraryapi.exception.auth.InvalidRefreshTokenException;
import com.cursojava.libraryapi.exception.auth.UserAlreadyExistsException;
import com.cursojava.libraryapi.model.role.RoleModel;
import com.cursojava.libraryapi.model.role.RoleName;
import com.cursojava.libraryapi.model.user.UserModel;
import com.cursojava.libraryapi.mapper.auth.AuthMapper;
import com.cursojava.libraryapi.repository.role.RoleRepository;
import com.cursojava.libraryapi.repository.user.UserRepository;
import com.cursojava.libraryapi.validator.auth.AuthValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String DUMMY_PASSWORD_HASH =
            "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthValidator authValidator;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public UserModel register(RegisterUserDTO request) {
        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        authValidator.validateRegistration(normalizedEmail);

        UserModel user = new UserModel();
        user.setName(request.name().trim());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.password()));

        RoleModel defaultRole = roleRepository.findByName(RoleName.USER)
                .orElseThrow(() -> new IllegalStateException("Role USER não encontrada."));
        user.getRoles().add(defaultRole);

        try {
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            throw new UserAlreadyExistsException();
        }
    }

    @Transactional
    public LoginResponseDTO login(LoginUserDTO request) {
        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        var foundUser = userRepository.findByEmailIgnoreCase(normalizedEmail);
        String passwordHash = foundUser.map(UserModel::getPassword).orElse(DUMMY_PASSWORD_HASH);
        boolean passwordMatches = passwordEncoder.matches(request.password(), passwordHash);

        if (foundUser.isEmpty() || !passwordMatches) {
            throw new InvalidCredentialsException();
        }

        UserModel user = foundUser.get();
        String refreshToken = refreshTokenService.issue(user).value();

        return createTokenResponse(user, refreshToken);
    }

    @Transactional(dontRollbackOn = InvalidRefreshTokenException.class)
    public LoginResponseDTO refresh(RefreshTokenDTO request) {
        RefreshTokenService.RotatedRefreshToken rotatedToken = refreshTokenService.rotate(request.refreshToken());

        return createTokenResponse(rotatedToken.user(), rotatedToken.value());
    }

    @Transactional
    public RegisteredUserResponseDTO getAuthenticatedUser(String subject) {
        final UUID userId;
        try {
            userId = UUID.fromString(subject);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new InvalidAccessTokenException();
        }

        UserModel user = userRepository.findById(userId)
                .orElseThrow(InvalidAccessTokenException::new);

        return AuthMapper.toRegisteredUserResponseDTO(user);
    }

    private LoginResponseDTO createTokenResponse(UserModel user, String refreshToken) {
        return new LoginResponseDTO(
                jwtService.generateToken(user),
                refreshToken,
                "Bearer",
                jwtService.getExpirationSeconds()
        );
    }
}
