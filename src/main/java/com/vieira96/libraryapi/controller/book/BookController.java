package com.vieira96.libraryapi.controller.book;

import com.vieira96.libraryapi.dto.book.BookFiltersDTO;
import com.vieira96.libraryapi.dto.book.BookResponseDTO;
import com.vieira96.libraryapi.dto.book.CreateBookDTO;
import com.vieira96.libraryapi.dto.global.PageResponseDTO;
import com.vieira96.libraryapi.mapper.book.BookMapper;
import com.vieira96.libraryapi.model.book.BookModel;
import com.vieira96.libraryapi.service.book.BookService;
import org.springdoc.core.annotations.ParameterObject;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
public class BookController {
    private final BookService bookService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookResponseDTO> createBook(@Valid @RequestBody CreateBookDTO request) {
        BookModel bookModel = bookService.createBook(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BookMapper.toBookResponseDTO(bookModel));
    }

    @GetMapping
    public ResponseEntity<PageResponseDTO<BookResponseDTO>> getBooks(
            @ParameterObject @Valid @ModelAttribute BookFiltersDTO filters
    ) {
        Page<BookResponseDTO> books = bookService.getBooks(filters)
                .map(BookMapper::toBookResponseDTO);

        return ResponseEntity.ok(PageResponseDTO.from(books));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDTO> getBook(@PathVariable("id") UUID bookId) {
        BookModel book = bookService.getBookById(bookId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BookMapper.toBookResponseDTO(book));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookResponseDTO> updateBook(
            @PathVariable("id") UUID bookId,
            @Valid @RequestBody CreateBookDTO request
    ) {
        BookModel book = bookService.updateBook(bookId, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BookMapper.toBookResponseDTO(book));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBook(@PathVariable("id") UUID bookId) {
        bookService.deleteBook(bookId);
        return ResponseEntity.noContent().build();
    }
}
