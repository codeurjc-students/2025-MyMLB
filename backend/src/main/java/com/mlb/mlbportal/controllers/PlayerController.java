package com.mlb.mlbportal.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mlb.mlbportal.dto.player.PlayerDTO;
import com.mlb.mlbportal.dto.player.PositionPlayerDTO;
import com.mlb.mlbportal.services.player.PlayerService;

import lombok.AllArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.mlb.mlbportal.dto.player.PitcherDTO;

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
}