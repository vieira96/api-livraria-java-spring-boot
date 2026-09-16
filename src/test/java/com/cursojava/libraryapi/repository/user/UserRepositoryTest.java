package com.cursojava.libraryapi.repository.user;

import com.cursojava.libraryapi.config.AuditingConfiguration;
import com.cursojava.libraryapi.model.user.UserModel;
import com.cursojava.libraryapi.support.IntegrationTestContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(AuditingConfiguration.class)
class UserRepositoryTest extends IntegrationTestContainer {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveUser() {
        UserModel user = createUser("maria.silva@example.com");

        UserModel savedUser = userRepository.saveAndFlush(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
        assertThat(userRepository.findById(savedUser.getId())).isPresent();
    }

    @Test
    void shouldCheckIfEmailExists() {
        UserModel user = createUser("maria.silva@example.com");
        userRepository.saveAndFlush(user);

        assertThat(userRepository.existsByEmail("maria.silva@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("unknown@example.com")).isFalse();
    }

    @Test
    void shouldNotAllowDuplicateEmail() {
        userRepository.saveAndFlush(createUser("maria.silva@example.com"));

        assertThatThrownBy(() -> userRepository.saveAndFlush(createUser("maria.silva@example.com")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private UserModel createUser(String email) {
        UserModel user = new UserModel();
        user.setName("Maria Silva");
        user.setEmail(email);
        user.setPassword("hashed-password");
        return user;
    }
}
