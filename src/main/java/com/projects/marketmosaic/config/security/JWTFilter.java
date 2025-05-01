package com.projects.marketmosaic.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projects.marketmosaic.common.dto.resp.BaseRespDTO;
import com.projects.marketmosaic.exception.exceptions.AuthException;
import com.projects.marketmosaic.service.TokenBlackListService;
import com.projects.marketmosaic.utils.CookieUtils;
import com.projects.marketmosaic.utils.JWTUtils;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {
    private final JWTUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;
    private final TokenBlackListService tokenBlackListService;
    private final CookieUtils cookieUtils;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String token = null;

            // First try to get token from cookie
            token = cookieUtils.extractJwtFromCookies(request);

            // If not in cookie, try Authorization header
            if (token == null) {
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = jwtUtils.extractTokenFromHeader(authHeader);
                }
            }

            if (token != null) {
                // Check if token is blacklisted
                if (tokenBlackListService.isTokenBlacklisted(token)) {
                    handleAuthError(response, AuthException.tokenBlacklisted());
                    return;
                }

                String username = jwtUtils.extractUsername(token);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (Boolean.TRUE.equals(jwtUtils.validateToken(token, userDetails))) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            }
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            handleAuthError(response, AuthException.tokenExpired());
        } catch (JwtException e) {
            handleAuthError(response, AuthException.invalidToken(e.getMessage()));
        }
    }

    private void handleAuthError(HttpServletResponse response, AuthException ex) throws IOException {
        response.setStatus(ex.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        BaseRespDTO errorResponse = new BaseRespDTO();
        errorResponse.setCode(ex.getCode().name());
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setStatus(false);

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}