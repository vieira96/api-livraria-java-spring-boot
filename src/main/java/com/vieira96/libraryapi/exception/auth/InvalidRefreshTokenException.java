package com.vieira96.libraryapi.exception.auth;

public class InvalidRefreshTokenException extends RuntimeException {

    public InvalidRefreshTokenException() {
        super("Refresh token inválido ou expirado.");
    }
}
