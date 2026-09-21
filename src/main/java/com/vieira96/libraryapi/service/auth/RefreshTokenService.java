package com.vieira96.libraryapi.service.auth;

import com.vieira96.libraryapi.exception.auth.InvalidRefreshTokenException;
import com.vieira96.libraryapi.model.auth.RefreshTokenModel;
import com.vieira96.libraryapi.model.user.UserModel;
import com.vieira96.libraryapi.repository.auth.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
@Slf4j
public class RefreshTokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();

    private final RefreshTokenRepository refreshTokenRepository;
    private final Duration expiration;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${security.refresh-token.expiration}") Duration expiration
    ) {
        if (expiration.isZero() || expiration.isNegative()) {
            throw new IllegalArgumentException("REFRESH_TOKEN_EXPIRATION deve ser maior que zero.");
        }

        this.refreshTokenRepository = refreshTokenRepository;
        this.expiration = expiration;
    }

    public IssuedRefreshToken issue(UserModel user) {
        return issue(user, UUID.randomUUID());
    }

    public RotatedRefreshToken rotate(String rawToken) {
        RefreshTokenModel currentToken = refreshTokenRepository.findForUpdateByTokenHash(hash(rawToken))
                .orElseThrow(InvalidRefreshTokenException::new);
        Instant now = Instant.now();

        if (currentToken.getRevokedAt() != null) {
            log.warn("REUSE DETECTED: Refresh token já revogado utilizado. Revogando família inteira. familyId={}, userId={}, tokenId={}",
                    currentToken.getFamilyId(), currentToken.getUser().getId(), currentToken.getId());
            refreshTokenRepository.revokeActiveFamily(currentToken.getFamilyId(), now);
            throw new InvalidRefreshTokenException();
        }

        if (!currentToken.getExpiresAt().isAfter(now)) {
            log.warn("Refresh token expirado utilizado. userId={}, tokenId={}", currentToken.getUser().getId(), currentToken.getId());
            currentToken.setRevokedAt(now);
            throw new InvalidRefreshTokenException();
        }

        log.debug("Refresh token rotacionado com sucesso. userId={}, familyId={}", currentToken.getUser().getId(), currentToken.getFamilyId());
        currentToken.setRevokedAt(now);
        IssuedRefreshToken newToken = issue(currentToken.getUser(), currentToken.getFamilyId());

        return new RotatedRefreshToken(currentToken.getUser(), newToken.value());
    }

    public void revoke(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }

        refreshTokenRepository.findForUpdateByTokenHash(hash(rawToken))
                .ifPresent(token -> refreshTokenRepository.revokeActiveFamily(token.getFamilyId(), Instant.now()));
    }

    private IssuedRefreshToken issue(UserModel user, UUID familyId) {
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);
        String rawToken = BASE64_URL_ENCODER.encodeToString(randomBytes);

        RefreshTokenModel refreshToken = new RefreshTokenModel();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(hash(rawToken));
        refreshToken.setFamilyId(familyId);
        refreshToken.setExpiresAt(Instant.now().plus(expiration));
        refreshTokenRepository.save(refreshToken);

        return new IssuedRefreshToken(rawToken);
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 não está disponível.", exception);
        }
    }

    public void revokeAllForUser(UserModel user) {
        refreshTokenRepository.deleteAllByUser_Id(user.getId());
    }

    public record IssuedRefreshToken(String value) {}

    public record RotatedRefreshToken(UserModel user, String value) {}
}
