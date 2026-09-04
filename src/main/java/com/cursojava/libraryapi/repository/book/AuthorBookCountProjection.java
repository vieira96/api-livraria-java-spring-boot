package com.cursojava.libraryapi.repository.book;

import java.util.UUID;

public interface AuthorBookCountProjection {
    UUID getAuthorId();
    long getBookCount();
}
