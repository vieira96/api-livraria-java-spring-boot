package com.cursojava.libraryapi.repository.book;

import com.cursojava.libraryapi.model.author.AuthorModel;
import com.cursojava.libraryapi.model.book.BookGender;
import com.cursojava.libraryapi.model.book.BookModel;
import com.cursojava.libraryapi.repository.author.AuthorRepository;
import com.cursojava.libraryapi.validator.author.AuthorValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@SpringBootTest
public class BookRepositoryTest {

    @Autowired
    BookRepository bookRepository;

    @Autowired
    AuthorRepository authorRepository;

    @Autowired
    AuthorValidator authorValidator;

    @Test
    public void saveTest() {
        UUID authorId = UUID.fromString("5a5c90df-d629-45a2-b0ce-5aff66cd3c9e");
        AuthorModel author = authorValidator.authorExists(authorId);

        BookModel book = new BookModel();
        book.setIsbn(UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        book.setTitle("Livro de teste");
        book.setPublishDate(LocalDate.now());
        book.setGender(BookGender.FICCAO);
        book.setPrice(new BigDecimal("49.90"));
        book.setAuthor(author);

        BookModel bookSaved = bookRepository.saveAndFlush(book);

        System.out.println("Livro persistido: " + bookSaved);
    }

    @Test
    public void findAll() {
        String search = null;
        LocalDate publishDateFrom = LocalDate.of(2020, 1, 1);
        LocalDate publishDateTo = LocalDate.of(2020, 1, 1);
        String sortBy = "publishDate";
        Sort.Direction sortDirection = Sort.Direction.DESC;
        int page = 0;
        int size = 10;

        Specification<BookModel> specification = Specification.unrestricted();

        if (search != null && !search.isBlank()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("title")),
                            "%" + search.toLowerCase(Locale.ROOT) + "%"
                    )
            );
        }

        if (publishDateFrom != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("publishDate"), publishDateFrom)
            );
        }

        if (publishDateTo != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get("publishDate"), publishDateTo)
            );
        }

        var result = bookRepository.findAll(
                specification,
                PageRequest.of(page, size, Sort.by(sortDirection, sortBy))
        );

        result.forEach(System.out::println);
        System.out.println("Página: " + result.getNumber());
        System.out.println("Total de elementos: " + result.getTotalElements());
        System.out.println("Total de páginas: " + result.getTotalPages());
    }

}
