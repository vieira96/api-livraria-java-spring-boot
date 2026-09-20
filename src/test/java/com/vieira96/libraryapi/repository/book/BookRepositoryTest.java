package com.vieira96.libraryapi.repository.book;

import com.vieira96.libraryapi.config.AuditingConfiguration;
import com.vieira96.libraryapi.model.author.AuthorModel;
import com.vieira96.libraryapi.model.book.BookGender;
import com.vieira96.libraryapi.model.book.BookModel;
import com.vieira96.libraryapi.repository.author.AuthorRepository;
import com.vieira96.libraryapi.support.IntegrationTestContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(AuditingConfiguration.class)
class BookRepositoryTest extends IntegrationTestContainer {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Test
    void shouldSaveBook() {
        AuthorModel author = saveAuthor("J. R. R. Tolkien");
        BookModel book = createBook(author, "9780261102385", "The Lord of the Rings", LocalDate.of(1954, 7, 29));

        BookModel savedBook = bookRepository.saveAndFlush(book);

        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getCreatedAt()).isNotNull();
        assertThat(savedBook.getUpdatedAt()).isNotNull();
        assertThat(bookRepository.findWithAuthorById(savedBook.getId()))
                .isPresent()
                .get()
                .extracting(BookModel::getAuthor)
                .isEqualTo(author);
    }

    @Test
    void shouldFindBooksByTitleWithPagination() {
        AuthorModel author = saveAuthor("Isaac Asimov");
        bookRepository.saveAndFlush(createBook(author, "9780553293357", "Foundation", LocalDate.of(1951, 6, 1)));
        bookRepository.saveAndFlush(createBook(author, "9780553293364", "Foundation and Empire", LocalDate.of(1952, 1, 1)));
        bookRepository.saveAndFlush(createBook(author, "9780451524935", "Nineteen Eighty-Four", LocalDate.of(1949, 6, 8)));

        Specification<BookModel> titleContainsFoundation = (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")),
                        "%foundation%"
                );

        Page<BookModel> result = bookRepository.findAll(titleContainsFoundation, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(BookModel::getTitle)
                .containsExactlyInAnyOrder("Foundation", "Foundation and Empire");
    }

    @Test
    void shouldCheckIfBooksExistByAuthor() {
        AuthorModel author = saveAuthor("George Orwell");
        bookRepository.saveAndFlush(createBook(author, "9780451524935", "Nineteen Eighty-Four", LocalDate.of(1949, 6, 8)));

        assertThat(bookRepository.existsByAuthorId(author.getId())).isTrue();
        assertThat(bookRepository.existsByAuthorId(UUID.randomUUID())).isFalse();
    }

    @Test
    void shouldCheckIfAnotherBookExistsWithSameIsbn() {
        AuthorModel author = saveAuthor("Frank Herbert");
        BookModel book = bookRepository.saveAndFlush(
                createBook(author, "9780441172719", "Dune", LocalDate.of(1965, 8, 1))
        );

        assertThat(bookRepository.existsByIsbnAndIdNot(book.getIsbn(), UUID.randomUUID())).isTrue();
        assertThat(bookRepository.existsByIsbnAndIdNot(book.getIsbn(), book.getId())).isFalse();
    }

    @Test
    void shouldCountBooksByAuthorIds() {
        AuthorModel firstAuthor = saveAuthor("Isaac Asimov");
        AuthorModel secondAuthor = saveAuthor("George Orwell");
        bookRepository.saveAndFlush(createBook(firstAuthor, "9780553293357", "Foundation", LocalDate.of(1951, 6, 1)));
        bookRepository.saveAndFlush(createBook(firstAuthor, "9780553293364", "Foundation and Empire", LocalDate.of(1952, 1, 1)));
        bookRepository.saveAndFlush(createBook(secondAuthor, "9780451524935", "Nineteen Eighty-Four", LocalDate.of(1949, 6, 8)));

        Map<UUID, Long> counts = bookRepository.countBooksByAuthorIds(
                        List.of(firstAuthor.getId(), secondAuthor.getId())
                ).stream()
                .collect(Collectors.toMap(
                        AuthorBookCountProjection::getAuthorId,
                        AuthorBookCountProjection::getBookCount
                ));

        assertThat(counts)
                .containsEntry(firstAuthor.getId(), 2L)
                .containsEntry(secondAuthor.getId(), 1L);
    }

    private AuthorModel saveAuthor(String name) {
        AuthorModel author = new AuthorModel();
        author.setName(name);
        author.setBirthdate(LocalDate.of(1920, 1, 1));
        author.setNationality("British");
        return authorRepository.saveAndFlush(author);
    }

    private BookModel createBook(AuthorModel author, String isbn, String title, LocalDate publishDate) {
        BookModel book = new BookModel();
        book.setIsbn(isbn);
        book.setTitle(title);
        book.setPublishDate(publishDate);
        book.setGender(BookGender.FICCAO);
        book.setPrice(new BigDecimal("49.90"));
        book.setAuthor(author);
        return book;
    }
}
