package com.cursojava.libraryapi.exception.auth;

import com.cursojava.libraryapi.exception.global.ConflictException;

public class UserAlreadyExistsException extends ConflictException {

    public UserAlreadyExistsException() {
        super("Já existe um usuário cadastrado com este e-mail.");
    }
}
