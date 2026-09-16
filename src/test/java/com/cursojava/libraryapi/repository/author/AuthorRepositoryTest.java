package com.cursojava.libraryapi.repository.author;

import com.cursojava.libraryapi.config.AuditingConfiguration;
import com.cursojava.libraryapi.model.author.AuthorModel;
import com.cursojava.libraryapi.support.IntegrationTestContainer;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(AuditingConfiguration.class)
class AuthorRepositoryTest extends IntegrationTestContainer {

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldSaveAuthor() {
        AuthorModel author = createAuthor();

        AuthorModel authorSaved = authorRepository.saveAndFlush(author);

        assertThat(authorSaved.getId()).isNotNull();
        assertThat(authorSaved.getCreatedAt()).isNotNull();
        assertThat(authorSaved.getUpdatedAt()).isNotNull();
        assertThat(authorRepository.findById(authorSaved.getId())).isPresent();
    }

    @Test
    void shouldUpdateAuthor() {
        AuthorModel author = createAuthor();
        AuthorModel authorSaved = authorRepository.saveAndFlush(author);

        authorSaved.setName("J.K. Rowling Updated");
        authorRepository.saveAndFlush(authorSaved);
        entityManager.clear();

        AuthorModel authorUpdated = authorRepository.findById(authorSaved.getId()).orElseThrow();

        assertThat(authorUpdated.getName()).isEqualTo("J.K. Rowling Updated");
        assertThat(authorUpdated.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldCheckIfAuthorExistsByNameBirthdateAndNationality() {
        AuthorModel author = authorRepository.saveAndFlush(createAuthor());

        boolean exists = authorRepository.existsByNameAndBirthdateAndNationality(
                author.getName(),
                author.getBirthdate(),
                author.getNationality()
        );

        assertThat(exists).isTrue();
    }

    @Test
    void shouldCheckIfAnotherAuthorExistsWithSameData() {
        AuthorModel author = authorRepository.saveAndFlush(createAuthor());

        boolean existsWithDifferentId = authorRepository.existsByNameAndBirthdateAndNationalityAndIdNot(
                author.getName(),
                author.getBirthdate(),
                author.getNationality(),
                UUID.randomUUID()
        );
        boolean existsWithSameId = authorRepository.existsByNameAndBirthdateAndNationalityAndIdNot(
                author.getName(),
                author.getBirthdate(),
                author.getNationality(),
                author.getId()
        );

        assertThat(existsWithDifferentId).isTrue();
        assertThat(existsWithSameId).isFalse();
    }

    private AuthorModel createAuthor() {
        AuthorModel author = new AuthorModel();
        author.setName("J.K. Rowling");
        author.setBirthdate(LocalDate.of(1965, 7, 31));
        author.setNationality("British");
        return author;
    }
}
