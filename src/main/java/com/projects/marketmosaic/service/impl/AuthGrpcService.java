package com.projects.marketmosaic.service.impl;

import auth.AuthServiceGrpc;
import auth.Auth;
import com.projects.marketmosaic.constants.GrpcConstants;
import com.projects.marketmosaic.dtos.TokenValidationRespDTO;
import com.projects.marketmosaic.service.AuthService;
import io.grpc.stub.StreamObserver;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthGrpcService extends AuthServiceGrpc.AuthServiceImplBase {

    private final AuthService authService;

    @Override
    public void validateToken(Auth.TokenRequest request, StreamObserver<Auth.TokenResponse> responseObserver) {
        try {
            String token = GrpcConstants.USER_TOKEN_CTX_KEY.get();
            TokenValidationRespDTO tokenValidation = authService.validateToken(token);

            var response = Auth.TokenResponse.newBuilder()
                    .setValid(tokenValidation.isValid())
                    .setUsername(tokenValidation.getUsername() != null ? tokenValidation.getUsername() : "")
                    .setUserId(tokenValidation.getUserId() != null ? tokenValidation.getUserId() : 0L)
                    .setEmail(tokenValidation.getEmail() != null ? tokenValidation.getEmail() : "")
                    .setName(tokenValidation.getName() != null ? tokenValidation.getName() : "")
                    .addAllAuthorities(
                            tokenValidation.getAuthorities() != null ? tokenValidation.getAuthorities().stream()
                                    .map(GrantedAuthority::getAuthority)
                                    .toList() : Collections.emptyList())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error validating token: " + e.getMessage())
                    .asRuntimeException());
        }
    }
}