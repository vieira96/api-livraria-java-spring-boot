package com.cursojava.libraryapi.dto.author;

public enum AuthorSortBy {
    NAME("name"),
    BIRTHDATE("birthdate"),
    CREATED_AT("createdAt"),
    UPDATED_AT("updatedAt");

    private final String property;

    AuthorSortBy(String property) {
        this.property = property;
    }

    public String getProperty() {
        return property;
    }
}
