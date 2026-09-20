package com.vieira96.libraryapi.service.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RefreshTokenCookieService {

    public static final String COOKIE_NAME = "__Host-refresh_token";

    private final Duration expiration;

    public RefreshTokenCookieService(
            @Value("${security.refresh-token.expiration}") Duration expiration
    ) {
        this.expiration = expiration;
    }

    public ResponseCookie create(String refreshToken) {
        return baseCookie()
                .value(refreshToken)
                .maxAge(expiration)
                .build();
    }

    public ResponseCookie clear() {
        return baseCookie()
                .value("")
                .maxAge(Duration.ZERO)
                .build();
    }

    private ResponseCookie.ResponseCookieBuilder baseCookie() {
        return ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/");
    }
}
