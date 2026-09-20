package com.vieira96.libraryapi.repository.user;

import com.vieira96.libraryapi.model.user.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserModel, UUID> {

    boolean existsByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    Optional<UserModel> findByEmailIgnoreCase(String email);
}
