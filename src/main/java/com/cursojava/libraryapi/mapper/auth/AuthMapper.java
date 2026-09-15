package com.cursojava.libraryapi.mapper.auth;

import com.cursojava.libraryapi.dto.auth.RegisteredUserResponseDTO;
import com.cursojava.libraryapi.model.role.RoleModel;
import com.cursojava.libraryapi.model.user.UserModel;

public final class AuthMapper {

    private AuthMapper() {
    }

    public static RegisteredUserResponseDTO toRegisteredUserResponseDTO(UserModel user) {
        return new RegisteredUserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRoles().stream()
                        .map(RoleModel::getName)
                        .map(Enum::name)
                        .sorted()
                        .toList(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
