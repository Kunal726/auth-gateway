package com.projects.marketmosaic.config.security;

import com.projects.marketmosaic.common.utils.RedisManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Component
public class LoginAttemptTracker {
    private final RedisManager redisManager;
    private static final String LOGIN_ATTEMPTS_KEY_PREFIX = "login:attempts:";
    private static final String LOCKOUT_TIME_KEY_PREFIX = "login:lockout:";

    @Value("${auth.login.max-attempts:5}")
    private int maxAttempts;

    @Value("${auth.login.lockout-duration-minutes:30}")
    private int lockoutDurationMinutes;

    public LoginAttemptTracker(RedisManager redisManager) {
        this.redisManager = redisManager;
    }

    public void recordFailedLogin(String username) {
        String attemptsKey = LOGIN_ATTEMPTS_KEY_PREFIX + username;
        String lockoutKey = LOCKOUT_TIME_KEY_PREFIX + username;

        // Increment attempts counter
        Long attempts = redisManager.increment(attemptsKey);
        if (attempts == null) {
            attempts = 1L;
        }

        // Set expiration for both keys
        redisManager.expire(attemptsKey, lockoutDurationMinutes, TimeUnit.MINUTES);
        redisManager.expire(lockoutKey, lockoutDurationMinutes, TimeUnit.MINUTES);

        // If max attempts reached, set lockout time
        if (attempts >= maxAttempts) {
            LocalDateTime lockoutTime = LocalDateTime.now().plusMinutes(lockoutDurationMinutes);
            redisManager.set(lockoutKey, lockoutTime.format(DateTimeFormatter.ISO_DATE_TIME));
        }
    }

    public void recordSuccessfulLogin(String username) {
        String attemptsKey = LOGIN_ATTEMPTS_KEY_PREFIX + username;
        String lockoutKey = LOCKOUT_TIME_KEY_PREFIX + username;

        redisManager.delete(attemptsKey);
        redisManager.delete(lockoutKey);
    }

    public boolean isAccountLocked(String username) {
        String lockoutKey = LOCKOUT_TIME_KEY_PREFIX + username;
        String lockoutTimeStr = (String) redisManager.get(lockoutKey);

        if (lockoutTimeStr == null) {
            return false;
        }

        LocalDateTime lockoutTime = LocalDateTime.parse(lockoutTimeStr, DateTimeFormatter.ISO_DATE_TIME);
        return LocalDateTime.now().isBefore(lockoutTime);
    }

    public LocalDateTime getLockoutTime(String username) {
        String lockoutKey = LOCKOUT_TIME_KEY_PREFIX + username;
        String lockoutTimeStr = (String) redisManager.get(lockoutKey);

        if (lockoutTimeStr == null) {
            return null;
        }

        return LocalDateTime.parse(lockoutTimeStr, DateTimeFormatter.ISO_DATE_TIME);
    }

    public void clearAttempts(String username) {
        String attemptsKey = LOGIN_ATTEMPTS_KEY_PREFIX + username;
        String lockoutKey = LOCKOUT_TIME_KEY_PREFIX + username;

        redisManager.delete(attemptsKey);
        redisManager.delete(lockoutKey);
    }
}