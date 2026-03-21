package com.mlb.mlbportal.dto.analytics;

import java.time.LocalDateTime;
import java.util.List;

public record APIAnalyticsDTO(
        LocalDateTime timeStamp,
        Long totalRequests,
        Long totalErrors,
        Long totalSuccesses,
        Double averageResponseTime,
        List<EndpointAnalyticsDTO> mostDemandedEndpoints
) {}