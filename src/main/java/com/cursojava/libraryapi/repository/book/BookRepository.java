package com.cursojava.libraryapi.repository.book;

import com.cursojava.libraryapi.model.book.BookModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface BookRepository extends JpaRepository<BookModel, UUID>, JpaSpecificationExecutor<BookModel> {}
