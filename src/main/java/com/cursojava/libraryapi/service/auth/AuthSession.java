package com.cursojava.libraryapi.service.auth;

import com.cursojava.libraryapi.dto.auth.LoginResponseDTO;

public record AuthSession(
        LoginResponseDTO response,
        String refreshToken
) {}
