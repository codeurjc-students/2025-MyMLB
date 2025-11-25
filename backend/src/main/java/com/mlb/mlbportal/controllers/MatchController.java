package com.mlb.mlbportal.controllers;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mlb.mlbportal.dto.match.MatchDTO;
import com.mlb.mlbportal.services.MatchService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

import java.security.Principal;
import java.util.List;

@Tag(name = "Matches", description = "Operations related to the matches")
@RestController
@RequestMapping("/api/v1/matches")
@AllArgsConstructor
public class MatchController {
    private final MatchService matchService;

    @Operation(summary = "Get the matches of the day (paginated)", description = "Returns a paginated list of matches scheduled for today. Always 10 matches per page.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the matches", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MatchDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @GetMapping(value = "/today", produces = "application/json")
    public ResponseEntity<Page<MatchDTO>> getMatchesOfTheDayPaginated(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String username = (principal != null) ? principal.getName() : null;
        Page<MatchDTO> matches = this.matchService.getMatchesOfTheDay(username, page, size);
        return ResponseEntity.ok(matches);
    }

    @Operation(summary = "Get home matches of a team", description = "Returns a list of all matches where the specified team plays at home.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved home matches", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MatchDTO.class))),
            @ApiResponse(responseCode = "404", description = "Team not found or no home matches available", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @GetMapping(value = "home/{teamName}", produces = "application/json")
    public ResponseEntity<List<MatchDTO>> getHomeMatches(@PathVariable("teamName") String teamName) {
        return ResponseEntity.ok(this.matchService.getHomeMatches(teamName));
    }

    @Operation(summary = "Get away matches of a team", description = "Returns a list of all matches where the specified team plays away.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved away matches", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MatchDTO.class))),
            @ApiResponse(responseCode = "404", description = "Team not found or no away matches available", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @GetMapping(value = "away/{teamName}", produces = "application/json")
    public ResponseEntity<List<MatchDTO>> getAwayMatches(@PathVariable("teamName") String teamName) {
        return ResponseEntity.ok(this.matchService.getAwayMatches(teamName));
    }
}