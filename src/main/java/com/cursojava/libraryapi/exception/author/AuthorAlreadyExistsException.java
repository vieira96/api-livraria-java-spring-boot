package com.cursojava.libraryapi.exception.author;

import com.cursojava.libraryapi.exception.global.ConflictException;

public class AuthorAlreadyExistsException extends ConflictException {

    public AuthorAlreadyExistsException() {
        super("Já existe um autor cadastrado com este nome e data de nascimento.");
    }
}
