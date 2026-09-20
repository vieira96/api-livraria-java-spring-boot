package com.vieira96.libraryapi.mapper.auth;

import com.vieira96.libraryapi.dto.auth.RegisteredUserResponseDTO;
import com.vieira96.libraryapi.model.role.RoleModel;
import com.vieira96.libraryapi.model.user.UserModel;

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
