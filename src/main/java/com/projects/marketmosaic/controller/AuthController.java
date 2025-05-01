package com.projects.marketmosaic.controller;

import com.projects.marketmosaic.common.dto.resp.BaseRespDTO;
import com.projects.marketmosaic.config.security.LoginAttemptTracker;
import com.projects.marketmosaic.dtos.*;
import com.projects.marketmosaic.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final LoginAttemptTracker loginAttemptTracker;

    @PostMapping("/login")
    public ResponseEntity<BaseRespDTO> login(
            @RequestBody LoginReqDTO loginReqDTO,
            HttpServletResponse response) {

        return ResponseEntity.ok(authService.loginUser(loginReqDTO, response));
    }

    @PostMapping("/register")
    public ResponseEntity<BaseRespDTO> register(
            @RequestBody RegisterReqDTO registerReqDTO) {
        return ResponseEntity.ok(authService.registerUser(registerReqDTO));
    }

    @PostMapping("/logout")
    public ResponseEntity<BaseRespDTO> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletRequest request,
            HttpServletResponse response) {
        return ResponseEntity.ok(authService.logout(authHeader, request, response));
    }

    @PostMapping("/logout-all")
    public ResponseEntity<BaseRespDTO> logoutAllSessions(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletRequest request,
            HttpServletResponse response) {
        return ResponseEntity.ok(authService.logoutAllSessions(authHeader, request, response));
    }

//    @PostMapping("/validate")
//    public ResponseEntity<TokenValidationRespDTO> validateToken(
//            HttpServletRequest request) {
//        return ResponseEntity.ok(authService.validateToken(request));
//    }

    @PostMapping("/forgot-password")
    public ResponseEntity<BaseRespDTO> forgotPassword(
            @RequestBody ForgotPasswordReqDTO reqDTO) {
        return ResponseEntity.ok(authService.forgotPassword(reqDTO));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<BaseRespDTO> resetPassword(
            @RequestBody ResetPasswordReqDTO reqDTO) {
        return ResponseEntity.ok(authService.resetPassword(reqDTO));
    }
}
