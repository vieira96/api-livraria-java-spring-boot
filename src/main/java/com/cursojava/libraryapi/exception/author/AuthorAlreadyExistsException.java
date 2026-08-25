package com.cursojava.libraryapi.exception.author;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class AuthorAlreadyExistsException extends RuntimeException {

    public AuthorAlreadyExistsException() {
        super("Já existe um autor cadastrado com este nome e data de nascimento.");
    }
}
