package com.cursojava.libraryapi.repository.author;

import com.cursojava.libraryapi.config.AuditingConfiguration;
import com.cursojava.libraryapi.model.author.AuthorModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(AuditingConfiguration.class)
public class AuthorRepositoryTest {

    @Autowired
    AuthorRepository authorRepository;

    @Test
    public void shouldSaveAuthor() {
        AuthorModel author = new AuthorModel();
        author.setName("J.K. Rowling");
        author.setBirthdate(LocalDate.of(1965, 7, 31));
        author.setNationality("British");

        AuthorModel authorSaved = authorRepository.saveAndFlush(author);

        assertThat(authorSaved.getId()).isNotNull();
        assertThat(authorSaved.getCreatedAt()).isNotNull();
        assertThat(authorSaved.getUpdatedAt()).isNotNull();
        assertThat(authorRepository.findById(authorSaved.getId())).isPresent();
    }

    @Test
    public void shouldUpdateAuthor() {
        AuthorModel author = new AuthorModel();
        author.setName("J.K. Rowling");
        author.setBirthdate(LocalDate.of(1965, 7, 31));
        author.setNationality("British");
        AuthorModel authorSaved = authorRepository.saveAndFlush(author);

        authorSaved.setName("J.K. Rowling Updated");
        AuthorModel authorUpdated = authorRepository.saveAndFlush(authorSaved);

        assertThat(authorUpdated.getName()).isEqualTo("J.K. Rowling Updated");
        assertThat(authorUpdated.getUpdatedAt()).isNotNull();
    }
}
