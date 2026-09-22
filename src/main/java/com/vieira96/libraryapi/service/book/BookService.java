package com.vieira96.libraryapi.service.book;

import com.vieira96.libraryapi.dto.book.BookFiltersDTO;
import com.vieira96.libraryapi.dto.book.CreateBookDTO;
import com.vieira96.libraryapi.integration.notification.NotificationPublisher;
import com.vieira96.libraryapi.model.author.AuthorModel;
import com.vieira96.libraryapi.model.book.BookModel;
import com.vieira96.libraryapi.repository.book.BookRepository;
import com.vieira96.libraryapi.validator.author.AuthorValidator;
import com.vieira96.libraryapi.validator.book.BookValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final AuthorValidator authorValidator;
    private final BookValidator bookValidator;
    private final NotificationPublisher notificationPublisher;

    private static final Set<String> SEARCHABLE_FIELDS = Set.of(
            "title",
            "isbn"
    );

    public BookModel createBook(CreateBookDTO request) {
        AuthorModel author = authorValidator.authorExists(request.authorId());
        bookValidator.verifyIfBookExistsByISBN(request.isbn(), null);
        BookModel bookModel = new BookModel();
        bookModel.setTitle(request.title());
        bookModel.setIsbn(request.isbn());
        bookModel.setPublishDate(request.publishDate());
        bookModel.setGender(request.gender());
        bookModel.setPrice(request.price());
        bookModel.setAuthor(author);

        BookModel savedBook = bookRepository.save(bookModel);
        notificationPublisher.publishBookCreated(savedBook.getId(), savedBook.getTitle());

        return savedBook;
    }

    public Page<BookModel> getBooks(BookFiltersDTO filters) {
        Specification<BookModel> specification = Specification.unrestricted();

        if (filters.search() != null && !filters.search().isBlank()) {
            specification = specification.and(searchBy(filters.search()));
        }

        if (filters.title() != null && !filters.title().isBlank()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("title")),
                            "%" + filters.title().toLowerCase(Locale.ROOT) + "%"
                    )
            );
        }

        if (filters.isbn() != null && !filters.isbn().isBlank()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("isbn")),
                            "%" + filters.isbn().toLowerCase(Locale.ROOT) + "%"
                    )
            );
        }

        if (filters.gender() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("gender"), filters.gender())
            );
        }

        if (filters.authorId() != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("author").get("id"), filters.authorId())
            );
        }

        PageRequest pageable = PageRequest.of(
                filters.pageOrDefault(),
                filters.sizeOrDefault(),
                Sort.by(filters.directionOrDefault(), filters.sortByOrDefault().getProperty())
        );

        return bookRepository.findAll(specification, pageable);
    }

    public BookModel getBookById(UUID bookId) {
        return bookValidator.verifyIfBookExists(bookId);
    }

    public BookModel updateBook(UUID bookId, CreateBookDTO request) {
        BookModel book = this.getBookById(bookId);
        AuthorModel author = authorValidator.authorExists(request.authorId());
        bookValidator.verifyIfBookExistsByISBN(request.isbn(), bookId);
        book.setTitle(request.title());
        book.setIsbn(request.isbn());
        book.setPublishDate(request.publishDate());
        book.setGender(request.gender());
        book.setPrice(request.price());
        book.setAuthor(author);

        return bookRepository.save(book);
    }

    public void deleteBook(UUID bookId) {
        BookModel book = this.getBookById(bookId);
        bookRepository.delete(book);
    }

    private Specification<BookModel> searchBy(String search) {
        return (root, query, criteriaBuilder) -> {
            String term = "%" + search.toLowerCase(Locale.ROOT) + "%";

            return criteriaBuilder.or(
                    SEARCHABLE_FIELDS.stream()
                            .map(field -> criteriaBuilder.like(
                                    criteriaBuilder.lower(root.get(field)),
                                    term
                            ))
                            .toArray(jakarta.persistence.criteria.Predicate[]::new)
            );
        };
    }
}
