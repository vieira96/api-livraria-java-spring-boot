package com.vieira96.libraryapi.exception.auth;

public class InvalidAccessTokenException extends RuntimeException {

    public InvalidAccessTokenException() {
        super("Token de acesso inválido.");
    }
}
