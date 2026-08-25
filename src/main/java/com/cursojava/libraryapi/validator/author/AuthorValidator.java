package com.cursojava.libraryapi.validator.author;

import com.cursojava.libraryapi.dto.author.CreateAuthorDTO;
import com.cursojava.libraryapi.exception.NotFoundException;
import com.cursojava.libraryapi.exception.author.AuthorAlreadyExistsException;
import com.cursojava.libraryapi.model.author.AuthorModel;
import com.cursojava.libraryapi.repository.author.AuthorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuthorValidator {
    private final AuthorRepository authorRepository;

    public void validateAuthor(
            CreateAuthorDTO request,
            UUID currentAuthorId
    ) {
        boolean authorAlreadyExists = currentAuthorId == null
                ? authorRepository.existsByNameAndBirthdateAndNationality(request.name(), request.birthdate(), request.nationality())
                : authorRepository.existsByNameAndBirthdateAndNationalityAndIdNot(request.name(), request.birthdate(), request.nationality(), currentAuthorId);

        if (authorAlreadyExists) {
            throw new AuthorAlreadyExistsException();
        }
    }

    public AuthorModel authorExists(UUID authorId) {
        return authorRepository.findById(authorId)
                .orElseThrow(() -> new NotFoundException("Author not found with id: " + authorId));
    }
}
