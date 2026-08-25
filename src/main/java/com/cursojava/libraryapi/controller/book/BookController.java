package com.cursojava.libraryapi.controller.book;

import com.cursojava.libraryapi.dto.book.BookResponseDTO;
import com.cursojava.libraryapi.dto.book.CreateBookDTO;
import com.cursojava.libraryapi.mapper.book.BookMapper;
import com.cursojava.libraryapi.model.book.BookModel;
import com.cursojava.libraryapi.service.book.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/books")
public class BookController {
    private final BookService bookService;

    @PostMapping
    public ResponseEntity<BookResponseDTO> createBook(@RequestBody CreateBookDTO request) {
        BookModel bookModel = bookService.createBook(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BookMapper.toBookResponseDTO(bookModel));
    }
}
