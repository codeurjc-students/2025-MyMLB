package com.mlb.mlbportal.controllers;

import com.mlb.mlbportal.security.jwt.AuthResponse;
import com.mlb.mlbportal.services.utilities.CacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Cache Management", description = "Endpoints for managing and clearing application caches")
@RestController
@RequestMapping("/api/v1/cache")
@AllArgsConstructor
public class CacheController {
    private final CacheService cacheService;

    @Operation(summary = "Get all cache names", description = "Retrieve a list of all active cache names managed by the application.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved cache names", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @GetMapping(produces = "application/json")
    public ResponseEntity<List<String>> getCaches() {
        return ResponseEntity.ok(this.cacheService.getCaches());
    }

    @Operation(summary = "Clear a specific cache", description = "Clear the contents of a single cache.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully cleared the cache", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Cache not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @DeleteMapping(value = "/{name}", produces = "application/json")
    public ResponseEntity<AuthResponse> clearSingleCache(@PathVariable("name")String name) {
        this.cacheService.clearSingleCache(name);
        String message = "Cache: " + name + " Successfully Cleared";
        return ResponseEntity.ok(new AuthResponse(AuthResponse.Status.SUCCESS, message));
    }

    @Operation(summary = "Clear all caches", description = "Perform a global purge of all application caches.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully cleared all caches", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @DeleteMapping(produces = "application/json")
    public ResponseEntity<AuthResponse> clearAllCaches() {
        this.cacheService.clearAllCaches();
        return ResponseEntity.ok(new AuthResponse(AuthResponse.Status.SUCCESS, "All Caches Successfully Cleared"));
    }
}