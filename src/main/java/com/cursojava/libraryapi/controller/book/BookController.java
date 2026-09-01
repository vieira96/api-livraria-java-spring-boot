package com.cursojava.libraryapi.controller.book;

import com.cursojava.libraryapi.dto.book.BookResponseDTO;
import com.cursojava.libraryapi.dto.book.CreateBookDTO;
import com.cursojava.libraryapi.mapper.book.BookMapper;
import com.cursojava.libraryapi.model.book.BookModel;
import com.cursojava.libraryapi.service.book.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
}
