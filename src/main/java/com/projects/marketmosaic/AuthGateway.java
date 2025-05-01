package com.projects.marketmosaic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AuthGateway {
    public static void main(String[] args) {
        SpringApplication.run(AuthGateway.class, args);
    }
}