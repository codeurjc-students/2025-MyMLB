package com.mlb.mlbportal.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mlb.mlbportal.dto.match.MatchDTO;
import com.mlb.mlbportal.services.MatchService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "Matches", description = "Operations related to the matches")
@RestController
@RequestMapping("/api/matches")
public class MatchController {
    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @Operation(summary = "Get the matches of the day", description = "Returns a list of matches scheduled for today. Each match includes the home and away teams, scores, scheduled date/time, and current status (Scheduled, InProgress, Finished).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the matches", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MatchDTO.class), examples = @ExampleObject(value = "[{\"homeTeam\":{\"name\":\"Los Angeles Dodgers\",\"abbreviation\":\"LAD\"},\"awayTeam\":{\"name\":\"New York Yankees\",\"abbreviation\":\"NYY\"},\"homeScore\":0,\"awayScore\":0,\"date\":\"2025-10-20 19:00\",\"status\":\"Scheduled\"},{\"homeTeam\":{\"name\":\"Toronto Blue Jays\",\"abbreviation\":\"TOR\"},\"awayTeam\":{\"name\":\"Detroit Tigers\",\"abbreviation\":\"DET\"},\"homeScore\":3,\"awayScore\":2,\"date\":\"2025-10-20 21:00\",\"status\":\"InProgress\"}]"))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @GetMapping(value = "/today", produces = "application/json")
    public ResponseEntity<List<MatchDTO>> getMatchesOfTheDay() {
        return ResponseEntity.ok(this.matchService.getMatchesOfTheDay());
    }
}