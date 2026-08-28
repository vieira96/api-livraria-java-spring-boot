package com.cursojava.libraryapi.exception.book;

import com.cursojava.libraryapi.exception.global.ConflictException;

public class BookWithISBNAlreadyExists extends ConflictException {
    public BookWithISBNAlreadyExists(String message) {
        super(message);
    }
}
