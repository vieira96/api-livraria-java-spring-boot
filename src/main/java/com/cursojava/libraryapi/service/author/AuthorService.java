package com.cursojava.libraryapi.service.author;

import com.cursojava.libraryapi.dto.author.CreateAuthorDTO;
import com.cursojava.libraryapi.dto.author.AuthorFiltersDTO;
import com.cursojava.libraryapi.dto.author.AuthorResponseDTO;
import com.cursojava.libraryapi.mapper.author.AuthorMapper;
import com.cursojava.libraryapi.model.author.AuthorModel;
import com.cursojava.libraryapi.repository.author.AuthorRepository;
import com.cursojava.libraryapi.repository.book.BookRepository;
import com.cursojava.libraryapi.validator.author.AuthorValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthorService {
    private static final Set<String> SEARCHABLE_FIELDS = Set.of(
            "name",
            "nationality"
    );

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    private final AuthorValidator authorValidator;

    @Transactional
    public AuthorModel createAuthor(CreateAuthorDTO request) {
        authorValidator.validateAuthor(
                request,
                null
        );
        AuthorModel authorModel = new AuthorModel();
        authorModel.setName(request.name());
        authorModel.setBirthdate(request.birthdate());
        authorModel.setNationality(request.nationality());
        return authorRepository.save(authorModel);
    }

    public AuthorResponseDTO getAuthor(UUID id, AuthorFiltersDTO filters) {
        AuthorModel author = authorValidator.authorExists(id);
        Long bookCount = filters.includesBookCount()
                ? getBookCountsByAuthorIds(List.of(id)).getOrDefault(id, 0L)
                : null;

        return AuthorMapper.toAuthorResponseDTO(author, bookCount);
    }

    @Transactional
    public AuthorModel updateAuthor(UUID id, CreateAuthorDTO request) {
        AuthorModel authorModel = this.authorValidator.authorExists(id);

        authorValidator.validateAuthor(
                request,
                id
        );

        authorModel.setName(request.name());
        authorModel.setBirthdate(request.birthdate());
        authorModel.setNationality(request.nationality());

        return authorRepository.save(authorModel);
    }

    public Page<AuthorResponseDTO> getAuthors(AuthorFiltersDTO filters) {
        Specification<AuthorModel> specification = Specification.unrestricted();

        if (filters.search() != null && !filters.search().isBlank()) {
            specification = specification.and(searchBy(filters.search()));
        }

        if(filters.name() != null && !filters.name().isBlank()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("name")),
                            "%" + filters.name().toLowerCase(Locale.ROOT) + "%"
                    )
            );
        }

        if (filters.nationality() != null && !filters.nationality().isBlank()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("nationality")),
                            "%" + filters.nationality().toLowerCase(Locale.ROOT) + "%"
                    )
            );
        }

        PageRequest pageable = PageRequest.of(
                filters.pageOrDefault(),
                filters.sizeOrDefault(),
                Sort.by(filters.directionOrDefault(), filters.sortByOrDefault().getProperty())
        );

        Page<AuthorModel> authorPage = authorRepository.findAll(specification, pageable);
        Map<UUID, Long> bookCounts = filters.includesBookCount()
                ? getBookCountsByAuthorIds(
                        authorPage.getContent().stream().map(AuthorModel::getId).toList()
                )
                : Map.of();

        return authorPage.map(author -> AuthorMapper.toAuthorResponseDTO(
                author,
                filters.includesBookCount() ? bookCounts.getOrDefault(author.getId(), 0L) : null
        ));
    }

    private Map<UUID, Long> getBookCountsByAuthorIds(Collection<UUID> authorIds) {
        if (authorIds.isEmpty()) {
            return Map.of();
        }

        return bookRepository.countBooksByAuthorIds(authorIds).stream()
                .collect(java.util.stream.Collectors.toMap(
                        projection -> projection.getAuthorId(),
                        projection -> projection.getBookCount()
                ));
    }

    @Transactional
    public void deleteAuthor(UUID id) {
        AuthorModel authorModel = this.authorValidator.authorExists(id);
        authorValidator.validateDeleteAuthor(authorModel.getId());
        authorRepository.delete(authorModel);
    }

    private Specification<AuthorModel> searchBy(String search) {
        return (root, query, criteriaBuilder) -> {
            String term = "%" + search.toLowerCase(Locale.ROOT) + "%";

            return criteriaBuilder.or(
                    SEARCHABLE_FIELDS.stream()
                            .map(field -> criteriaBuilder.like(
                                    criteriaBuilder.lower(root.get(field)),
                                    term
                            ))
                            .toArray(jakarta.persistence.criteria.Predicate[]::new)
            );
        };
    }
}
