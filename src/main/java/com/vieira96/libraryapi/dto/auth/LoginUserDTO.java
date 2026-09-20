package com.vieira96.libraryapi.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginUserDTO(
        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "Informe um e-mail válido")
        @Size(max = 255, message = "E-mail deve ter no máximo {max} caracteres")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        @Size(max = 72, message = "Senha deve ter no máximo {max} caracteres")
        String password
) {}
