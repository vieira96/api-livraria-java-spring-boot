package com.cursojava.libraryapi.controller.auth;

import com.cursojava.libraryapi.dto.auth.LoginResponseDTO;
import com.cursojava.libraryapi.dto.auth.LoginUserDTO;
import com.cursojava.libraryapi.dto.auth.RefreshTokenDTO;
import com.cursojava.libraryapi.dto.auth.RegisterUserDTO;
import com.cursojava.libraryapi.dto.auth.RegisteredUserResponseDTO;
import com.cursojava.libraryapi.mapper.auth.AuthMapper;
import com.cursojava.libraryapi.model.user.UserModel;
import com.cursojava.libraryapi.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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

    @PostMapping("/register")
    public ResponseEntity<RegisteredUserResponseDTO> register(@Valid @RequestBody RegisterUserDTO request) {
        UserModel registeredUser = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(AuthMapper.toRegisteredUserResponseDTO(registeredUser));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginUserDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refresh(@Valid @RequestBody RefreshTokenDTO request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Retorna o usuário autenticado", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<RegisteredUserResponseDTO> me(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(authService.getAuthenticatedUser(jwt.getSubject()));
    }
}
