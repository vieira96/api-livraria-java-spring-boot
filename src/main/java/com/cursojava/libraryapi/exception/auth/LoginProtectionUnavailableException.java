package com.cursojava.libraryapi.exception.auth;

public class LoginProtectionUnavailableException extends RuntimeException {

    public LoginProtectionUnavailableException(Throwable cause) {
        super("Login temporariamente indisponível. Tente novamente mais tarde.", cause);
    }
}
