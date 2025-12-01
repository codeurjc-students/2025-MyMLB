package com.mlb.mlbportal.controllers;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import com.mlb.mlbportal.dto.team.TeamSummary;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mlb.mlbportal.dto.team.TeamDTO;
import com.mlb.mlbportal.dto.team.TeamInfoDTO;
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

@Tag(name = "Teams", description = "Operations related to MLB teams and standings")
@RestController
@RequestMapping("/api/v1/teams")
@AllArgsConstructor
public class TeamController {
    private final TeamService teamService;

    @Operation(summary = "Get all teams", description = "Returns a list of all MLB teams with calculated stats, including wins, losses, total games, and win percentage.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of teams", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @GetMapping(produces = "application/json")
    public ResponseEntity<List<TeamInfoDTO>> getAllTeams() {
        List<TeamInfoDTO> teams = this.teamService.getTeams();
        return ResponseEntity.ok(teams);
    }

    @GetMapping(value = "/available", produces = "application/json")
    public ResponseEntity<Page<TeamSummary>> getAvailableTeams(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(this.teamService.getAvailableTeams(page, size));
    }

    @Operation(summary = "Get team standings", description = "Returns standings grouped by league and division, ordered by win percentage. Each team includes stats such as total games, wins, losses, win percentage, games back, and current streak.")
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

    @Operation(summary = "Get team info by name", description = "Returns detailed information about a specific MLB team, including abbreviation, logo, league, division, total games, wins, losses, win percentage, etc.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved team info", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamInfoDTO.class))),
            @ApiResponse(responseCode = "404", description = "Team not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @GetMapping(value = "/{teamName}", produces = "application/json")
    public ResponseEntity<TeamInfoDTO> getTeamInfo(@PathVariable("teamName") String teamName) {
        return ResponseEntity.ok(this.teamService.getTeamInfo(teamName));
    }

    @Operation(summary = "Update team information", description = "Updates specific fields of an MLB team identified by its name. Supports partial updates such as wins, losses, or team details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Team successfully updated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body or missing required fields", content = @Content(mediaType = "application/json")),
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