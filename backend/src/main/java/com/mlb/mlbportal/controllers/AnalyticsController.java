package com.mlb.mlbportal.controllers;

import com.mlb.mlbportal.dto.analytics.APIAnalyticsDTO;
import com.mlb.mlbportal.models.analytics.VisibilityStats;
import com.mlb.mlbportal.security.jwt.AuthResponse;
import com.mlb.mlbportal.services.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(name = "Application Analytics", description = "Endpoints related to analytical data of the application")
@RestController
@RequestMapping("/api/v1/analytics")
@AllArgsConstructor
public class AnalyticsController {
    private final AnalyticsService statsService;

    // --------- Visibility Analytics ----------------------------------

    @Operation(summary = "Return Visibility Analytics", description = "Retrieve the visibility analytics of the application within a period of time.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the analytics", content = @Content(mediaType = "application/json", schema = @Schema(implementation = VisibilityStats.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @GetMapping(value = "/visibility", produces = "application/json")
    public ResponseEntity<List<VisibilityStats>> getVisibilityStats(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        return ResponseEntity.ok(this.statsService.getVisibilityStats(dateFrom, dateTo));
    }

    @Operation(summary = "Increase Visualizations", description = "Register a new visualization.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully update the visualizations", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @PostMapping(value = "/visibility/visualizations", produces = "application/json")
    public ResponseEntity<AuthResponse> updateVisualizations() {
        this.statsService.increaseVisualizations();
        return ResponseEntity.ok(new AuthResponse(AuthResponse.Status.SUCCESS, "Visualizations successfully updated"));
    }

    @Operation(summary = "Increase New Users", description = "Increase the number of registered users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully update the number of registered users", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @PostMapping(value = "/visibility/registrations", produces = "application/json")
    public ResponseEntity<AuthResponse> updateNewUsers() {
        this.statsService.increaseNewUsers();
        return ResponseEntity.ok(new AuthResponse(AuthResponse.Status.SUCCESS, "New Users successfully updated"));
    }

    @Operation(summary = "Increase Deleted Users", description = "Increase the number of deleted users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully update the number of deleted users", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @PostMapping(value = "/visibility/losses", produces = "application/json")
    public ResponseEntity<AuthResponse> updateDeletedUsers() {
        this.statsService.increaseDeletedUsers();
        return ResponseEntity.ok(new AuthResponse(AuthResponse.Status.SUCCESS, "Deleted Users successfully updated"));
    }

    // --------- Fav Teams Analytics ----------------------------------

    @Operation(summary = "Return Favorite Team Analytics", description = "Retrieve analytical data related to the number of fans a team has.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the analytics", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @GetMapping(value = "/fav-teams", produces = "application/json")
    public ResponseEntity<Map<String, Long>> getFavTeamsAnalytics() {
        return ResponseEntity.ok(this.statsService.getFavTeamsAnalytics());
    }

    // --------- API Analytics ----------------------------------

    @Operation(summary = "Return API Performance Analytics", description = "Retrieve real time API performance metrics, including total requests, error counts, and average response times.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieve the analytics", content = @Content(mediaType = "application/json", schema = @Schema(implementation = APIAnalyticsDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @GetMapping(value = "/api-performance", produces = "application/json")
    public ResponseEntity<List<APIAnalyticsDTO>> getAPIPerformanceHistory(@RequestParam(defaultValue = "1h") String dateRange) {
        return ResponseEntity.ok(this.statsService.getAPIPerformanceHistory(dateRange));
    }
}