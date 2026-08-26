package com.cursojava.libraryapi.repository.book;

import com.cursojava.libraryapi.model.book.BookModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface BookRepository extends JpaRepository<BookModel, UUID>, JpaSpecificationExecutor<BookModel> {
    boolean existsByAuthorId(UUID authorId);
}
