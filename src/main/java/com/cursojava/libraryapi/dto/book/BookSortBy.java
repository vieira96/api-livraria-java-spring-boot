package com.cursojava.libraryapi.dto.book;

public enum BookSortBy {
    TITLE("title"),
    ISBN("isbn"),
    PUBLISH_DATE("publishDate"),
    PRICE("price"),
    CREATED_AT("createdAt"),
    UPDATED_AT("updatedAt");

    private final String property;

    BookSortBy(String property) {
        this.property = property;
    }

    public String getProperty() {
        return property;
    }
}
