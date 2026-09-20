package com.vieira96.libraryapi.validator.auth;

import com.vieira96.libraryapi.exception.auth.PasswordsDoNotMatchException;
import com.vieira96.libraryapi.exception.auth.UserAlreadyExistsException;
import com.vieira96.libraryapi.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthValidator {

    private final UserRepository userRepository;

    public void validateRegistration(String email) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new UserAlreadyExistsException();
        }
    }

    public void validatePasswordMatch(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new PasswordsDoNotMatchException();
        }
    }
}
