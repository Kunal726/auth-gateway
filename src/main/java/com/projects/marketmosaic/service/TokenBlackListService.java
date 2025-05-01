package com.projects.marketmosaic.service;

import java.util.Set;

public interface TokenBlackListService {
    void blacklistToken(String token);

    boolean isTokenBlacklisted(String token);

    void invalidateAllUserTokens(String username);

    void storeUserToken(String username, String token);

    Set<String> getUserTokens(String username);
}
