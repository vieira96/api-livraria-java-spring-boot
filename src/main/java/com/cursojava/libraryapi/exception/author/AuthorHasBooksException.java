package com.cursojava.libraryapi.exception.author;

import java.util.UUID;

public class AuthorHasBooksException extends RuntimeException {

    public AuthorHasBooksException(UUID authorId) {
        super("Não é possível excluir o autor " + authorId + " porque ele possui livros cadastrados.");
    }
}
