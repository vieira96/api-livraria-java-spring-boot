package com.vieira96.libraryapi.controller.author;

import com.vieira96.libraryapi.dto.author.AuthorFiltersDTO;
import com.vieira96.libraryapi.dto.author.AuthorResponseDTO;
import com.vieira96.libraryapi.dto.author.CreateAuthorDTO;
import com.vieira96.libraryapi.dto.global.PageResponseDTO;
import com.vieira96.libraryapi.mapper.author.AuthorMapper;
import com.vieira96.libraryapi.model.author.AuthorModel;
import com.vieira96.libraryapi.service.author.AuthorService;
import org.springdoc.core.annotations.ParameterObject;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
public class AuthorController {
    private final AuthorService authorService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthorResponseDTO> createAuthor(@Valid @RequestBody CreateAuthorDTO request) {
        AuthorModel author = authorService.createAuthor(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(AuthorMapper.toAuthorResponseDTO(author));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthorResponseDTO> getAuthor(
            @PathVariable("id") UUID authorId,
            @ParameterObject @Valid @ModelAttribute AuthorFiltersDTO filters
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(authorService.getAuthor(authorId, filters));
    }

    @GetMapping
    public ResponseEntity<PageResponseDTO<AuthorResponseDTO>> getAuthors(
            @ParameterObject @Valid @ModelAttribute AuthorFiltersDTO filters
    ) {
        Page<AuthorResponseDTO> authors = authorService.getAuthors(filters);

        return ResponseEntity.ok(PageResponseDTO.from(authors));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthorResponseDTO> updateAuthor(
            @PathVariable("id") UUID authorId,
            @Valid @RequestBody CreateAuthorDTO request
    ) {
        AuthorModel updatedAuthor = authorService.updateAuthor(authorId, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(AuthorMapper.toAuthorResponseDTO(updatedAuthor));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAuthor(@PathVariable("id") UUID authorId) {
        authorService.deleteAuthor(authorId);

        return ResponseEntity.noContent().build();
    }
}
