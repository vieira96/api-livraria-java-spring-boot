package com.cursojava.libraryapi.exception.global;

public abstract class ConflictException extends RuntimeException {

    protected ConflictException(String message) {
        super(message);
    }
}
