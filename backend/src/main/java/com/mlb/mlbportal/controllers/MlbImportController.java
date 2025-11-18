package com.mlb.mlbportal.controllers;

import com.mlb.mlbportal.dto.match.MatchDTO;
import com.mlb.mlbportal.services.MlbImportService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "MLB Official Import", description = "Endpoints to fetch MLB official data from statsapi.mlb.com")
@RestController
@RequestMapping("/api/mlb")
@RequiredArgsConstructor
public class MlbImportController {

    private final MlbImportService mlbImportService;

    @Operation(summary = "Fetch official MLB matches from the MLB Stats API", description = "Returns matches between the given date range directly from the official MLB StatsAPI")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Matches retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MatchDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error"),
            @ApiResponse(responseCode = "503", description = "MLB statsAPI Unavailable")
    })
    @GetMapping("/schedule")
    public ResponseEntity<List<MatchDTO>> getOfficialSchedule(@RequestParam LocalDate from, @RequestParam LocalDate to) {
        List<MatchDTO> matches = mlbImportService.getOfficialMatches(from, to);
        return ResponseEntity.ok(matches);
    }
}