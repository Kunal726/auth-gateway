package com.projects.marketmosaic.utils;

import com.projects.marketmosaic.constants.ErrorMessages;
import com.projects.marketmosaic.config.security.LoginAttemptTracker;
import com.projects.marketmosaic.entity.PasswordResetTokenEntity;
import com.projects.marketmosaic.entity.UserEntity;
import com.projects.marketmosaic.exception.exceptions.AuthException;
import com.projects.marketmosaic.repositories.PasswordResetTokenRepository;
import com.projects.marketmosaic.repositories.UserRepository;
import com.projects.marketmosaic.service.TokenBlackListService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SecurityUtils {
    private final UserRepository userRepository;
    private final JWTUtils jwtUtils;
    private final CookieUtils cookieUtils;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlackListService tokenBlackListService;
    private final LoginAttemptTracker loginAttemptTracker;

    public void validateUser(String username, HttpServletRequest request) {
        String token = cookieUtils.extractJwtFromCookies(request);
        if (token == null) {
            throw AuthException.invalidToken(ErrorMessages.NO_TOKEN_FOUND);
        }

        String currentUsername = jwtUtils.extractUsername(token);
        if (!username.equals(currentUsername) && !isAdmin(currentUsername)) {
            throw AuthException.unauthorized();
        }
    }

    public UserEntity getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(AuthException::userNotFound);
    }

    public boolean isAdmin(String username) {
        UserEntity user = getUserByUsername(username);
        return user.getRoles() != null && user.getRoles().contains("ADMIN");
    }

    public String validateAndExtractUsername(HttpServletRequest request) {
        String token = cookieUtils.extractJwtFromCookies(request);
        if (token == null) {
            throw AuthException.invalidToken(ErrorMessages.NO_TOKEN_FOUND);
        }
        return jwtUtils.extractUsername(token);
    }

    @Transactional
    @Modifying
    public void updatePasswordAndDeleteToken(PasswordResetTokenEntity resetToken, String newPassword) {
        UserEntity user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        resetTokenRepository.delete(resetToken);

        // Reset login attempts after successful password reset
        loginAttemptTracker.clearAttempts(user.getUsername());

        // Invalidate all existing sessions for security
        tokenBlackListService.invalidateAllUserTokens(user.getUsername());
    }

}