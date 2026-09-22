package com.vieira96.libraryapi.repository.user;

import com.vieira96.libraryapi.model.user.UserModel;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserModel, UUID> {

    boolean existsByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    Optional<UserModel> findByEmailIgnoreCase(String email);

    // Só IDs, sem hidratar entidades. Quando existir o opt-in,
    // esta query ganha o filtro (ex.: WHERE notificationsEnabled = true).
    @Query("SELECT user.id FROM UserModel user")
    List<UUID> findAllIds();

    // Versão paginada para fan-out em lotes (Slice evita o COUNT do Page).
    @Query("SELECT user.id FROM UserModel user ORDER BY user.id")
    Slice<UUID> findIdsBy(Pageable pageable);
}
