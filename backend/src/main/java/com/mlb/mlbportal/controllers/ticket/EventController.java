package com.mlb.mlbportal.controllers.ticket;

import com.mlb.mlbportal.dto.ticket.*;
import com.mlb.mlbportal.services.ticket.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    @GetMapping(produces = "application/json")
    public ResponseEntity<List<EventResponseDTO>> getAllEvents() {
        return ResponseEntity.ok(this.eventService.getAllEvents());
    }

    @GetMapping(value = "/{eventId}", produces = "application/json")
    public ResponseEntity<EventResponseDTO> getEvent(@PathVariable("eventId")Long eventId) {
        return ResponseEntity.ok(this.eventService.getEvent(eventId));
    }

    @GetMapping(value = "/{eventId}/sectors", produces = "application/json")
    public ResponseEntity<List<EventManagerDTO>> getAvailableSectors(@PathVariable("eventId")Long eventId) {
        return ResponseEntity.ok(this.eventService.getAvailableSectors(eventId));
    }

    @GetMapping(value = "/{eventId}/sector/{sectorId}", produces = "application/json")
    public ResponseEntity<Page<SeatDTO>> getAvailableSeats(@PathVariable("eventId")Long eventId,
                                                           @PathVariable("sectorId")Long sectorId,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(this.eventService.getAvailableSeats(sectorId, eventId, page, size));
    }

    @GetMapping(value = "/match/{matchId}", produces = "application/json")
    public ResponseEntity<EventResponseDTO> getEventByMatchId(@PathVariable("matchId")Long matchId) {
        return ResponseEntity.ok(this.eventService.getEventByMatchId(matchId));
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<EventResponseDTO> createEvent(@Valid @RequestBody EventCreateRequest request) {
        EventResponseDTO response = this.eventService.createEvent(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/{eventId}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<EventResponseDTO> editEvent(@Valid @RequestBody EditEventRequest request) {
        return ResponseEntity.ok(this.eventService.editEvent(request));
    }
}