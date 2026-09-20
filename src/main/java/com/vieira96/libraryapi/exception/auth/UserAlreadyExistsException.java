package com.vieira96.libraryapi.exception.auth;

import com.vieira96.libraryapi.exception.global.ConflictException;

public class UserAlreadyExistsException extends ConflictException {

    public UserAlreadyExistsException() {
        super("Já existe um usuário cadastrado com este e-mail.");
    }
}
