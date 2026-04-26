package com.mlb.mlbportal.controllers;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import com.mlb.mlbportal.security.jwt.AuthResponse;
import com.mlb.mlbportal.services.mlbAPI.MatchImportService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

@Tag(name = "Matches", description = "Endpoints related to the matches of the current MLB season")
@RestController
@RequestMapping("/api/v1/matches")
@AllArgsConstructor
public class MatchController {
    private final MatchService matchService;
    private final MatchImportService matchImportService;

    @Operation(summary = "Get the match of the given id", description = "Retrieve the match whose ID is provided.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the match", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MatchDTO.class))),
            @ApiResponse(responseCode = "404", description = "Match Not Found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @GetMapping(value = "/{matchId}", produces = "application/json")
    public ResponseEntity<MatchDTO> getMatchById(@PathVariable("matchId")Long matchId) {
        return ResponseEntity.ok(this.matchService.getMatchById(matchId));
    }

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

    @Operation(summary = "Get matches of a team (home/away)", description = "Returns a paginated list of matches for the specified team, filtered by location (home or away).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved matches", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MatchDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid location parameter (must be 'home' or 'away')", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Team not found or no matches available", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @GetMapping(value = "/team/{teamName}", produces = "application/json")
    public ResponseEntity<Page<MatchDTO>> getMatchesOfATeam(@PathVariable("teamName") String teamName,
                                                            @RequestParam String location, @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "10") int size) {
        if ("home".equalsIgnoreCase(location)) {
            return ResponseEntity.ok(this.matchService.getHomeMatches(teamName, page, size));
        }
        else if ("away".equalsIgnoreCase(location)) {
            return ResponseEntity.ok(this.matchService.getAwayMatches(teamName, page, size));
        }
        else {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Get matches of a team by month", description = "Returns all matches of the specified team within a given year and month.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved matches", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MatchDTO.class))),
            @ApiResponse(responseCode = "404", description = "Team not found or no matches available for the given month", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @GetMapping(value = "/team/{teamName}/calendar", produces = "application/json")
    public ResponseEntity<List<MatchDTO>> getMatchesOfTeamByMonth(@PathVariable String teamName,
                                                                  @RequestParam int year, @RequestParam int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return ResponseEntity.ok(this.matchService.getMatchesOfTeamBetweenDates(teamName, start, end));
    }

    @Operation(summary = "Synchronize Matches", description = "Synchronize the matches of the full current season or just today ones.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Matches Successfully refreshed", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @PostMapping(value = "/sync", produces = "application/json")
    public ResponseEntity<AuthResponse> refreshSeasonMatches(@RequestParam(defaultValue = "today")String scope, Authentication auth) {
        if ("season".equalsIgnoreCase(scope)) {
            if (auth == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new AuthResponse(AuthResponse.Status.FAILURE, "Access denied"));
            }
            boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new AuthResponse(AuthResponse.Status.FAILURE, "Access denied"));
            }
            this.matchImportService.updateSeasonMatches();
        }
        else {
            this.matchImportService.verifyMatchStatus();
        }
        String message = ("season".equalsIgnoreCase(scope)) ? "Matches Successfully Synchronized for the current season" : "Matches Successfully Synchronized for the current day";
        return ResponseEntity.accepted().body(new AuthResponse(AuthResponse.Status.SUCCESS, message));
    }
}