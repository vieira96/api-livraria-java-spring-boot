package com.vieira96.libraryapi.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterUserDTO(
        @NotBlank(message = "Nome é obrigatório")
        @Size(min = 2, max = 100, message = "Nome deve ter entre {min} e {max} caracteres")
        String name,

        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "Informe um e-mail válido")
        @Size(max = 255, message = "E-mail deve ter no máximo {max} caracteres")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, max = 72, message = "Senha deve ter entre {min} e {max} caracteres")
        String password,

        @NotBlank(message = "Confirmação de senha é obrigatória")
        String confirmPassword
) {}
