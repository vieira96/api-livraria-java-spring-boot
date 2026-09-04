package com.cursojava.libraryapi.controller.author;

import com.cursojava.libraryapi.dto.author.AuthorFiltersDTO;
import com.cursojava.libraryapi.dto.author.AuthorResponseDTO;
import com.cursojava.libraryapi.dto.author.CreateAuthorDTO;
import com.cursojava.libraryapi.dto.global.PageResponseDTO;
import com.cursojava.libraryapi.mapper.author.AuthorMapper;
import com.cursojava.libraryapi.model.author.AuthorModel;
import com.cursojava.libraryapi.service.author.AuthorService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
public class AuthorController {
    private final AuthorService authorService;

    @PostMapping
    public ResponseEntity<AuthorResponseDTO> createAuthor(@Valid @RequestBody CreateAuthorDTO request) {
        AuthorModel author = authorService.createAuthor(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(AuthorMapper.toAuthorResponseDTO(author));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthorResponseDTO> getAuthor(
            @PathVariable String id,
            @Valid @ModelAttribute AuthorFiltersDTO filters
    ) {
        UUID authorId = UUID.fromString(id);
        AuthorModel author = authorService.getAuthorById(authorId);
        Long bookCount = filters.includesBookCount()
                ? authorService.getBookCountsByAuthorIds(List.of(authorId))
                        .getOrDefault(authorId, 0L)
                : null;

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(AuthorMapper.toAuthorResponseDTO(author, bookCount));
    }

    @GetMapping
    public ResponseEntity<PageResponseDTO<AuthorResponseDTO>> getAuthors(
            @Valid @ModelAttribute AuthorFiltersDTO filters
    ) {
        Page<AuthorModel> authorPage = authorService.getAuthors(filters);
        Map<UUID, Long> bookCounts = filters.includesBookCount()
                ? authorService.getBookCountsByAuthorIds(
                        authorPage.getContent().stream().map(AuthorModel::getId).toList()
                )
                : Map.of();

        Page<AuthorResponseDTO> authors = authorPage.map(author ->
                AuthorMapper.toAuthorResponseDTO(
                        author,
                        filters.includesBookCount() ? bookCounts.getOrDefault(author.getId(), 0L) : null
                )
        );

        return ResponseEntity.ok(PageResponseDTO.from(authors));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AuthorResponseDTO> updateAuthor(@Valid @PathVariable String id, @RequestBody CreateAuthorDTO request) {
        UUID authorId = UUID.fromString(id);
        AuthorModel updatedAuthor = authorService.updateAuthor(authorId, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(AuthorMapper.toAuthorResponseDTO(updatedAuthor));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable String id) {
        UUID authorId = UUID.fromString(id);
        authorService.deleteAuthor(authorId);

        return ResponseEntity.noContent().build();
    }
}
