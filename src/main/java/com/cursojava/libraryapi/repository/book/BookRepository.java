package com.cursojava.libraryapi.repository.book;

import com.cursojava.libraryapi.model.book.BookModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;
import java.util.UUID;

public interface BookRepository extends JpaRepository<BookModel, UUID>, JpaSpecificationExecutor<BookModel> {
    @Override
    @EntityGraph(attributePaths = "author")
    Page<BookModel> findAll(Specification<BookModel> specification, Pageable pageable);

    @EntityGraph(attributePaths = "author")
    Optional<BookModel> findWithAuthorById(UUID id);

    boolean existsByAuthorId(UUID authorId);
    boolean existsByIsbnAndIdNot(String isbn, UUID bookId);
}
