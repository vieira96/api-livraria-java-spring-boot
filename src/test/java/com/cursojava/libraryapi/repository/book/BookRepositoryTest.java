package com.cursojava.libraryapi.repository.book;

import com.cursojava.libraryapi.model.book.BookModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.LocalDate;
import java.util.Locale;


@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
public class BookRepositoryTest {

    @Autowired
    BookRepository bookRepository;

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
