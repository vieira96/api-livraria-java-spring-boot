package com.vieira96.libraryapi.service.auth;

import com.vieira96.libraryapi.dto.auth.LoginResponseDTO;

public record AuthSession(
        LoginResponseDTO response,
        String refreshToken
) {}
