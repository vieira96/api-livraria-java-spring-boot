package com.cursojava.libraryapi.validator.book;

import com.cursojava.libraryapi.exception.book.BookWithISBNAlreadyExists;
import com.cursojava.libraryapi.exception.global.NotFoundException;
import com.cursojava.libraryapi.model.book.BookModel;
import com.cursojava.libraryapi.repository.book.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BookValidator {
    private final BookRepository bookRepository;

    public void verifyIfBookExistsByISBN(String isbn, UUID currentBookUuid) {
        if (this.bookRepository.existsByIsbnAndIdNot(isbn, currentBookUuid)) {
            throw new BookWithISBNAlreadyExists("Já existe um livro com o código isbn: " + isbn);
        }
    }

    public BookModel verifyIfBookExists(UUID bookId) {
        return bookRepository.findById(bookId).orElseThrow(() ->
                new NotFoundException("Livro com o ID: " + bookId + " Não encontrado"));
    }
}
