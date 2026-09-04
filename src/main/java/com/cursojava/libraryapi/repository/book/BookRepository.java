package com.cursojava.libraryapi.repository.book;

import com.cursojava.libraryapi.model.book.BookModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
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

    @Query("""
            select b.author.id as authorId, count(b) as bookCount
            from BookModel b
            where b.author.id in :authorIds
            group by b.author.id
            """)
    List<AuthorBookCountProjection> countBooksByAuthorIds(
            @Param("authorIds") Collection<UUID> authorIds
    );
}
