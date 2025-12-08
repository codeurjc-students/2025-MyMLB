package com.mlb.mlbportal.controllers;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mlb.mlbportal.dto.match.MatchDTO;
import com.mlb.mlbportal.services.MatchService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;

@Tag(name = "Matches", description = "Operations related to the matches")
@RestController
@RequestMapping("/api/v1/matches")
@AllArgsConstructor
public class MatchController {
	private final MatchService matchService;

	@Operation(summary = "Get the matches of the day (paginated)", description = "Returns a paginated list of matches scheduled for today. Always 10 matches per page.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully retrieved the matches", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MatchDTO.class))),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
	})
	@GetMapping(value = "/today", produces = "application/json")
	public ResponseEntity<Page<MatchDTO>> getMatchesOfTheDayPaginated(
			Principal principal,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {

		String username = (principal != null) ? principal.getName() : null;
		Page<MatchDTO> matches = this.matchService.getMatchesOfTheDay(username, page, size);
		return ResponseEntity.ok(matches);
	}

	@Operation(summary = "Get matches of a team (home/away)", description = "Returns a paginated list of matches for the specified team, filtered by location (home or away).")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully retrieved matches", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MatchDTO.class))),
			@ApiResponse(responseCode = "400", description = "Invalid location parameter (must be 'home' or 'away')", content = @Content(mediaType = "application/json")),
			@ApiResponse(responseCode = "404", description = "Team not found or no matches available", content = @Content(mediaType = "application/json")),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
	})
	@GetMapping(value = "/{teamName}", produces = "application/json")
	public ResponseEntity<Page<MatchDTO>> getMatchesOfATeam(@PathVariable("teamName") String teamName,
			@RequestParam String location, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		if ("home".equalsIgnoreCase(location)) {
			return ResponseEntity.ok(this.matchService.getHomeMatches(teamName, page, size));
		} else if ("away".equalsIgnoreCase(location)) {
			return ResponseEntity.ok(this.matchService.getAwayMatches(teamName, page, size));
		} else {
			return ResponseEntity.badRequest().build();
		}
	}

	@Operation(summary = "Get matches of a team by month", description = "Returns all matches of the specified team within a given year and month.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully retrieved matches", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MatchDTO.class))),
			@ApiResponse(responseCode = "404", description = "Team not found or no matches available for the given month", content = @Content(mediaType = "application/json")),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
	})
	@GetMapping(value = "/team/{teamName}", produces = "application/json")
	public ResponseEntity<List<MatchDTO>> getMatchesOfTeamByMonth(@PathVariable String teamName,
			@RequestParam int year, @RequestParam int month) {
		LocalDate start = LocalDate.of(year, month, 1);
		LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
		return ResponseEntity.ok(this.matchService.getMatchesOfTeamBetweenDates(teamName, start, end));
	}
}