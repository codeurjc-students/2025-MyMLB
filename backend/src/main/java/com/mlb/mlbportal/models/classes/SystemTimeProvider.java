package com.mlb.mlbportal.models.classes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import com.mlb.mlbportal.models.interfaces.TimeProvider;

public class SystemTimeProvider implements TimeProvider {
    private final ZoneId zone;

    public SystemTimeProvider(ZoneId zone) {
        this.zone = zone;
    }

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now(zone);
    }

    @Override
    public LocalDate today() {
        return LocalDate.now(zone);
    }
}