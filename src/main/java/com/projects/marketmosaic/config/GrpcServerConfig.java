package com.projects.marketmosaic.config;

import com.projects.marketmosaic.service.impl.AuthGrpcService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GrpcServerConfig {

    private final AuthGrpcService authGrpcService;

    @Value("${grpc.server.port:9090}")
    private int grpcServerPort;

    private Server grpcServer;

    @PostConstruct
    public void startGrpcServer() throws IOException {
        grpcServer = ServerBuilder.forPort(grpcServerPort)
                .addService(ServerInterceptors.intercept(authGrpcService, new AuthInterceptorGrpc()))
                .build()
                .start();

        log.info("gRPC server started on port: {}", grpcServerPort);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down gRPC server");
            if (grpcServer != null) {
                grpcServer.shutdown();
            }
        }));
    }

    @PreDestroy
    public void stopGrpcServer() {
        if (grpcServer != null) {
            grpcServer.shutdown();
        }
    }
}