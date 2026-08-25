package com.cursojava.libraryapi.repository.author;

import com.cursojava.libraryapi.model.author.AuthorModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.LocalDate;
import java.util.UUID;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
public class AuthorRepositoryTest {

    @Autowired
    AuthorRepository authorRepository;

    @Test
    public void salvarTest() {
        AuthorModel author = new AuthorModel();
        author.setName("J.K. Rowling");
        author.setBirthdate(LocalDate.of(1965, 7, 31));
        author.setNationality("British");

        AuthorModel authorSaved = authorRepository.save(author);

        System.out.println("Author saved: " + authorSaved);
    }

    @Test
    public void updateTest() {
        UUID id = UUID.fromString("c98e548a-d992-41d4-9cef-1365445e366f");
        AuthorModel author = authorRepository.findById(id).orElseThrow(() -> new RuntimeException("Author not found"));
        author.setName("J.K. Rowling Updated");
        AuthorModel authorUpdated = authorRepository.save(author);

        System.out.println("Author atualizado: " + authorUpdated);
    }
}
