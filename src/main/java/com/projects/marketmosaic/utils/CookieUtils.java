package com.projects.marketmosaic.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CookieUtils {

    @Value("${jwt.cookie.name:JWT_SESSION}")
    private String jwtCookieName;

    @Value("${jwt.cookie.max-age:86400}") // 24 hours in seconds
    private int cookieMaxAge;

    public void createJwtCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(jwtCookieName, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // Only transmitted over HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(cookieMaxAge);
        response.addCookie(cookie);
    }

    public void clearJwtCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(jwtCookieName, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    public String extractJwtFromCookies(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (jwtCookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}