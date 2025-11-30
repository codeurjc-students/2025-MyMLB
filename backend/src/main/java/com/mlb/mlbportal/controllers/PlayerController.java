package com.mlb.mlbportal.controllers;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import com.mlb.mlbportal.dto.player.pitcher.CreatePitcherRequest;
import com.mlb.mlbportal.dto.player.pitcher.EditPitcherRequest;
import com.mlb.mlbportal.dto.player.position.CreatePositionPlayerRequest;
import com.mlb.mlbportal.dto.player.position.EditPositionPlayerRequest;
import com.mlb.mlbportal.models.others.PictureInfo;
import com.mlb.mlbportal.security.jwt.AuthResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mlb.mlbportal.dto.player.PlayerDTO;
import com.mlb.mlbportal.dto.player.pitcher.PitcherDTO;
import com.mlb.mlbportal.dto.player.pitcher.PitcherSummaryDTO;
import com.mlb.mlbportal.dto.player.position.PositionPlayerDTO;
import com.mlb.mlbportal.dto.player.position.PositionPlayerSummaryDTO;
import com.mlb.mlbportal.services.player.PlayerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Tag(name = "Players", description = "Operations related to MLB players, including position players and pitchers")
@RestController
@RequestMapping("/api/v1/players")
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

    @Operation(summary = "Create a position player", description = "Creates a new MLB position player and assigns them to a team.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Player successfully created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PositionPlayerDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
            @ApiResponse(responseCode = "404", description = "Team not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Roster full or player already exists", content = @Content)
    })
    @PostMapping(value = "/position-players", consumes = "application/json", produces = "application/json")
    public ResponseEntity<PositionPlayerDTO> createPositionPlayer(@Valid @RequestBody CreatePositionPlayerRequest request) {
        PositionPlayerDTO player = this.playerService.createPositionPlayer(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/v1/players/{name}")
                .buildAndExpand(player.name())
                .toUri();
        return ResponseEntity.created(location).body(player);
    }

    @Operation(summary = "Create a pitcher", description = "Creates a new MLB pitcher and assigns them to a team.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Player successfully created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PitcherDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
            @ApiResponse(responseCode = "404", description = "Team not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Roster full or player already exists", content = @Content)
    })
    @PostMapping(value = "/pitchers", consumes = "application/json", produces = "application/json")
    public ResponseEntity<PitcherDTO> createPitcher(@Valid @RequestBody CreatePitcherRequest request) {
        PitcherDTO player = this.playerService.createPitcher(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/v1/players/{name}")
                .buildAndExpand(player.name())
                .toUri();
        return ResponseEntity.created(location).body(player);
    }

    @Operation(summary = "Upload player picture", description = "Uploads or updates the profile picture of a specific player.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Picture successfully uploaded", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PictureInfo.class))),
            @ApiResponse(responseCode = "404", description = "Player not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid file format", content = @Content)
    })
    @PostMapping(value = "/{playerName}/pictures", consumes = "multipart/form-data", produces = "application/json")
    public ResponseEntity<PictureInfo> uploadPicture(@PathVariable("playerName") String playerName, @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(this.playerService.updatePicture(playerName, file));
    }

    @Operation(summary = "Update position player", description = "Partially updates the stats or team assignment of a position player.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Player successfully updated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "404", description = "Player not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Roster full", content = @Content)
    })
    @PatchMapping(value = "/position-players/{playerName}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<AuthResponse> updatePositionPlayer(@PathVariable("playerName") String playerName,
                                                             @RequestBody EditPositionPlayerRequest request) {
        this.playerService.updatePositionPlayer(playerName, request);
        return ResponseEntity.ok(new AuthResponse(AuthResponse.Status.SUCCESS, "Player successfully updated"));
    }

    @Operation(summary = "Update pitcher", description = "Partially updates the stats or team assignment of a pitcher.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Player successfully updated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "404", description = "Player not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Roster full", content = @Content)
    })
    @PatchMapping(value = "/pitchers/{playerName}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<AuthResponse> updatePitcher(@PathVariable("playerName") String playerName,
                                                      @RequestBody EditPitcherRequest request) {
        this.playerService.updatePitcher(playerName, request);
        return ResponseEntity.ok(new AuthResponse(AuthResponse.Status.SUCCESS, "Player successfully updated"));
    }

    @Operation(summary = "Delete player", description = "Deletes a player by name and returns their details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Player successfully deleted", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PlayerDTO.class))),
            @ApiResponse(responseCode = "404", description = "Player not found", content = @Content)
    })
    @DeleteMapping(value = "/{playerName}", produces = "application/json")
    public ResponseEntity<PlayerDTO> deletePlayer(@PathVariable("playerName") String playerName) {
        return ResponseEntity.ok(this.playerService.deletePlayer(playerName));
    }
}