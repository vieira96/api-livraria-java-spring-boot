package com.cursojava.libraryapi.service.author;

import com.cursojava.libraryapi.dto.author.CreateAuthorDTO;
import com.cursojava.libraryapi.dto.author.AuthorFiltersDTO;
import com.cursojava.libraryapi.exception.NotFoundException;
import com.cursojava.libraryapi.exception.author.AuthorAlreadyExistsException;
import com.cursojava.libraryapi.model.author.AuthorModel;
import com.cursojava.libraryapi.repository.author.AuthorRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthorService {
    private final AuthorRepository authorRepository;

    @Transactional
    public AuthorModel createAuthor(CreateAuthorDTO request) {
        validateAuthorDoesNotExist(request.name(), request.birthdate(), null);

        AuthorModel authorModel = new AuthorModel();
        authorModel.setName(request.name());
        authorModel.setBirthdate(request.birthdate());
        authorModel.setNationality(request.nationality());
        return authorRepository.save(authorModel);
    }

    @Transactional
    public AuthorModel updateAuthor(UUID id, CreateAuthorDTO request) {
        AuthorModel authorModel = this.getAuthorById(id);

        validateAuthorDoesNotExist(request.name(), request.birthdate(), id);

        authorModel.setName(request.name());
        authorModel.setBirthdate(request.birthdate());
        authorModel.setNationality(request.nationality());

        return authorRepository.save(authorModel);
    }

    public AuthorModel getAuthorById(UUID id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Autor não encontrado."));
    }

    public Page<AuthorModel> getAuthors(AuthorFiltersDTO filters) {
        Specification<AuthorModel> specification = Specification.unrestricted();

        if (filters.search() != null && !filters.search().isBlank()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("name")),
                            "%" + filters.search().toLowerCase(Locale.ROOT) + "%"
                    )
            );
        }

        if (filters.nationality() != null && !filters.nationality().isBlank()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(
                            criteriaBuilder.lower(root.get("nationality")),
                            filters.nationality().toLowerCase(Locale.ROOT)
                    )
            );
        }

        PageRequest pageable = PageRequest.of(
                filters.pageOrDefault(),
                filters.sizeOrDefault(),
                Sort.by(filters.directionOrDefault(), filters.sortByOrDefault().getProperty())
        );

        return authorRepository.findAll(specification, pageable);
    }

    @Transactional
    public void deleteAuthor(UUID id) {
        AuthorModel authorModel = this.getAuthorById(id);
        authorRepository.delete(authorModel);
    }

    private void validateAuthorDoesNotExist(String name, LocalDate birthdate, UUID currentAuthorId) {
        boolean authorAlreadyExists = currentAuthorId == null
                ? authorRepository.existsByNameAndBirthdate(name, birthdate)
                : authorRepository.existsByNameAndBirthdateAndIdNot(name, birthdate, currentAuthorId);

        if (authorAlreadyExists) {
            throw new AuthorAlreadyExistsException();
        }
    }
}
