package com.mlb.mlbportal.controllers.ticket;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.mlb.mlbportal.dto.ticket.EditEventRequest;
import com.mlb.mlbportal.dto.ticket.EventCreateRequest;
import com.mlb.mlbportal.dto.ticket.EventManagerDTO;
import com.mlb.mlbportal.dto.ticket.EventResponseDTO;
import com.mlb.mlbportal.dto.ticket.SeatDTO;
import com.mlb.mlbportal.dto.ticket.TicketDTO;
import com.mlb.mlbportal.services.ticket.EventService;
import com.mlb.mlbportal.services.ticket.TicketService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Events", description = "Operations for managing match events, including seating, pricing, and sector configurations")
@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;
    private final TicketService ticketService;

    @Operation(summary = "Return all active events", description = "Retrieve all active events")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the events", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @GetMapping(produces = "application/json")
    public ResponseEntity<Page<EventResponseDTO>> getAllEvents(@RequestParam(defaultValue = "0")int page, @RequestParam(defaultValue = "10")int size) {
        return ResponseEntity.ok(this.eventService.getAllEvents(page, size));
    }

    @Operation(summary = "Return a certain event", description = "Return the event whose ID is provided")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the event", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Event Not Found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @GetMapping(value = "/{eventId}", produces = "application/json")
    public ResponseEntity<EventResponseDTO> getEvent(@PathVariable("eventId")Long eventId) {
        return ResponseEntity.ok(this.eventService.getEvent(eventId));
    }

    @Operation(summary = "Return available sectors of an event", description = "Retrieve all the available sectors of the given event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the sectors", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventManagerDTO.class))),
            @ApiResponse(responseCode = "404", description = "Event Not Found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @GetMapping(value = "/{eventId}/sectors", produces = "application/json")
    public ResponseEntity<Page<EventManagerDTO>> getAvailableSectors(@PathVariable("eventId")Long eventId,@RequestParam(defaultValue = "0")int page, @RequestParam(defaultValue = "10")int size) {
        return ResponseEntity.ok(this.eventService.getAvailableSectors(eventId, page, size));
    }

    @Operation(summary = "Return available seats of an event", description = "Retrieve all the available seats of a given sector and event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the seats", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SeatDTO.class))),
            @ApiResponse(responseCode = "404", description = "Event or Sector Not Found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @GetMapping(value = "/{eventId}/sector/{sectorId}", produces = "application/json")
    public ResponseEntity<Page<SeatDTO>> getAvailableSeats(@PathVariable("eventId")Long eventId,
                                                           @PathVariable("sectorId")Long sectorId,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(this.eventService.getAvailableSeats(sectorId, eventId, page, size));
    }

    @Operation(summary = "Return the event of a certain match", description = "Returns the event belonging to the match whose ID is provided")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the event", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Match Not Found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @GetMapping(value = "/match/{matchId}", produces = "application/json")
    public ResponseEntity<EventResponseDTO> getEventByMatchId(@PathVariable("matchId")Long matchId) {
        return ResponseEntity.ok(this.eventService.getEventByMatchId(matchId));
    }

    @Operation(summary = "Get all sold tickets of an event", description = "Retrieves all sold tickets of a certain event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the tickets", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TicketDTO.class))),
            @ApiResponse(responseCode = "404", description = "Event Not Found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @GetMapping(value = "/{eventId}/tickets", produces = "application/json")
    public ResponseEntity<Page<TicketDTO>> getTicketsOfEvent(@PathVariable("eventId")Long eventId, @RequestParam(defaultValue = "0")int page, @RequestParam(defaultValue = "10")int size) {
        return ResponseEntity.ok(this.ticketService.getTicketsOfEvent(eventId, page, size));
    }

    @Operation(summary = "Creates an Event", description = "Allows the admins to create an event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created the event", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "The number of sectors selected for the creation process does not match the number of new prices", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Match Not Found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<EventResponseDTO> createEvent(@Valid @RequestBody EventCreateRequest request) {
        EventResponseDTO response = this.eventService.createEvent(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{eventId}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Edit an Event", description = "Allows the admins to edit the prices of the sectors within a given event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully modified the event", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "The number of sectors selected for editing does not match the number of new prices", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Event or Sector Not Found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @PutMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<EventResponseDTO> editEvent(@Valid @RequestBody EditEventRequest request) {
        return ResponseEntity.ok(this.eventService.editEvent(request));
    }

    @Operation(summary = "Delete an Event", description = "Allows the admins to delete the given event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deleted the event", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Event Not Found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @DeleteMapping(value = "/{eventId}", produces = "application/json")
    public ResponseEntity<EventResponseDTO> deleteEvent(@PathVariable("eventId")Long eventId) {
        return ResponseEntity.ok(this.eventService.deleteEvent(eventId));
    }
}