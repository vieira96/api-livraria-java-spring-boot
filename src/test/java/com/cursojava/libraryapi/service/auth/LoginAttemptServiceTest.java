package com.cursojava.libraryapi.service.auth;

import com.cursojava.libraryapi.exception.auth.TooManyLoginAttemptsException;
import com.cursojava.libraryapi.support.IntegrationTestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class LoginAttemptServiceTest extends IntegrationTestContainer {

    private static final String KEY_PREFIX = "security:login-attempts:ip:";

    @Autowired
    private StringRedisTemplate redisTemplate;

    private LoginAttemptService service;

    @BeforeEach
    void setUp() {
        redisTemplate.execute((org.springframework.data.redis.core.RedisCallback<Object>) connection -> {
            var cursor = connection.scan(org.springframework.data.redis.core.ScanOptions.scanOptions().match(KEY_PREFIX + "*").count(100).build());
            while (cursor.hasNext()) {
                connection.keyCommands().del(cursor.next());
            }
            cursor.close();
            return null;
        });
        service = new LoginAttemptService(redisTemplate, true, 3, Duration.ofMinutes(5));
    }

    @Test
    void shouldAllowFirstAttemptAndSetKey() {
        service.consumeAttempt("192.168.1.1");

        assertThat(redisTemplate.opsForValue().get(keyFor("192.168.1.1"))).isEqualTo("1");
        assertThat(redisTemplate.getExpire(keyFor("192.168.1.1"))).isPositive();
    }

    @Test
    void shouldIncrementAttemptsOnSameIp() {
        service.consumeAttempt("10.0.0.1");
        service.consumeAttempt("10.0.0.1");
        service.consumeAttempt("10.0.0.1");

        assertThat(redisTemplate.opsForValue().get(keyFor("10.0.0.1"))).isEqualTo("3");
    }

    @Test
    void shouldBlockOnFourthAttemptWithMaxThree() {
        service.consumeAttempt("10.0.0.1");
        service.consumeAttempt("10.0.0.1");
        service.consumeAttempt("10.0.0.1");

        assertThatThrownBy(() -> service.consumeAttempt("10.0.0.1"))
                .isInstanceOf(TooManyLoginAttemptsException.class)
                .satisfies(e -> assertThat(((TooManyLoginAttemptsException) e).getRetryAfterSeconds()).isPositive());
    }

    @Test
    void shouldNotBlockWhenDisabled() {
        LoginAttemptService disabled = new LoginAttemptService(redisTemplate, false, 3, Duration.ofMinutes(5));

        for (int i = 0; i < 10; i++) {
            disabled.consumeAttempt("10.0.0.1");
        }

        assertThat(redisTemplate.hasKey(keyFor("10.0.0.1"))).isFalse();
    }

    @Test
    void shouldDecrementOnSuccessfulLogin() {
        service.consumeAttempt("10.0.0.1");
        service.consumeAttempt("10.0.0.1");

        service.releaseSuccessfulAttempt("10.0.0.1");

        assertThat(redisTemplate.opsForValue().get(keyFor("10.0.0.1"))).isEqualTo("1");
    }

    @Test
    void shouldDeleteKeyAfterSuccessfulLoginWithSingleAttempt() {
        service.consumeAttempt("10.0.0.1");

        service.releaseSuccessfulAttempt("10.0.0.1");

        assertThat(redisTemplate.hasKey(keyFor("10.0.0.1"))).isFalse();
    }

    @Test
    void shouldNotFailWhenReleasingNonExistentKey() {
        service.releaseSuccessfulAttempt("10.0.0.99");
    }

    @Test
    void shouldHaveIndependentCountersPerIp() {
        service.consumeAttempt("10.0.0.1");
        service.consumeAttempt("10.0.0.1");
        service.consumeAttempt("10.0.0.1");

        assertThatThrownBy(() -> service.consumeAttempt("10.0.0.1"))
                .isInstanceOf(TooManyLoginAttemptsException.class);

        service.consumeAttempt("10.0.0.2");
    }

    @Test
    void shouldRejectNullIp() {
        assertThatThrownBy(() -> service.consumeAttempt(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectBlankIp() {
        assertThatThrownBy(() -> service.consumeAttempt("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldResetAfterWindowExpires() throws InterruptedException {
        LoginAttemptService shortWindow = new LoginAttemptService(redisTemplate, true, 3, Duration.ofSeconds(1));
        shortWindow.consumeAttempt("10.0.0.1");
        shortWindow.consumeAttempt("10.0.0.1");

        Thread.sleep(1500);

        shortWindow.consumeAttempt("10.0.0.1");
        shortWindow.consumeAttempt("10.0.0.1");
        shortWindow.consumeAttempt("10.0.0.1");

        assertThatThrownBy(() -> shortWindow.consumeAttempt("10.0.0.1"))
                .isInstanceOf(TooManyLoginAttemptsException.class);
    }

    private String keyFor(String ip) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(ip.trim().toLowerCase(java.util.Locale.ROOT).getBytes(StandardCharsets.UTF_8));
            return KEY_PREFIX + HexFormat.of().formatHex(digest);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
