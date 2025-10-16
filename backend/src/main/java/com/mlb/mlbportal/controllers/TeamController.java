package com.mlb.mlbportal.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mlb.mlbportal.services.TeamService;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import com.mlb.mlbportal.dto.team.TeamDTO;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;

@Tag(name = "Teams", description = "Operations related to MLB teams and standings")
@RestController
@RequestMapping("/api/teams")
public class TeamController {
    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @Operation(summary = "Get all teams", description = "Returns a list of all MLB teams with calculated stats")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of teams"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<TeamDTO>> getAllTeams() {
        List<TeamDTO> teams = this.teamService.getTeams();
        return ResponseEntity.ok(teams);
    }

    @Operation(summary = "Get team standings", description = "Returns standings grouped by league and division, ordered by win percentage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved standings"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/standings")
    public ResponseEntity<Map<League, Map<Division, List<TeamDTO>>>> getStandings() {
        Map<League, Map<Division, List<TeamDTO>>> standings = this.teamService.getStandings();
        return ResponseEntity.ok(standings);
    }
}