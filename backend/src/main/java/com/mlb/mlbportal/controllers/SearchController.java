package com.mlb.mlbportal.controllers;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mlb.mlbportal.handler.InvalidSearchTypeException;
import com.mlb.mlbportal.services.SearchService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

@Tag(name = "Search", description = "Operations related to searching stadiums, teams, and players")
@RestController
@RequestMapping("api/v1/searchs")
@AllArgsConstructor
public class SearchController {
    private final SearchService searchService;

    @Operation(summary = "Search entities", description = "Performs a search across MLB entities based on the provided type. "
            +
            "Supported types are: 'stadium', 'team', and 'player'. " +
            "When searching for players, the 'playerType' parameter must be specified as either 'position' or 'pitcher'. "
            +
            "Results are returned in a paginated format.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved search results", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Missing or invalid parameters (e.g., playerType not provided or incorrect)", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Invalid search type (not stadium, team, or player)", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/{type}")
    public ResponseEntity<Page<?>> searchStadium(@PathVariable("type") String type, @RequestParam String query,
            @RequestParam(required = false) String playerType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "0") int size) {

        switch (type.toLowerCase()) {
            case "stadium" -> {
                return ResponseEntity.ok(searchService.searchStadiums(query, page, size));
            }
            case "team" -> {
                return ResponseEntity.ok(searchService.searchTeams(query, page, size));
            }
            case "player" -> {
                if ("position".equalsIgnoreCase(playerType)) {
                    return ResponseEntity.ok(searchService.searchPositionPlayers(query, page, size));
                } else if ("pitcher".equalsIgnoreCase(playerType)) {
                    return ResponseEntity.ok(searchService.searchPitchers(query, page, size));
                } else {
                    throw new InvalidSearchTypeException("Missing or invalid playerType parameter");
                }
            }
            default -> throw new InvalidSearchTypeException("Invalid search type: " + type);
        }
    }
}