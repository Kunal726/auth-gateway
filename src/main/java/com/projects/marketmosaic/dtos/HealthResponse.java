package com.projects.marketmosaic.dtos;

import java.util.Map;

public record HealthResponse(String status, Map<String, HealthStatus> components) {
}