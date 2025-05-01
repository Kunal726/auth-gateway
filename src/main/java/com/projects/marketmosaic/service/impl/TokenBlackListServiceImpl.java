package com.projects.marketmosaic.service.impl;

import com.projects.marketmosaic.service.TokenBlackListService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@Slf4j
public class TokenBlackListServiceImpl implements TokenBlackListService {
    // Token expiration time in milliseconds (24 hours)
    private static final long TOKEN_EXPIRATION_MS = 24 * 60 * 60 * 1000L;
    // Store blacklisted tokens with expiration time
    private final ConcurrentMap<String, Instant> blacklistedTokens = new ConcurrentHashMap<>();
    // Store user's active tokens: username -> Set of tokens
    private final ConcurrentMap<String, Set<String>> userTokens = new ConcurrentHashMap<>();

    @Override
    public void blacklistToken(String token) {
        blacklistedTokens.put(token, Instant.now().plusMillis(TOKEN_EXPIRATION_MS));
        // Remove token from user's active tokens
        userTokens.values().forEach(tokens -> tokens.remove(token));
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        Instant expirationTime = blacklistedTokens.get(token);
        if (expirationTime == null) {
            return false;
        }

        if (Instant.now().isAfter(expirationTime)) {
            blacklistedTokens.remove(token);
            return false;
        }

        return true;
    }

    @Override
    public void invalidateAllUserTokens(String username) {
        Set<String> tokens = userTokens.get(username);
        if (tokens != null) {
            Instant expirationTime = Instant.now().plusMillis(TOKEN_EXPIRATION_MS);
            tokens.forEach(token -> blacklistedTokens.put(token, expirationTime));
            userTokens.remove(username);
        }
    }

    @Override
    public void storeUserToken(String username, String token) {
        userTokens.computeIfAbsent(username, k -> ConcurrentHashMap.newKeySet()).add(token);
    }

    @Override
    public Set<String> getUserTokens(String username) {
        return new HashSet<>(userTokens.getOrDefault(username, new HashSet<>()));
    }

    @Scheduled(fixedRate = 60 * 60 * 1000) // Run every hour
    public void cleanupExpiredTokens() {
        Instant now = Instant.now();
        blacklistedTokens.entrySet().removeIf(entry -> now.isAfter(entry.getValue()));

        // Clean up empty user token sets
        userTokens.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }
}
