package com.mlb.mlbportal.controllers;

import com.mlb.mlbportal.models.VisibilityStats;
import com.mlb.mlbportal.security.jwt.AuthResponse;
import com.mlb.mlbportal.services.StatsService;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/stats")
@AllArgsConstructor
public class StatsController {
    private final StatsService statsService;

    @GetMapping(value = "/visibility", produces = "application/json")
    public ResponseEntity<List<VisibilityStats>> getVisibilityStats(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        return ResponseEntity.ok(this.statsService.getVisibilityStats(dateFrom, dateTo));
    }

    @PostMapping(value = "/visibility/visualizations", produces = "application/json")
    public ResponseEntity<AuthResponse> updateVisualizations() {
        this.statsService.increaseVisualizations();
        return ResponseEntity.ok(new AuthResponse(AuthResponse.Status.SUCCESS, "Visualizations successfully updated"));
    }

    @PostMapping(value = "/visibility/registrations", produces = "application/json")
    public ResponseEntity<AuthResponse> updateRegistrations() {
        this.statsService.increaseRegistrations();
        return ResponseEntity.ok(new AuthResponse(AuthResponse.Status.SUCCESS, "Registrations successfully updated"));
    }

    @PostMapping(value = "/visibility/losses", produces = "application/json")
    public ResponseEntity<AuthResponse> updateLosses() {
        this.statsService.increaseLosses();
        return ResponseEntity.ok(new AuthResponse(AuthResponse.Status.SUCCESS, "Losses successfully updated"));
    }
}