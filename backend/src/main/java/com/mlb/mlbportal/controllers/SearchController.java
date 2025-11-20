package com.mlb.mlbportal.controllers;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mlb.mlbportal.handler.InvalidSearchTypeException;
import com.mlb.mlbportal.services.SearchService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("api/searchs")
@AllArgsConstructor
public class SearchController {
    private final SearchService searchService;

    @GetMapping("/{type}")
    public ResponseEntity<Page<?>> searchStadium(@PathVariable("type") String type, @RequestParam String query,
            @RequestParam(required = false) String playerType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "0") int size) {

        switch (type.toLowerCase()) {
            case "stadium" -> {
                return ResponseEntity.ok(searchService.searchStadiums(query, page, size));
            }
            case "team" -> {
                return ResponseEntity.ok(searchService.searchTeams(query, page, size));
            }
            case "player" -> {
                if ("position".equalsIgnoreCase(playerType)) {
                    return ResponseEntity.ok(searchService.searchPositionPlayers(query, page, size));
                } 
                else if ("pitcher".equalsIgnoreCase(playerType)) {
                    return ResponseEntity.ok(searchService.searchPitchers(query, page, size));
                } 
                else {
                    throw new InvalidSearchTypeException("Missing or invalid playerType parameter");
                }
            }
            default -> throw new InvalidSearchTypeException("Invalid search type: "+ type);
        }
    }
}