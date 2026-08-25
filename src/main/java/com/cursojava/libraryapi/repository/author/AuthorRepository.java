package com.cursojava.libraryapi.repository.author;

import com.cursojava.libraryapi.model.author.AuthorModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.UUID;

public interface AuthorRepository extends JpaRepository<AuthorModel, UUID>, JpaSpecificationExecutor<AuthorModel> {
    boolean existsByNameAndBirthdate(String name, LocalDate birthdate);

    boolean existsByNameAndBirthdateAndIdNot(String name, LocalDate birthdate, UUID id);
}
