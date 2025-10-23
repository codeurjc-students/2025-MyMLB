package com.mlb.mlbportal.models.interfaces;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface TimeProvider {
    LocalDateTime now();
    LocalDate today();
}