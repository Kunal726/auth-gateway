package com.projects.marketmosaic.service.impl;

import com.projects.marketmosaic.common.dto.resp.BaseRespDTO;
import com.projects.marketmosaic.common.dto.resp.TokenValidationRespDTO;
import com.projects.marketmosaic.config.security.CustomUserDetails;
import com.projects.marketmosaic.config.security.LoginAttemptTracker;
import com.projects.marketmosaic.constants.ErrorMessages;
import com.projects.marketmosaic.dtos.*;
import com.projects.marketmosaic.entity.PasswordResetTokenEntity;
import com.projects.marketmosaic.entity.SellerEntity;
import com.projects.marketmosaic.entity.UserEntity;
import com.projects.marketmosaic.enums.AuthStatus;
import com.projects.marketmosaic.enums.SellerStatus;
import com.projects.marketmosaic.exception.exceptions.AuthException;
import com.projects.marketmosaic.repositories.PasswordResetTokenRepository;
import com.projects.marketmosaic.repositories.UserRepository;
import com.projects.marketmosaic.service.AuthService;
import com.projects.marketmosaic.service.TokenBlackListService;
import com.projects.marketmosaic.utils.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTUtils jwtUtils;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final LoginAttemptTracker loginAttemptTracker;
    private final SecurityUtils securityUtils;
    private final TokenBlackListService tokenBlackListService;
    private final CookieUtils cookieUtils;
    private final EmailUtils emailUtils;

    @Override
    @Transactional
    public BaseRespDTO loginUser(LoginReqDTO loginReqDTO, HttpServletResponse response) {
        BaseRespDTO respDTO = new BaseRespDTO();
        try {
            // Check if account is locked
            if (loginAttemptTracker.isAccountLocked(loginReqDTO.getUsername())) {
                return getLockoutResponse(loginReqDTO.getUsername());
            }

            Authentication authenticated = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginReqDTO.getUsername(), loginReqDTO.getPassword()));

            // Reset attempts on successful login
            loginAttemptTracker.recordSuccessfulLogin(loginReqDTO.getUsername());

            UserDetails userDetails = (CustomUserDetails) authenticated.getPrincipal();
            UserEntity user = securityUtils.getUserByUsername(loginReqDTO.getUsername());

            // Generate the JWT token with user details
            String token = jwtUtils.generateToken(userDetails, user);

            // Store the token for session management
            tokenBlackListService.storeUserToken(loginReqDTO.getUsername(), token);

            cookieUtils.createJwtCookie(response, token);

            SecurityContextHolder.getContext().setAuthentication(authenticated);

            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            respDTO.setStatus(authenticated.isAuthenticated());
            respDTO.setMessage("Login successful");
            respDTO.setCode("200");
        } catch (BadCredentialsException e) {
            // Record failed attempt
            loginAttemptTracker.recordFailedLogin(loginReqDTO.getUsername());
            log.warn("Failed login attempt for user: {}", loginReqDTO.getUsername());
            throw AuthException.invalidCredentials();
        }

        return respDTO;
    }

    @Override
    public BaseRespDTO logout(String authHeader, HttpServletRequest request, HttpServletResponse response) {
        // First try to get token from cookie
        String token = cookieUtils.extractJwtFromCookies(request);

        // If not in cookie, try Authorization header
        if (token == null && authHeader != null) {
            token = jwtUtils.extractTokenFromHeader(authHeader);
        }

        if (token == null) {
            throw AuthException.invalidToken(ErrorMessages.NO_TOKEN_FOUND);
        }

        tokenBlackListService.blacklistToken(token);
        SecurityContextHolder.clearContext();

        // Clear the JWT cookie
        cookieUtils.clearJwtCookie(response);

        BaseRespDTO respDTO = new BaseRespDTO();
        respDTO.setMessage("Logged out successfully");
        respDTO.setStatus(true);
        respDTO.setCode("200");
        return respDTO;
    }

    @Override
    public BaseRespDTO logoutAllSessions(String authHeader, HttpServletRequest request, HttpServletResponse response) {
        String username = securityUtils.validateAndExtractUsername(request);
        tokenBlackListService.invalidateAllUserTokens(username);
        SecurityContextHolder.clearContext();

        // Clear the JWT cookie
        cookieUtils.clearJwtCookie(response);

        BaseRespDTO respDTO = new BaseRespDTO();
        respDTO.setMessage("All sessions logged out successfully");
        respDTO.setStatus(true);
        respDTO.setCode("200");
        return respDTO;
    }

    @Override
    public BaseRespDTO registerUser(RegisterReqDTO reqDTO) {
        BaseRespDTO respDTO = new BaseRespDTO();
        if (reqDTO == null) {
            respDTO.setMessage(ErrorMessages.INVALID_DATA);
            return respDTO;
        }

        // Check if username already exists
        if (userRepository.findByUsername(reqDTO.getUsername()).isPresent()) {
            respDTO.setMessage(ErrorMessages.USERNAME_EXISTS);
            respDTO.setStatus(false);
            respDTO.setCode("400");
            return respDTO;
        }

        UserEntity user = new UserEntity();
        user.setName(reqDTO.getName());
        user.setEmail(reqDTO.getEmail());
        user.setUsername(reqDTO.getUsername());
        user.setPassword(passwordEncoder.encode(reqDTO.getPassword()));

        // Handle seller registration request
        if (reqDTO.isRegisterAsSeller()) {
            SellerEntity seller = new SellerEntity();
            seller.setUser(user);
            seller.setSellerStatus(SellerStatus.PENDING);

            seller.setAddress(reqDTO.getSeller().getAddress());
            seller.setBusinessName(reqDTO.getSeller().getBusinessName());
            seller.setContactPhone(reqDTO.getSeller().getContactPhone());

            user.setSeller(seller);

            respDTO.setMessage("User registered successfully. Seller status is pending approval.");
        } else {
            respDTO.setMessage("User registered successfully");
        }

        userRepository.save(user);
        respDTO.setCode("200");
        respDTO.setStatus(true);

        return respDTO;
    }

    @Override
    public TokenValidationRespDTO validateToken(String token) {
        TokenValidationRespDTO response = new TokenValidationRespDTO();
        response.setValid(false);

        try {
            if (token == null) {
                return response;
            }

            if (tokenBlackListService.isTokenBlacklisted(token)) {
                return response;
            }

            String username = jwtUtils.extractUsername(token);
            if (username == null) {
                return response;
            }

            UserEntity user = securityUtils.getUserByUsername(username);

            UserDetails userDetails = User.builder()
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .authorities(user.getRole())
                    .build();

            if (Boolean.FALSE.equals(jwtUtils.validateToken(token, userDetails))) {
                return response;
            }

            // Token is valid, set all user details
            response.setValid(true);
            response.setUsername(username);
            response.setUserId(user.getId());
            response.setEmail(user.getEmail());
            response.setName(user.getName());
            response.setAuthorities(userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList());

        } catch (AuthException ae) {
            log.error("Auth error validating token: {}", ae.getMessage());
            throw ae;
        } catch (Exception e) {
            log.error("Error validating token: {}", e.getMessage());
            throw new AuthException("Error validating token",
                    AuthStatus.AUTH_001, HttpStatus.UNAUTHORIZED);
        }

        return response;
    }

    @Override
    @Transactional
    public BaseRespDTO forgotPassword(ForgotPasswordReqDTO reqDTO) {
        BaseRespDTO respDTO = new BaseRespDTO();

        UserEntity user = userRepository.findByEmail(reqDTO.getEmail())
                .orElseThrow(AuthException::userNotFound);

        // Generate reset token
        String token = jwtUtils.generatePasswordResetToken(user.getId() + UUID.randomUUID().toString());
        // Create password reset token entity
        PasswordResetTokenEntity resetToken = new PasswordResetTokenEntity();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(24)); // Token valid for 24 hours

        passwordResetTokenRepository.save(resetToken);

        try {
            // Send password reset email
            emailUtils.sendPasswordResetEmail(user.getEmail(), token, user.getName());

            respDTO.setStatus(true);
            respDTO.setMessage("Password reset instructions have been sent to your email " + token); // for now the
            // token os
            // temporary
            respDTO.setCode("200");
        } catch (AuthException ae) {
            throw ae;
        } catch (Exception e) {
            log.error("Error sending password reset email: {}", e.getMessage());
            throw new AuthException("Failed to send password reset email",
                    AuthStatus.AUTH_007, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return respDTO;
    }

    @Override
    @Modifying
    public BaseRespDTO resetPassword(ResetPasswordReqDTO reqDTO) {
        BaseRespDTO respDTO = new BaseRespDTO();

        PasswordResetTokenEntity resetToken = passwordResetTokenRepository.findByToken(reqDTO.getToken())
                .orElseThrow(() -> AuthException.invalidToken("Invalid or expired reset token"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            passwordResetTokenRepository.delete(resetToken);
            throw AuthException.invalidToken("Reset token has expired. Please generate a new Token");
        }

        securityUtils.updatePasswordAndDeleteToken(resetToken, reqDTO.getNewPassword());

        respDTO.setStatus(true);
        respDTO.setMessage("Password reset successful");
        respDTO.setCode("200");

        return respDTO;
    }

    @Override
    public TokenValidationRespDTO validateUserToken() {
        TokenValidationRespDTO tokenValidationRespDTO = new TokenValidationRespDTO();
        tokenValidationRespDTO.setValid(false);
        CustomUserDetails userDetails = UserContextHelper.getLoggedInUser();
        if(userDetails != null) {
            tokenValidationRespDTO.setValid(true);
            tokenValidationRespDTO.setUserId(userDetails.getUserId());
            tokenValidationRespDTO.setEmail(userDetails.getEmail());
            tokenValidationRespDTO.setUsername(userDetails.getUsername());
            tokenValidationRespDTO.setAuthorities(userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
        }
        return tokenValidationRespDTO;
    }

    private BaseRespDTO getLockoutResponse(String username) {
        LocalDateTime lockoutTime = loginAttemptTracker.getLockoutTime(username);
        String formattedTime = lockoutTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        BaseRespDTO respDTO = new BaseRespDTO();
        respDTO.setStatus(false);
        respDTO.setMessage("Account is locked due to too many failed attempts. Please try again after " + formattedTime);
        respDTO.setCode(String.valueOf(HttpStatus.TOO_MANY_REQUESTS.value()));
        return respDTO;
    }
}
