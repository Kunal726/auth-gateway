package com.projects.marketmosaic.controller;

import com.projects.marketmosaic.dtos.HealthResponse;
import com.projects.marketmosaic.dtos.HealthStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.availability.AvailabilityState;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthController {
    private final JdbcTemplate jdbcTemplate;
    private final RedisConnectionFactory redisConnectionFactory;
    private final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

    @GetMapping
    public HealthResponse checkHealth() {
        return new HealthResponse(
                "UP",
                Map.of(
                        "database", checkDatabase(),
                        "redis", checkRedis(),
                        "memory", checkMemory(),
                        "liveness", checkLiveness(),
                        "readiness", checkReadiness()));
    }

    private HealthStatus checkDatabase() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return new HealthStatus("UP", "Database connection is healthy");
        } catch (Exception e) {
            log.error("Database health check failed", e);
            return new HealthStatus("DOWN", "Database connection failed: " + e.getMessage());
        }
    }

    private HealthStatus checkRedis() {
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            connection.ping();
            return new HealthStatus("UP", "Redis connection is healthy");
        } catch (Exception e) {
            log.error("Redis health check failed", e);
            return new HealthStatus("DOWN", "Redis connection failed: " + e.getMessage());
        }
    }

    private HealthStatus checkMemory() {
        long usedMemory = memoryMXBean.getHeapMemoryUsage().getUsed();
        long maxMemory = memoryMXBean.getHeapMemoryUsage().getMax();
        double memoryUsage = (double) usedMemory / maxMemory * 100;

        String status = memoryUsage > 90 ? "WARNING" : "UP";
        String message = String.format("Memory usage: %.2f%%", memoryUsage);

        return new HealthStatus(status, message);
    }

    private HealthStatus checkLiveness() {
        AvailabilityState state = LivenessState.BROKEN;
        return new HealthStatus(
                state == LivenessState.CORRECT ? "UP" : "DOWN",
                "Liveness state: " + state);
    }

    private HealthStatus checkReadiness() {
        AvailabilityState state = ReadinessState.ACCEPTING_TRAFFIC;
        return new HealthStatus(
                state == ReadinessState.ACCEPTING_TRAFFIC ? "UP" : "DOWN",
                "Readiness state: " + state);
    }
}