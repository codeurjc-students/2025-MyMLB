package com.mlb.mlbportal.controllers;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mlb.mlbportal.dto.match.MatchDTO;
import com.mlb.mlbportal.services.MatchService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Matches", description = "Operations related to the matches")
@RestController
@RequestMapping("/api/matches")
public class MatchController {
    private final MatchService matchService;
    private static final Logger log = LoggerFactory.getLogger(MatchController.class);

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @Operation(summary = "Get the matches of the day (paginated)", description = "Returns a paginated list of matches scheduled for today. Always 10 matches per page.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the matches", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MatchDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @GetMapping(value = "/today", produces = "application/json")
    public ResponseEntity<Page<MatchDTO>> getMatchesOfTheDayPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/matches/today invoked with page={} size={}", page, size);

        try {
            Page<MatchDTO> result = matchService.getMatchesOfTheDay(page, size);
            log.info("Returning {} matches", result.getTotalElements());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Exception in getMatchesOfTheDay: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}