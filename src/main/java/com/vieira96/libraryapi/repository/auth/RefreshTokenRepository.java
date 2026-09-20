package com.vieira96.libraryapi.repository.auth;

import com.vieira96.libraryapi.model.auth.RefreshTokenModel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenModel, UUID> {

    @Transactional
    void deleteAllByUser_Id(UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select token from RefreshTokenModel token where token.tokenHash = :tokenHash")
    Optional<RefreshTokenModel> findForUpdateByTokenHash(@Param("tokenHash") String tokenHash);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update RefreshTokenModel token
               set token.revokedAt = :revokedAt
             where token.familyId = :familyId
               and token.revokedAt is null
            """)
    int revokeActiveFamily(@Param("familyId") UUID familyId, @Param("revokedAt") Instant revokedAt);
}
