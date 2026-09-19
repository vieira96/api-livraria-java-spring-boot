package com.cursojava.libraryapi.exception.author;

import com.cursojava.libraryapi.exception.global.ConflictException;

public class AuthorAlreadyExistsException extends ConflictException {

    public AuthorAlreadyExistsException() {
        super("Esse autor já está cadastrado no sistema.");
    }
}
