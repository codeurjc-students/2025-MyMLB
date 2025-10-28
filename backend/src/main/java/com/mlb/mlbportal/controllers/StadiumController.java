package com.mlb.mlbportal.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mlb.mlbportal.dto.stadium.StadiumInitDTO;
import com.mlb.mlbportal.services.StadiumService;

@RestController
@RequestMapping("/api/stadiums")
public class StadiumController {
    private final StadiumService stadiumService;

    public StadiumController(StadiumService service) {
        this.stadiumService = service;
    }

    @GetMapping
    public ResponseEntity<List<StadiumInitDTO>> getAllStadiums() {
        List<StadiumInitDTO> response = this.stadiumService.getAllStadiums();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{name}")
    public ResponseEntity<StadiumInitDTO> getStadiumByName(@PathVariable("name") String name) {
        return ResponseEntity.ok(this.stadiumService.findStadiumByName(name));
    }
}