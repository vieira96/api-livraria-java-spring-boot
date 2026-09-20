package com.vieira96.libraryapi.repository.author;

import com.vieira96.libraryapi.model.author.AuthorModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.UUID;

public interface AuthorRepository extends JpaRepository<AuthorModel, UUID>, JpaSpecificationExecutor<AuthorModel> {
    boolean existsByNameAndBirthdateAndNationality(String name, LocalDate birthdate, String nationality);

    boolean existsByNameAndBirthdateAndNationalityAndIdNot(String name, LocalDate birthdate, String nationality, UUID id);
}
