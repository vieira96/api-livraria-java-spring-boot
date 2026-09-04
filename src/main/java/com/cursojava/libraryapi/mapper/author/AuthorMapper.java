package com.cursojava.libraryapi.mapper.author;

import com.cursojava.libraryapi.dto.author.AuthorResponseDTO;
import com.cursojava.libraryapi.model.author.AuthorModel;

public class AuthorMapper {
    public static AuthorResponseDTO toAuthorResponseDTO(AuthorModel authorModel) {
        return toAuthorResponseDTO(authorModel, null);
    }

    public static AuthorResponseDTO toAuthorResponseDTO(AuthorModel authorModel, Long bookCount) {
        return new AuthorResponseDTO(
                authorModel.getId(),
                authorModel.getName(),
                authorModel.getBirthdate(),
                authorModel.getNationality(),
                authorModel.getCreatedAt(),
                authorModel.getUpdatedAt(),
                bookCount
        );
    }
}
