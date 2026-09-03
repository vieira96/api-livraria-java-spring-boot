package com.cursojava.libraryapi.controller.book;

import com.cursojava.libraryapi.dto.author.AuthorFiltersDTO;
import com.cursojava.libraryapi.dto.author.AuthorResponseDTO;
import com.cursojava.libraryapi.dto.book.BookResponseDTO;
import com.cursojava.libraryapi.dto.book.BookFiltersDTO;
import com.cursojava.libraryapi.dto.book.CreateBookDTO;
import com.cursojava.libraryapi.dto.global.PageResponseDTO;
import com.cursojava.libraryapi.mapper.author.AuthorMapper;
import com.cursojava.libraryapi.mapper.book.BookMapper;
import com.cursojava.libraryapi.model.book.BookModel;
import com.cursojava.libraryapi.service.book.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
public class BookController {
    private final BookService bookService;

    @PostMapping
    public ResponseEntity<BookResponseDTO> createBook(@Valid @RequestBody CreateBookDTO request) {
        BookModel bookModel = bookService.createBook(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BookMapper.toBookResponseDTO(bookModel));
    }

    @GetMapping
    public ResponseEntity<PageResponseDTO<BookResponseDTO>> getAuthors(
            @Valid @ModelAttribute BookFiltersDTO filters
    ) {
        Page<BookResponseDTO> books = bookService.getBooks(filters)
                .map(BookMapper::toBookResponseDTO);

        return ResponseEntity.ok(PageResponseDTO.from(books));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDTO> getBook(@PathVariable UUID bookId) {
        BookModel book = bookService.getBookById(bookId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BookMapper.toBookResponseDTO(book));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookResponseDTO> updateBook(
            @PathVariable UUID bookId,
            @Valid @RequestBody CreateBookDTO request
    ) {
        BookModel book = bookService.updateBook(bookId, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BookMapper.toBookResponseDTO(book));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable UUID bookId) {
        bookService.deleteBook(bookId);
        return ResponseEntity.noContent().build();
    }
}
