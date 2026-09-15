package com.cursojava.libraryapi.service.auth;

import com.cursojava.libraryapi.dto.auth.RegisterUserDTO;
import com.cursojava.libraryapi.exception.auth.UserAlreadyExistsException;
import com.cursojava.libraryapi.model.role.RoleModel;
import com.cursojava.libraryapi.model.role.RoleName;
import com.cursojava.libraryapi.model.user.UserModel;
import com.cursojava.libraryapi.repository.role.RoleRepository;
import com.cursojava.libraryapi.repository.user.UserRepository;
import com.cursojava.libraryapi.validator.auth.AuthValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AuthValidator authValidator;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldRegisterUserWithNormalizedDataAndEncodedPassword() {
        RegisterUserDTO request = new RegisterUserDTO(
                "  Maria Silva  ",
                "  MARIA.SILVA@example.com  ",
                "strong-password"
        );
        RoleModel userRole = createUserRole();
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
        when(roleRepository.findByName(RoleName.USER)).thenReturn(Optional.of(userRole));

        when(userRepository.saveAndFlush(any(UserModel.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserModel registeredUser = authService.register(request);

        ArgumentCaptor<UserModel> userCaptor = ArgumentCaptor.forClass(UserModel.class);
        verify(authValidator).validateRegistration("maria.silva@example.com");
        verify(passwordEncoder).encode("strong-password");
        verify(userRepository).saveAndFlush(userCaptor.capture());

        UserModel savedUser = userCaptor.getValue();
        assertThat(savedUser.getName()).isEqualTo("Maria Silva");
        assertThat(savedUser.getEmail()).isEqualTo("maria.silva@example.com");
        assertThat(savedUser.getPassword()).isEqualTo("encoded-password");
        assertThat(savedUser.getRoles()).containsExactly(userRole);
        assertThat(registeredUser).isSameAs(savedUser);
    }

    @Test
    void shouldNotPersistUserWhenEmailIsAlreadyRegistered() {
        RegisterUserDTO request = new RegisterUserDTO(
                "Maria Silva",
                "MARIA.SILVA@example.com",
                "strong-password"
        );
        doThrow(new UserAlreadyExistsException())
                .when(authValidator).validateRegistration("maria.silva@example.com");

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Já existe um usuário cadastrado com este e-mail.");

        verifyNoInteractions(passwordEncoder, roleRepository, userRepository);
    }

    @Test
    void shouldTranslateDatabaseEmailConflictToDomainException() {
        RegisterUserDTO request = new RegisterUserDTO(
                "Maria Silva",
                "maria.silva@example.com",
                "strong-password"
        );
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
        when(roleRepository.findByName(RoleName.USER)).thenReturn(Optional.of(createUserRole()));
        when(userRepository.saveAndFlush(any(UserModel.class)))
                .thenThrow(new DataIntegrityViolationException("Unique constraint violation"));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Já existe um usuário cadastrado com este e-mail.");
    }

    private RoleModel createUserRole() {
        RoleModel role = new RoleModel();
        role.setName(RoleName.USER);
        return role;
    }
}
