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
            @PathVariable("id") UUID authorId,
            @Valid @ModelAttribute AuthorFiltersDTO filters
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(authorService.getAuthor(authorId, filters));
    }

    @GetMapping
    public ResponseEntity<PageResponseDTO<AuthorResponseDTO>> getAuthors(
            @Valid @ModelAttribute AuthorFiltersDTO filters
    ) {
        Page<AuthorResponseDTO> authors = authorService.getAuthors(filters);

        return ResponseEntity.ok(PageResponseDTO.from(authors));
    }

    @PutMapping("/{id}")
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
    public ResponseEntity<Void> deleteAuthor(@PathVariable("id") UUID authorId) {
        authorService.deleteAuthor(authorId);

        return ResponseEntity.noContent().build();
    }
}
