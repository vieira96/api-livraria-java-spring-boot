package com.vieira96.libraryapi.integration.notification;

import java.util.UUID;

public record BookCreatedEvent(String pattern, Data data) {

    public record Data(UUID bookId, String title) {
    }

    public static BookCreatedEvent of(UUID bookId, String title) {
        return new BookCreatedEvent("book.created", new Data(bookId, title));
    }
}
