package com.projects.marketmosaic.impl;

import auth.AuthServiceGrpc;
import auth.Auth;
import com.projects.marketmosaic.dtos.TokenValidationRespDTO;
import com.projects.marketmosaic.service.AuthService;
import com.projects.marketmosaic.service.impl.AuthGrpcService;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class AuthGrpcServiceTest {

    @Mock
    private AuthService authService;

    @Mock
    private jakarta.servlet.http.HttpServletRequest request;

    private Server server;
    private ManagedChannel channel;
    private AuthServiceGrpc.AuthServiceBlockingStub blockingStub;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Create a server using a random name
        String serverName = InProcessServerBuilder.generateName();
        server = InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(new AuthGrpcService(authService))
                .build()
                .start();

        // Create a client channel and register for automatic graceful shutdown
        channel = InProcessChannelBuilder.forName(serverName)
                .directExecutor()
                .usePlaintext()
                .build();

        blockingStub = AuthServiceGrpc.newBlockingStub(channel);
    }

    @AfterEach
    void tearDown() throws Exception {
        channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        server.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    void validateToken_ValidToken_ReturnsValidResponse() {
        // Arrange
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
        TokenValidationRespDTO mockResponse = new TokenValidationRespDTO();
        mockResponse.setValid(true);
        mockResponse.setUsername("testuser");
        mockResponse.setUserId(1L);
        mockResponse.setEmail("test@example.com");
        mockResponse.setName("Test User");
        mockResponse.setAuthorities(Collections.singletonList(authority));

        when(authService.validateToken(anyString())).thenReturn(mockResponse);

        // Act
        Auth.TokenResponse response = blockingStub.validateToken(Auth.TokenRequest.getDefaultInstance());

        // Assert
        assertTrue(response.getValid());
        assertEquals("testuser", response.getUsername());
        assertEquals(1L, response.getUserId());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("Test User", response.getName());
        assertEquals(1, response.getAuthoritiesCount());
        assertEquals("ROLE_USER", response.getAuthorities(0));
    }

    @Test
    void validateToken_InvalidToken_ReturnsInvalidResponse() {
        // Arrange
        TokenValidationRespDTO mockResponse = new TokenValidationRespDTO();
        mockResponse.setValid(false);
        mockResponse.setAuthorities(Collections.emptyList());

        when(authService.validateToken(anyString())).thenReturn(mockResponse);

        // Act
        Auth.TokenResponse response = blockingStub.validateToken(Auth.TokenRequest.getDefaultInstance());

        // Assert
        assertFalse(response.getValid());
        assertEquals(0, response.getAuthoritiesCount());
        assertEquals("", response.getUsername());
        assertEquals(0L, response.getUserId());
        assertEquals("", response.getEmail());
        assertEquals("", response.getName());
    }

    @Test
    void validateToken_NullFields_ReturnsDefaultValues() {
        // Arrange
        TokenValidationRespDTO mockResponse = new TokenValidationRespDTO();
        mockResponse.setValid(true);
        // All other fields are null by default

        when(authService.validateToken(anyString())).thenReturn(mockResponse);

        // Act
        Auth.TokenResponse response = blockingStub.validateToken(Auth.TokenRequest.getDefaultInstance());

        // Assert
        assertTrue(response.getValid());
        assertEquals("", response.getUsername());
        assertEquals(0L, response.getUserId());
        assertEquals("", response.getEmail());
        assertEquals("", response.getName());
        assertEquals(0, response.getAuthoritiesCount());
    }
}