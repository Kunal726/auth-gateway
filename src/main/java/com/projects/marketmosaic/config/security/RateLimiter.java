package com.projects.marketmosaic.config.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class RateLimiter {
    private static class RateLimit {
        private final int maxRequests;
        private final Duration duration;
        private final Map<Instant, Integer> requests = new ConcurrentHashMap<>();

        public RateLimit(int maxRequests, Duration duration) {
            this.maxRequests = maxRequests;
            this.duration = duration;
        }

        public synchronized boolean tryAcquire() {
            Instant now = Instant.now();
            Instant cutoff = now.minus(duration);

            // Clean up old entries
            requests.entrySet().removeIf(entry -> entry.getKey().isBefore(cutoff));

            // Count current requests
            int currentRequests = requests.values().stream().mapToInt(Integer::intValue).sum();

            if (currentRequests >= maxRequests) {
                return false;
            }

            requests.merge(now, 1, Integer::sum);
            return true;
        }
    }

    private final Map<String, RateLimit> rateLimits = new ConcurrentHashMap<>();

    public boolean isAllowed(String key, int maxRequests, Duration duration) {
        RateLimit limit = rateLimits.computeIfAbsent(key,
                _ -> new RateLimit(maxRequests, duration));
        return limit.tryAcquire();
    }

    public void clearRateLimit(String key) {
        rateLimits.remove(key);
    }
}