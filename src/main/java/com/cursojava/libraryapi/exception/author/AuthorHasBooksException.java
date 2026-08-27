package com.cursojava.libraryapi.exception.author;

import com.cursojava.libraryapi.exception.global.ConflictException;

import java.util.UUID;

public class AuthorHasBooksException extends ConflictException {

    public AuthorHasBooksException(UUID authorId) {
        super("Não é possível excluir o autor " + authorId + " porque ele possui livros cadastrados.");
    }
}
