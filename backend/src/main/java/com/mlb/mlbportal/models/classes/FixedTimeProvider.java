package com.mlb.mlbportal.models.classes;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.mlb.mlbportal.models.interfaces.TimeProvider;

public class FixedTimeProvider implements TimeProvider {
    private final LocalDateTime fixedDateTime;

    public FixedTimeProvider(LocalDateTime fixedDateTime) {
        this.fixedDateTime = fixedDateTime;
    }

    @Override
    public LocalDateTime now() {
        return fixedDateTime;
    }

    @Override
    public LocalDate today() {
        return fixedDateTime.toLocalDate();
    }
}