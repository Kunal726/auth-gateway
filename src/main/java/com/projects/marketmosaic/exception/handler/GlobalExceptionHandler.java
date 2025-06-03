package com.projects.marketmosaic.exception.handler;

import com.projects.marketmosaic.common.dto.resp.BaseRespDTO;
import com.projects.marketmosaic.common.exception.handler.BaseGlobalHandler;
import com.projects.marketmosaic.exception.exceptions.AuthException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends BaseGlobalHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<BaseRespDTO> handleAuthException(AuthException ex) {
        log.error("Auth Exception: {}", ex.getMessage());
        BaseRespDTO response = new BaseRespDTO();
        response.setCode(ex.getCode().name());
        response.setMessage(ex.getMessage());
        response.setStatus(false);
        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<BaseRespDTO> handleBadCredentialsException(BadCredentialsException ex) {
        log.error("Bad Credentials Exception: {}", ex.getMessage());
        AuthException authException = AuthException.invalidCredentials();
        BaseRespDTO response = new BaseRespDTO();
        response.setCode(authException.getCode().name());
        response.setMessage(authException.getMessage());
        response.setStatus(false);
        return ResponseEntity.status(authException.getStatus()).body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<BaseRespDTO> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        BaseRespDTO response = new BaseRespDTO();
        response.setCode(String.valueOf(HttpStatus.CONFLICT.value()));
        response.setMessage(ex.getMessage());
        response.setStatus(false);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
}