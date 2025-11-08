package com.mlb.mlbportal.controllers;

import java.security.Principal;
import java.util.List;
import java.util.Set;

import com.mlb.mlbportal.dto.team.TeamSummary;
import com.mlb.mlbportal.security.jwt.AuthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mlb.mlbportal.dto.user.ShowUser;
import com.mlb.mlbportal.services.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<ShowUser> getUsers() {
        return this.userService.getAllUsers();
    }

    @GetMapping("/fav-teams")
    public ResponseEntity<Set<TeamSummary>> getFavouriteTeams(Principal principal) {
        return ResponseEntity.ok(this.userService.getFavTeamsOfAUser(principal.getName()));
    }

    @PostMapping("/fav-teams/{teamName}")
    public ResponseEntity<AuthResponse> addFavouriteTeam(Principal principal, @PathVariable("teamName") String teamName) {
        this.userService.addFavTeam(principal.getName(), teamName);
        return ResponseEntity.ok(new AuthResponse(AuthResponse.Status.SUCCESS, "Team Succesfullly Added"));
    }

    @DeleteMapping("/fav-teams/{teamName}")
    public ResponseEntity<AuthResponse> removeFavouriteTeam(Principal principal, @PathVariable("teamName") String teamName) {
        this.userService.removeFavTeam(principal.getName(), teamName);
        return ResponseEntity.ok(new AuthResponse(AuthResponse.Status.SUCCESS, "Team Succesfullly Remove"));
    }
}