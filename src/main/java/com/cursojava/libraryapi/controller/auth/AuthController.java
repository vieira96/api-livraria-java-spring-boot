package com.cursojava.libraryapi.controller.auth;

import com.cursojava.libraryapi.dto.auth.RegisterUserDTO;
import com.cursojava.libraryapi.dto.auth.RegisteredUserResponseDTO;
import com.cursojava.libraryapi.mapper.auth.AuthMapper;
import com.cursojava.libraryapi.model.user.UserModel;
import com.cursojava.libraryapi.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
}
