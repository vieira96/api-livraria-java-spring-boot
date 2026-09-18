package com.cursojava.libraryapi.exception.auth;

import com.cursojava.libraryapi.exception.global.ConflictException;

public class PasswordsDoNotMatchException extends ConflictException {

    public PasswordsDoNotMatchException() {
        super("As senhas não conferem.");
    }
}
