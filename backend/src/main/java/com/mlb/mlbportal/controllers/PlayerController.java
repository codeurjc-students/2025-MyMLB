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

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/players")
@AllArgsConstructor
public class PlayerController {
    private final PlayerService playerService;

    @GetMapping
    public ResponseEntity<List<PlayerDTO>> getAllPlayers() {
        return ResponseEntity.ok(this.playerService.getAllPlayers());
    }

    @GetMapping("/position-players")
    public ResponseEntity<List<PositionPlayerDTO>> getAllPositionPlayers() {
        return ResponseEntity.ok(this.playerService.getAllPositionPlayers());
    }

    @GetMapping("/pitchers")
    public ResponseEntity<List<PitcherDTO>> getAllPitchers() {
        return ResponseEntity.ok(this.playerService.getAllPitchers());
    }

    @GetMapping("/{name}")
    public ResponseEntity<PlayerDTO> getPlayerByName(@PathVariable("name") String name) {
        return ResponseEntity.ok(this.playerService.findPlayerByName(name));
    }

    @GetMapping("/position-players/{teamName}")
    public ResponseEntity<Page<PositionPlayerSummaryDTO>> getPositionPlayersOfATeam(
            @PathVariable("teamName") String teamName, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (page < 0 || size <= 0) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(this.playerService.getAllPositionPlayersOfATeam(teamName, page, size));
    }

    @GetMapping("/pitchers/{teamName}")
    public ResponseEntity<Page<PitcherSummaryDTO>> getPitchersOfATeam(
            @PathVariable("teamName") String teamName, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (page < 0 || size <= 0) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(this.playerService.getAllPitchersOfATeam(teamName, page, size));
    }
}