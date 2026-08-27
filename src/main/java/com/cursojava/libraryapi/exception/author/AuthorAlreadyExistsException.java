package com.cursojava.libraryapi.exception.author;

public class AuthorAlreadyExistsException extends RuntimeException {

    public AuthorAlreadyExistsException() {
        super("Já existe um autor cadastrado com este nome e data de nascimento.");
    }
}
