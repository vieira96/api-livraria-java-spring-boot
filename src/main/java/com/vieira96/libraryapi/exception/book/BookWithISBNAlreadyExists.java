package com.vieira96.libraryapi.exception.book;

import com.vieira96.libraryapi.exception.global.ConflictException;

public class BookWithISBNAlreadyExists extends ConflictException {
    public BookWithISBNAlreadyExists(String message) {
        super(message);
    }
}
