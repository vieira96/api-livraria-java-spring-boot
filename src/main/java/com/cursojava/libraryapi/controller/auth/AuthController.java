package com.cursojava.libraryapi.controller.auth;

import com.cursojava.libraryapi.dto.auth.LoginResponseDTO;
import com.cursojava.libraryapi.dto.auth.LoginUserDTO;
import com.cursojava.libraryapi.dto.auth.RegisterUserDTO;
import com.cursojava.libraryapi.dto.auth.RegisteredUserResponseDTO;
import com.cursojava.libraryapi.mapper.auth.AuthMapper;
import com.cursojava.libraryapi.model.user.UserModel;
import com.cursojava.libraryapi.service.auth.AuthService;
import com.cursojava.libraryapi.service.auth.AuthSession;
import com.cursojava.libraryapi.service.auth.RefreshTokenCookieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenCookieService refreshTokenCookieService;

    @PostMapping("/register")
    public ResponseEntity<RegisteredUserResponseDTO> register(@Valid @RequestBody RegisterUserDTO request) {
        UserModel registeredUser = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(AuthMapper.toRegisteredUserResponseDTO(registeredUser));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginUserDTO request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        AuthSession session = authService.login(request, httpRequest.getRemoteAddr());
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookieService.create(session.refreshToken()).toString());

        return ResponseEntity.ok(session.response());
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refresh(
            @CookieValue(name = RefreshTokenCookieService.COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse httpResponse
    ) {
        AuthSession session = authService.refresh(refreshToken);
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookieService.create(session.refreshToken()).toString());

        return ResponseEntity.ok(session.response());
    }

    @GetMapping("/me")
    @Operation(summary = "Retorna o usuário autenticado", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<RegisteredUserResponseDTO> me(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(authService.getAuthenticatedUser(jwt.getSubject()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Revoga a sessão atual")
    public ResponseEntity<Void> logout(
            @CookieValue(name = RefreshTokenCookieService.COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse httpResponse
    ) {
        authService.logout(refreshToken);
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookieService.clear().toString());
        return ResponseEntity.noContent().build();
    }
}
