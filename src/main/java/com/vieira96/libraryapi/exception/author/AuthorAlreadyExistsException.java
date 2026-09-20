package com.vieira96.libraryapi.exception.author;

import com.vieira96.libraryapi.exception.global.ConflictException;

public class AuthorAlreadyExistsException extends ConflictException {

    public AuthorAlreadyExistsException() {
        super("Esse autor já está cadastrado no sistema.");
    }
}
