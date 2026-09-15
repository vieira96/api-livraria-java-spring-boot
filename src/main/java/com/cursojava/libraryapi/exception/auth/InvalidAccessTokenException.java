package com.cursojava.libraryapi.exception.auth;

public class InvalidAccessTokenException extends RuntimeException {

    public InvalidAccessTokenException() {
        super("Token de acesso inválido.");
    }
}
