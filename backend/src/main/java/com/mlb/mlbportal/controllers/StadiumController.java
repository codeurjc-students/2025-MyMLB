package com.mlb.mlbportal.controllers;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.mlb.mlbportal.dto.picture.PictureDTO;
import com.mlb.mlbportal.dto.stadium.StadiumInitDTO;
import com.mlb.mlbportal.services.StadiumService;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;

@Tag(name = "Stadiums", description = "Operations related to MLB stadiums")
@RestController
@RequestMapping("/api/stadiums")
@AllArgsConstructor
public class StadiumController {
    private final StadiumService stadiumService;

    @Operation(summary = "Get all stadiums", description = "Returns a list of all MLB stadiums with basic information such as name, location, and capacity.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of stadiums", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StadiumInitDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @GetMapping(produces = "application/json")
    public ResponseEntity<List<StadiumInitDTO>> getAllStadiums() {
        List<StadiumInitDTO> response = this.stadiumService.getAllStadiums();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get stadium by name", description = "Returns basic information about a specific MLB stadium identified by its name.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved stadium info", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StadiumInitDTO.class))),
            @ApiResponse(responseCode = "404", description = "Stadium not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @GetMapping(value = "/{name}", produces = "application/json")
    public ResponseEntity<StadiumInitDTO> getStadiumByName(@PathVariable("name") String name) {
        return ResponseEntity.ok(this.stadiumService.findStadiumByName(name));
    }

    @PostMapping("/{stadiumName}/pictures")
    public ResponseEntity<PictureDTO> uploadPicture(@PathVariable("stadiumName") String stadiumName, @RequestParam("file") MultipartFile picturePath) throws IOException {
        return ResponseEntity.ok(this.stadiumService.addPicture(stadiumName, picturePath));
    }

    @DeleteMapping("/{stadiumName}/pictures")
    public ResponseEntity<Void> deletePicture(@PathVariable("stadiumName") String stadiumName, @RequestParam("publicId") String publicId) throws IOException {
        this.stadiumService.deletePicture(stadiumName, publicId);
        return ResponseEntity.noContent().build();
    }
}