package com.mlb.mlbportal.dto.ticket;

public record SectorCreateRequest(
        String name,
        int totalCapacity
) {}