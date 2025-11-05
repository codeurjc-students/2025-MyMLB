package com.mlb.mlbportal.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mlb.mlbportal.dto.player.PitcherDTO;
import com.mlb.mlbportal.dto.player.PitcherSummaryDTO;
import com.mlb.mlbportal.dto.player.PlayerDTO;
import com.mlb.mlbportal.dto.player.PositionPlayerDTO;
import com.mlb.mlbportal.dto.player.PositionPlayerSummaryDTO;
import com.mlb.mlbportal.services.player.PlayerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

@Tag(name = "Players", description = "Operations related to MLB players, including position players and pitchers")
@RestController
@RequestMapping("/api/players")
@AllArgsConstructor
public class PlayerController {
    private final PlayerService playerService;

    @Operation(summary = "Get all players", description = "Returns a list of all MLB players, including both position players and pitchers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of players", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PlayerDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @GetMapping
    public ResponseEntity<List<PlayerDTO>> getAllPlayers() {
        return ResponseEntity.ok(this.playerService.getAllPlayers());
    }

    @Operation(summary = "Get all position players", description = "Returns a list of all MLB position players with detailed stats.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of position players", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PositionPlayerDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/position-players")
    public ResponseEntity<List<PositionPlayerDTO>> getAllPositionPlayers() {
        return ResponseEntity.ok(this.playerService.getAllPositionPlayers());
    }

    @Operation(summary = "Get all pitchers", description = "Returns a list of all MLB pitchers with detailed stats.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of pitchers", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PitcherDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/pitchers")
    public ResponseEntity<List<PitcherDTO>> getAllPitchers() {
        return ResponseEntity.ok(this.playerService.getAllPitchers());
    }

    @Operation(summary = "Get player by name", description = "Returns detailed information about a specific player identified by name.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved player info", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PlayerDTO.class))),
            @ApiResponse(responseCode = "404", description = "Player not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/{name}")
    public ResponseEntity<PlayerDTO> getPlayerByName(@PathVariable("name") String name) {
        return ResponseEntity.ok(this.playerService.findPlayerByName(name));
    }

    @Operation(summary = "Get position players of a team", description = "Returns a paginated list of position players for a specific team, ordered alphabetically.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved position players of the team", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PositionPlayerSummaryDTO.class))),
            @ApiResponse(responseCode = "404", description = "Team not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/position-players/{teamName}")
    public ResponseEntity<Page<PositionPlayerSummaryDTO>> getPositionPlayersOfATeam(
            @PathVariable("teamName") String teamName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(this.playerService.getAllPositionPlayersOfATeam(teamName, page, size));
    }

    @Operation(summary = "Get pitchers of a team", description = "Returns a paginated list of pitchers for a specific team, ordered alphabetically.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved pitchers of the team", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PitcherSummaryDTO.class))),
            @ApiResponse(responseCode = "404", description = "Team not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/pitchers/{teamName}")
    public ResponseEntity<Page<PitcherSummaryDTO>> getPitchersOfATeam(
            @PathVariable("teamName") String teamName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(this.playerService.getAllPitchersOfATeam(teamName, page, size));
    }
}