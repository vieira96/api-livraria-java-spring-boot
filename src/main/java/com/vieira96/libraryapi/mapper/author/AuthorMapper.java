package com.vieira96.libraryapi.mapper.author;

import com.vieira96.libraryapi.dto.author.AuthorResponseDTO;
import com.vieira96.libraryapi.model.author.AuthorModel;

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
