package com.cursojava.libraryapi.exception.auth;

public class InvalidRefreshTokenException extends RuntimeException {

    public InvalidRefreshTokenException() {
        super("Refresh token inválido ou expirado.");
    }
}
