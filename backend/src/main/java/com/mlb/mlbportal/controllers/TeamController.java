package com.mlb.mlbportal.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mlb.mlbportal.dto.team.TeamDTO;
import com.mlb.mlbportal.dto.team.TeamInfoDTO;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;
import com.mlb.mlbportal.services.TeamService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

@Tag(name = "Teams", description = "Operations related to MLB teams and standings")
@RestController
@RequestMapping("/api/teams")
@AllArgsConstructor
public class TeamController {
    private final TeamService teamService;

    @Operation(summary = "Get all teams", description = "Returns a list of all MLB teams with calculated stats, including wins, losses, total games, and win percentage.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of teams", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamDTO.class), examples = @ExampleObject(value = "[{\"name\":\"Yankees\",\"abbreviation\":\"NYY\",\"logo\":\"url\",\"league\":\"AL\",\"division\":\"EAST\",\"totalGames\":162,\"wins\":95,\"losses\":67,\"pct\":0.586,\"gamesBack\":0.0,\"streak\":\"W3\"}]"))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @GetMapping(produces = "application/json")
    public ResponseEntity<List<TeamInfoDTO>> getAllTeams() {
        List<TeamInfoDTO> teams = this.teamService.getTeams();
        return ResponseEntity.ok(teams);
    }

    @Operation(summary = "Get team standings", description = "Returns standings grouped by league and division, ordered by win percentage. Each team includes stats such as total games, wins, losses, win percentage, games back, and current streak.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved standings", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamDTO.class), examples = @ExampleObject(value = "{\"AL\":{\"EAST\":[{\"name\":\"Yankees\",\"abbreviation\":\"NYY\",\"logo\":\"url\",\"league\":\"AL\",\"division\":\"EAST\",\"totalGames\":162,\"wins\":95,\"losses\":67,\"pct\":0.586,\"gamesBack\":0.0,\"streak\":\"W3\"}]}}"))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @GetMapping(value = "/standings", produces = "application/json")
    public ResponseEntity<Map<League, Map<Division, List<TeamDTO>>>> getStandings() {
        Map<League, Map<Division, List<TeamDTO>>> standings = this.teamService.getStandings();
        return ResponseEntity.ok(standings);
    }

    @GetMapping("/{teamName}")
    public ResponseEntity<TeamInfoDTO> getMethodName(@PathVariable("teamName") String teamName) {
       return ResponseEntity.ok(this.teamService.getTeamInfo(teamName));
    }
}