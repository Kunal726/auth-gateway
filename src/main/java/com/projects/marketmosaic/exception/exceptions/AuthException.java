package com.projects.marketmosaic.exception.exceptions;

import com.projects.marketmosaic.constants.ErrorMessages;
import com.projects.marketmosaic.enums.AuthStatus;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AuthException extends RuntimeException {
    private final AuthStatus code;
    private final HttpStatus status;

    public AuthException(String message, AuthStatus code, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public static AuthException invalidToken(String message) {
        return new AuthException(message, AuthStatus.AUTH_001, HttpStatus.UNAUTHORIZED);
    }

    public static AuthException tokenExpired() {
        return new AuthException(ErrorMessages.TOKEN_EXPIRED, AuthStatus.AUTH_002, HttpStatus.UNAUTHORIZED);
    }

    public static AuthException invalidCredentials() {
        return new AuthException(ErrorMessages.INVALID_CREDENTIALS, AuthStatus.AUTH_003, HttpStatus.UNAUTHORIZED);
    }

    public static AuthException tokenBlacklisted() {
        return new AuthException(ErrorMessages.TOKEN_BLACKLISTED, AuthStatus.AUTH_004, HttpStatus.UNAUTHORIZED);
    }

    public static AuthException userNotFound() {
        return new AuthException(ErrorMessages.USER_NOT_FOUND, AuthStatus.AUTH_005, HttpStatus.NOT_FOUND);
    }

    public static AuthException unauthorized() {
        return new AuthException(ErrorMessages.UNAUTHORIZED_ACCESS, AuthStatus.AUTH_006, HttpStatus.FORBIDDEN);
    }

    public static AuthException emailInUse() {
        return new AuthException(ErrorMessages.EMAIL_IN_USE, AuthStatus.AUTH_007, HttpStatus.BAD_REQUEST);
    }
}