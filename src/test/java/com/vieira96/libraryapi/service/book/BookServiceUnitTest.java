package com.vieira96.libraryapi.service.book;

import com.vieira96.libraryapi.dto.book.BookFiltersDTO;
import com.vieira96.libraryapi.dto.book.CreateBookDTO;
import com.vieira96.libraryapi.exception.book.BookWithISBNAlreadyExists;
import com.vieira96.libraryapi.exception.global.NotFoundException;
import com.vieira96.libraryapi.model.author.AuthorModel;
import com.vieira96.libraryapi.model.book.BookGender;
import com.vieira96.libraryapi.model.book.BookModel;
import com.vieira96.libraryapi.repository.book.BookRepository;
import com.vieira96.libraryapi.validator.author.AuthorValidator;
import com.vieira96.libraryapi.validator.book.BookValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceUnitTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorValidator authorValidator;

    @Mock
    private BookValidator bookValidator;

    @InjectMocks
    private BookService bookService;

    @Test
    void shouldCreateBookWithValidData() {
        UUID authorId = UUID.randomUUID();
        AuthorModel author = new AuthorModel();
        author.setId(authorId);

        CreateBookDTO request = new CreateBookDTO(
                "O Cortiço",
                "978-85-7326-109-7",
                LocalDate.of(1890, 3, 15),
                BookGender.ROMANCE,
                new BigDecimal("49.90"),
                authorId
        );

        when(authorValidator.authorExists(authorId)).thenReturn(author);

        bookService.createBook(request);

        ArgumentCaptor<BookModel> bookCaptor = ArgumentCaptor.forClass(BookModel.class);
        verify(bookValidator).verifyIfBookExistsByISBN("978-85-7326-109-7", null);
        verify(bookRepository).save(bookCaptor.capture());

        BookModel savedBook = bookCaptor.getValue();
        assertThat(savedBook.getTitle()).isEqualTo("O Cortiço");
        assertThat(savedBook.getIsbn()).isEqualTo("978-85-7326-109-7");
        assertThat(savedBook.getPublishDate()).isEqualTo(LocalDate.of(1890, 3, 15));
        assertThat(savedBook.getGender()).isEqualTo(BookGender.ROMANCE);
        assertThat(savedBook.getPrice()).isEqualByComparingTo(new BigDecimal("49.90"));
        assertThat(savedBook.getAuthor()).isSameAs(author);
    }

    @Test
    void shouldRejectCreateBookWhenISBNAlreadyExists() {
        UUID authorId = UUID.randomUUID();
        AuthorModel author = new AuthorModel();
        author.setId(authorId);

        CreateBookDTO request = new CreateBookDTO(
                "O Cortiço",
                "978-85-7326-109-7",
                LocalDate.of(1890, 3, 15),
                BookGender.ROMANCE,
                new BigDecimal("49.90"),
                authorId
        );

        when(authorValidator.authorExists(authorId)).thenReturn(author);
        doThrow(new BookWithISBNAlreadyExists("Já existe um livro com o código isbn: 978-85-7326-109-7"))
                .when(bookValidator).verifyIfBookExistsByISBN("978-85-7326-109-7", null);

        assertThatThrownBy(() -> bookService.createBook(request))
                .isInstanceOf(BookWithISBNAlreadyExists.class)
                .hasMessage("Já existe um livro com o código isbn: 978-85-7326-109-7");

        verifyNoInteractions(bookRepository);
    }

    @Test
    void shouldRejectCreateBookWhenAuthorNotFound() {
        UUID authorId = UUID.randomUUID();

        CreateBookDTO request = new CreateBookDTO(
                "O Cortiço",
                "978-85-7326-109-7",
                LocalDate.of(1890, 3, 15),
                BookGender.ROMANCE,
                new BigDecimal("49.90"),
                authorId
        );

        when(authorValidator.authorExists(authorId))
                .thenThrow(new NotFoundException("Author not found with id: " + authorId));

        assertThatThrownBy(() -> bookService.createBook(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Author not found with id: " + authorId);

        verifyNoInteractions(bookRepository, bookValidator);
    }

    @Test
    void shouldGetBookById() {
        UUID bookId = UUID.randomUUID();
        BookModel book = new BookModel();
        book.setId(bookId);
        book.setTitle("O Cortiço");

        when(bookValidator.verifyIfBookExists(bookId)).thenReturn(book);

        BookModel result = bookService.getBookById(bookId);

        assertThat(result).isSameAs(book);
        verify(bookValidator).verifyIfBookExists(bookId);
    }

    @Test
    void shouldRejectGetBookByIdWhenBookNotFound() {
        UUID bookId = UUID.randomUUID();

        when(bookValidator.verifyIfBookExists(bookId))
                .thenThrow(new NotFoundException("Livro com o ID: " + bookId + " Não encontrado"));

        assertThatThrownBy(() -> bookService.getBookById(bookId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Livro com o ID: " + bookId + " Não encontrado");
    }

    @Test
    void shouldUpdateBookWithValidData() {
        UUID bookId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        BookModel existingBook = new BookModel();
        existingBook.setId(bookId);
        existingBook.setTitle("Old Title");

        AuthorModel author = new AuthorModel();
        author.setId(authorId);

        CreateBookDTO request = new CreateBookDTO(
                "O Cortiço",
                "978-85-7326-109-7",
                LocalDate.of(1890, 3, 15),
                BookGender.ROMANCE,
                new BigDecimal("49.90"),
                authorId
        );

        when(bookValidator.verifyIfBookExists(bookId)).thenReturn(existingBook);
        when(authorValidator.authorExists(authorId)).thenReturn(author);

        bookService.updateBook(bookId, request);

        ArgumentCaptor<BookModel> bookCaptor = ArgumentCaptor.forClass(BookModel.class);
        verify(bookValidator).verifyIfBookExistsByISBN("978-85-7326-109-7", bookId);
        verify(bookRepository).save(bookCaptor.capture());

        BookModel savedBook = bookCaptor.getValue();
        assertThat(savedBook.getTitle()).isEqualTo("O Cortiço");
        assertThat(savedBook.getIsbn()).isEqualTo("978-85-7326-109-7");
        assertThat(savedBook.getPublishDate()).isEqualTo(LocalDate.of(1890, 3, 15));
        assertThat(savedBook.getGender()).isEqualTo(BookGender.ROMANCE);
        assertThat(savedBook.getPrice()).isEqualByComparingTo(new BigDecimal("49.90"));
        assertThat(savedBook.getAuthor()).isSameAs(author);
    }

    @Test
    void shouldRejectUpdateBookWhenBookNotFound() {
        UUID bookId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        CreateBookDTO request = new CreateBookDTO(
                "O Cortiço",
                "978-85-7326-109-7",
                LocalDate.of(1890, 3, 15),
                BookGender.ROMANCE,
                new BigDecimal("49.90"),
                authorId
        );

        when(bookValidator.verifyIfBookExists(bookId))
                .thenThrow(new NotFoundException("Livro com o ID: " + bookId + " Não encontrado"));

        assertThatThrownBy(() -> bookService.updateBook(bookId, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Livro com o ID: " + bookId + " Não encontrado");

        verifyNoInteractions(bookRepository);
    }

    @Test
    void shouldRejectUpdateBookWhenISBNAlreadyExists() {
        UUID bookId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        BookModel existingBook = new BookModel();
        existingBook.setId(bookId);

        AuthorModel author = new AuthorModel();
        author.setId(authorId);

        CreateBookDTO request = new CreateBookDTO(
                "O Cortiço",
                "978-85-7326-109-7",
                LocalDate.of(1890, 3, 15),
                BookGender.ROMANCE,
                new BigDecimal("49.90"),
                authorId
        );

        when(bookValidator.verifyIfBookExists(bookId)).thenReturn(existingBook);
        when(authorValidator.authorExists(authorId)).thenReturn(author);
        doThrow(new BookWithISBNAlreadyExists("Já existe um livro com o código isbn: 978-85-7326-109-7"))
                .when(bookValidator).verifyIfBookExistsByISBN("978-85-7326-109-7", bookId);

        assertThatThrownBy(() -> bookService.updateBook(bookId, request))
                .isInstanceOf(BookWithISBNAlreadyExists.class)
                .hasMessage("Já existe um livro com o código isbn: 978-85-7326-109-7");

        verifyNoInteractions(bookRepository);
    }

    @Test
    void shouldDeleteBook() {
        UUID bookId = UUID.randomUUID();
        BookModel book = new BookModel();
        book.setId(bookId);

        when(bookValidator.verifyIfBookExists(bookId)).thenReturn(book);

        bookService.deleteBook(bookId);

        verify(bookValidator).verifyIfBookExists(bookId);
        verify(bookRepository).delete(book);
    }

    @Test
    void shouldRejectDeleteBookWhenBookNotFound() {
        UUID bookId = UUID.randomUUID();

        when(bookValidator.verifyIfBookExists(bookId))
                .thenThrow(new NotFoundException("Livro com o ID: " + bookId + " Não encontrado"));

        assertThatThrownBy(() -> bookService.deleteBook(bookId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Livro com o ID: " + bookId + " Não encontrado");

        verifyNoInteractions(bookRepository);
    }

    @Test
    void shouldReturnPaginatedBooksWithFilters() {
        BookFiltersDTO filters = new BookFiltersDTO(1, 10, null, null, null, null, null, null, null);
        BookModel book = new BookModel();
        book.setTitle("O Cortiço");
        Page<BookModel> page = new PageImpl<>(List.of(book), PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")), 1);

        when(bookRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);

        Page<BookModel> result = bookService.getBooks(filters);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("O Cortiço");
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(bookRepository).findAll(any(Specification.class), any(PageRequest.class));
    }
}
