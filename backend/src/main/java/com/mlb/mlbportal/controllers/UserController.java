package com.mlb.mlbportal.controllers;

import java.security.Principal;
import java.util.Set;

import com.mlb.mlbportal.dto.user.EditProfileRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mlb.mlbportal.dto.team.TeamSummary;
import com.mlb.mlbportal.dto.user.ShowUser;
import com.mlb.mlbportal.security.jwt.AuthResponse;
import com.mlb.mlbportal.services.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Users", description = "Operations related to MLB Portal users and their favorite teams")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get all users", description = "Returns a list of all registered users in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of users", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ShowUser.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @GetMapping(produces = "application/json")
    public ResponseEntity<Page<ShowUser>> getUsers(@RequestParam(defaultValue = "0")int page, @RequestParam(defaultValue = "10")int size) {
        return ResponseEntity.ok(this.userService.getAllUsers(page, size));
    }

    @Operation(summary = "Get favorite teams of a user", description = "Returns the list of favorite MLB teams for the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved favorite teams", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamSummary.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @GetMapping(value = "/favorites/teams", produces = "application/json")
    public ResponseEntity<Set<TeamSummary>> getFavoriteTeams(Principal principal) {
        return ResponseEntity.ok(this.userService.getFavTeamsOfAUser(principal.getName()));
    }

    @Operation(summary = "Edit the authenticated user's profile", description = "Updates the profile of the authenticated user, allowing them to modify their email and password.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile successfully updated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ShowUser.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @PatchMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<ShowUser> editProfile(Principal principal, @Valid @RequestBody EditProfileRequest request) {
        return ResponseEntity.ok(this.userService.updateProfile(principal.getName(), request));
    }

    @Operation(summary = "Add a favorite team", description = "Adds a team to the authenticated user's list of favorites.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Team successfully added", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Team not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @PostMapping(value = "/favorites/teams/{teamName}", produces = "application/json")
    public ResponseEntity<AuthResponse> addFavoriteTeam(Principal principal,
            @PathVariable("teamName") String teamName) {
        this.userService.addFavTeam(principal.getName(), teamName);
        return ResponseEntity.ok(new AuthResponse(AuthResponse.Status.SUCCESS, "Team Succesfullly Added"));
    }

    @Operation(summary = "Remove a favorite team", description = "Removes a team from the authenticated user's list of favorites.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Team successfully removed", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Team not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @DeleteMapping(value = "/favorites/teams/{teamName}", produces = "application/json")
    public ResponseEntity<AuthResponse> removeFavoriteTeam(Principal principal,
            @PathVariable("teamName") String teamName) {
        this.userService.removeFavTeam(principal.getName(), teamName);
        return ResponseEntity.ok(new AuthResponse(AuthResponse.Status.SUCCESS, "Team Successfully Remove"));
    }
}