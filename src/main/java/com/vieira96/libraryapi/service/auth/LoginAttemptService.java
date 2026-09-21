package com.vieira96.libraryapi.service.auth;

import com.vieira96.libraryapi.exception.auth.TooManyLoginAttemptsException;
import com.vieira96.libraryapi.exception.auth.LoginProtectionUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

@Service
@Slf4j
public class LoginAttemptService {

    private static final String KEY_PREFIX = "security:login-attempts:ip:";
    private static final RedisScript<List> CONSUME_ATTEMPT_SCRIPT = new DefaultRedisScript<>("""
            local attempts = redis.call('INCR', KEYS[1])
            if attempts == 1 then
                redis.call('PEXPIRE', KEYS[1], ARGV[1])
            end
            return {attempts, redis.call('PTTL', KEYS[1])}
            """, List.class);
    private static final RedisScript<Long> RELEASE_SUCCESSFUL_ATTEMPT_SCRIPT = new DefaultRedisScript<>("""
            local attempts = tonumber(redis.call('GET', KEYS[1]))
            if not attempts then
                return 0
            end
            if attempts <= 1 then
                redis.call('DEL', KEYS[1])
                return 0
            end
            return redis.call('DECR', KEYS[1])
            """, Long.class);

    private final StringRedisTemplate redisTemplate;
    private final boolean enabled;
    private final int maxAttempts;
    private final Duration window;

    public LoginAttemptService(
            StringRedisTemplate redisTemplate,
            @Value("${security.login-attempts.enabled}") boolean enabled,
            @Value("${security.login-attempts.max-attempts}") int maxAttempts,
            @Value("${security.login-attempts.window}") Duration window
    ) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("LOGIN_MAX_ATTEMPTS deve ser maior que zero.");
        }

        if (window.isZero() || window.isNegative()) {
            throw new IllegalArgumentException("LOGIN_ATTEMPT_WINDOW deve ser maior que zero.");
        }

        this.redisTemplate = redisTemplate;
        this.enabled = enabled;
        this.maxAttempts = maxAttempts;
        this.window = window;
    }

    public void consumeAttempt(String clientIp) {
        if (!enabled) {
            return;
        }

        final List<?> result;
        try {
            result = redisTemplate.execute(
                    CONSUME_ATTEMPT_SCRIPT,
                    List.of(keyFor(clientIp)),
                    Long.toString(window.toMillis())
            );
        } catch (DataAccessException exception) {
            throw new LoginProtectionUnavailableException(exception);
        }
        if (result == null || result.size() < 2) {
            throw new IllegalStateException("Redis não retornou o estado das tentativas de login.");
        }

        long attempts = ((Number) result.get(0)).longValue();
        long ttlMillis = ((Number) result.get(1)).longValue();
        if (attempts > maxAttempts) {
            long retryAfterSeconds = Math.max(1, (ttlMillis + 999) / 1000);
            log.warn("Rate-limit de login atingido. ip={}, tentativas={}, retryAfter={}s", clientIp, attempts, retryAfterSeconds);
            throw new TooManyLoginAttemptsException(retryAfterSeconds);
        }
    }

    public void releaseSuccessfulAttempt(String clientIp) {
        if (enabled) {
            try {
                redisTemplate.execute(
                        RELEASE_SUCCESSFUL_ATTEMPT_SCRIPT,
                        List.of(keyFor(clientIp))
                );
            } catch (DataAccessException exception) {
                throw new LoginProtectionUnavailableException(exception);
            }
        }
    }

    private String keyFor(String clientIp) {
        if (clientIp == null || clientIp.isBlank()) {
            throw new IllegalArgumentException("Endereço IP do cliente não informado.");
        }

        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(clientIp.trim().toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8));
            return KEY_PREFIX + HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 não está disponível.", exception);
        }
    }
}
