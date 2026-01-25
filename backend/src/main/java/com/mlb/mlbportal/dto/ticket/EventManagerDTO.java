package com.mlb.mlbportal.dto.ticket;

public record EventManagerDTO(
        Long id,
        Long sectorId,
        String sectorName,
        double price,
        int availability,
        int totalCapacity
) {}