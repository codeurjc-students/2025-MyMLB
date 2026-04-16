package com.mlb.mlbportal.controllers;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mlb.mlbportal.dto.team.HistoricRankingDTO;
import com.mlb.mlbportal.dto.team.RunsStatsDTO;
import com.mlb.mlbportal.dto.team.WinDistributionDTO;
import com.mlb.mlbportal.dto.team.WinsPerRivalDTO;
import com.mlb.mlbportal.services.mlbAPI.TeamImportService;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mlb.mlbportal.dto.team.TeamDTO;
import com.mlb.mlbportal.dto.team.TeamInfoDTO;
import com.mlb.mlbportal.dto.team.TeamSummary;
import com.mlb.mlbportal.dto.team.UpdateTeamRequest;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;
import com.mlb.mlbportal.security.jwt.AuthResponse;
import com.mlb.mlbportal.services.team.TeamService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

@Tag(name = "Teams", description = "Endpoints related to MLB teams, standings, and current season statistics")
@RestController
@RequestMapping("/api/v1/teams")
@AllArgsConstructor
public class TeamController {
    private final TeamService teamService;
    private final TeamImportService teamImportService;

    @Operation(summary = "Get all teams", description = "Returns a list of all teams with calculated stats, including wins, losses, total games, and win percentage.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of teams", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @GetMapping(produces = "application/json")
    public ResponseEntity<Page<TeamInfoDTO>> getAllTeams(@RequestParam(defaultValue = "0")int page, @RequestParam(defaultValue = "10")int size) {
        Page<TeamInfoDTO> teams = this.teamService.getTeams(page, size);
        return ResponseEntity.ok(teams);
    }

    @Operation(summary = "Get all available teams", description = "Returns a list of all available teams. An available team is one whose roster is less than 24")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of teams", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamSummary.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @GetMapping(value = "/available", produces = "application/json")
    public ResponseEntity<Page<TeamSummary>> getAvailableTeams(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(this.teamService.getAvailableTeams(page, size));
    }

    @Operation(summary = "Get team standings", description = "Returns standings grouped by league and division, ordered by win percentage. Each team includes stats such as total games, wins, losses, win percentage, games back, and last 10 games.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved standings", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @GetMapping(value = "/standings", produces = "application/json")
    public ResponseEntity<Map<League, Map<Division, List<TeamDTO>>>> getStandings(Principal principal) {
        String username = (principal != null) ? principal.getName() : null;
        Map<League, Map<Division, List<TeamDTO>>> standings = this.teamService.getStandings(username);
        return ResponseEntity.ok(standings);
    }

    @Operation(summary = "Get team info by name", description = "Returns detailed information about a specific team, including abbreviation, logo, league, division, total games, etc.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved team info", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamInfoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Team not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @GetMapping(value = "/{teamName}", produces = "application/json")
    public ResponseEntity<TeamInfoDTO> getTeamInfo(@PathVariable("teamName") String teamName) {
        return ResponseEntity.ok(this.teamService.getTeamInfo(teamName));
    }

    @Operation(summary = "Get rivals of a team", description = "Returns the rivals of a certain team.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the rivals", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamInfoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Team not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @GetMapping(value = "/{teamName}/rivals", produces = "application/json")
    public ResponseEntity<List<TeamDTO>> getRivalTeams(@PathVariable("teamName")String teamName) {
        return ResponseEntity.ok(this.teamService.getRivalTeams(teamName));
    }

    @Operation(summary = "Get wins per rival", description = "Calculates the number of wins for a specific team against a list of selected rivals.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved wins per rival stats", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = WinsPerRivalDTO.class)))),
            @ApiResponse(responseCode = "404", description = "Team or rivals not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @GetMapping(value = "/{teamName}/analytics/wins-per-rival", produces = "application/json")
    public ResponseEntity<List<WinsPerRivalDTO>> getWinsPerRival(@PathVariable("teamName") String teamName, @RequestParam Set<String> rivalTeamNames) {
        return ResponseEntity.ok(this.teamService.getWinsPerRivals(teamName, rivalTeamNames));
    }

    @Operation(summary = "Get run stats per rival", description = "Returns scoring statistics (runs scored vs runs allowed) between the specified teams.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved run stats", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = RunsStatsDTO.class)))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @GetMapping(value = "/analytics/runs-per-rival", produces = "application/json")
    public ResponseEntity<List<RunsStatsDTO>> getRunsStatsPerRival(@RequestParam Set<String> teams) {
        return ResponseEntity.ok(this.teamService.getRunStatsPerRival(teams));
    }

    @Operation(summary = "Get team win distribution", description = "Returns a percentage breakdown of home vs away wins for a specific team.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved win distribution", content = @Content(mediaType = "application/json", schema = @Schema(implementation = WinDistributionDTO.class))),
            @ApiResponse(responseCode = "404", description = "Team not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @GetMapping(value = "/{teamName}/analytics/win-distribution", produces = "application/json")
    public ResponseEntity<WinDistributionDTO> getWinDistribution(@PathVariable("teamName") String teamName) {
        return ResponseEntity.ok(this.teamService.getWinDistribution(teamName));
    }

    @Operation(summary = "Get historic daily rankings", description = "Returns a chronological map of division rankings, wins, and losses for the selected teams from a starting date.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved historic data", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @GetMapping(value = "/analytics/historic-ranking", produces = "application/json")
    public ResponseEntity<Map<String, List<HistoricRankingDTO>>> getHistoricRanking(@RequestParam Set<String> teams, @RequestParam(required = false)LocalDate dateFrom) {
        return ResponseEntity.ok(this.teamService.getHistoricRanking(teams, dateFrom));
    }

    @Operation(summary = "Hydrate historic ranking data", description = "Admin tool to manually trigger the historical data import for the current season.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hydration process completed successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "403", description = "Unauthorized - Admin access required", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Error during API hydration process", content = @Content(mediaType = "application/json"))
    })
    @PostMapping(value = "/analytics/hydrate", produces = "application/json")
    public ResponseEntity<AuthResponse> hydrateHistoricRanking() {
        this.teamImportService.hydrateHistoryFromStart();
        return ResponseEntity.ok(new AuthResponse(AuthResponse.Status.SUCCESS, "Daily Rankings Updated"));
    }

    @Operation(summary = "Refresh current season standings", description = "Triggers a manual update of the current season standings by fetching the team statistics.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Season Standings successfully updated", content = @Content(mediaType = "application/json",schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error during ranking update"),
    })
    @PostMapping(value = "/sync", produces = "application/json")
    public ResponseEntity<AuthResponse> refreshStandings() {
        this.teamImportService.getTeamStats();
        return ResponseEntity.accepted().body(new AuthResponse(AuthResponse.Status.SUCCESS, "Standings successfully updated!"));
    }

    @Operation(summary = "Update team information", description = "Updates specific fields of an MLB team identified by its name. Supports partial updates such as wins, losses, or team details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Team successfully updated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body or missing required fields", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Team not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @PatchMapping(value = "/{teamName}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<AuthResponse> updateTeam(@PathVariable("teamName") String teamName,
            @RequestBody UpdateTeamRequest request) {
        this.teamService.updateTeam(teamName, request);
        return ResponseEntity.ok(new AuthResponse(AuthResponse.Status.SUCCESS, "Team successfully updated"));
    }
}