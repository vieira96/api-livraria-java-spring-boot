package com.cursojava.libraryapi.repository.user;

import com.cursojava.libraryapi.model.user.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<UserModel, UUID> {

    boolean existsByEmail(String email);
}
