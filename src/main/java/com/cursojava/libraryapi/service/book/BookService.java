package com.cursojava.libraryapi.service.book;

import com.cursojava.libraryapi.dto.book.CreateBookDTO;
import com.cursojava.libraryapi.model.book.BookModel;
import org.springframework.stereotype.Service;

@Service
public class BookService {

    public BookModel createBook(CreateBookDTO request) {
        BookModel bookModel = new BookModel();
        bookModel.setTitle(request.title());
        bookModel.setIsbn(request.isbn());
        bookModel.setPublishDate(request.publishDate());
        bookModel.setGender(request.gender());
        bookModel.setPrice(request.price());
//        bookModel.setAuthorId(request.authorId());
        return bookModel;
    }
}
