package com.vieira96.libraryapi.exception.auth;

import com.vieira96.libraryapi.exception.global.ConflictException;

public class PasswordsDoNotMatchException extends ConflictException {

    public PasswordsDoNotMatchException() {
        super("As senhas não conferem.");
    }
}
