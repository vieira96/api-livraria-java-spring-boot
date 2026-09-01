package com.cursojava.libraryapi.service.book;

import com.cursojava.libraryapi.dto.book.CreateBookDTO;
import com.cursojava.libraryapi.model.author.AuthorModel;
import com.cursojava.libraryapi.model.book.BookModel;
import com.cursojava.libraryapi.repository.book.BookRepository;
import com.cursojava.libraryapi.validator.author.AuthorValidator;
import com.cursojava.libraryapi.validator.book.BookValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final AuthorValidator authorValidator;
    private final BookValidator bookValidator;

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

        return bookRepository.save(bookModel);
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
}
