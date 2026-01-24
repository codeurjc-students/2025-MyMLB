package com.mlb.mlbportal.dto.ticket;

public record EventManagerDTO(
        Long id,
        String sectorName,
        double price,
        int availability,
        int totalCapacity
) {}