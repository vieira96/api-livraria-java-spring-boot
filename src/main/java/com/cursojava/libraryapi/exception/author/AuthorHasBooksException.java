package com.cursojava.libraryapi.exception.author;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.CONFLICT)
public class AuthorHasBooksException extends RuntimeException {

    public AuthorHasBooksException(UUID authorId) {
        super("Não é possível excluir o autor " + authorId + " porque ele possui livros cadastrados.");
    }
}
