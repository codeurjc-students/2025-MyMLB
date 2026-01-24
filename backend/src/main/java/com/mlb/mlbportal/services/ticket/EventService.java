package com.mlb.mlbportal.services.ticket;

import com.mlb.mlbportal.dto.ticket.EventManagerDTO;
import com.mlb.mlbportal.dto.ticket.EventResponseDTO;
import com.mlb.mlbportal.handler.notFound.EventNotFoundException;
import com.mlb.mlbportal.handler.notFound.MatchNotFoundException;
import com.mlb.mlbportal.mappers.ticket.EventMapper;
import com.mlb.mlbportal.models.ticket.Event;
import com.mlb.mlbportal.models.ticket.EventManager;
import com.mlb.mlbportal.repositories.MatchRepository;
import com.mlb.mlbportal.repositories.ticket.EventManagerRepository;
import com.mlb.mlbportal.repositories.ticket.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final EventManagerRepository eventManagerRepository;
    private final MatchRepository matchRepository;
    private final EventMapper eventMapper;

    @Transactional(readOnly = true)
    public EventResponseDTO getEvent(Long eventId) {
        Event event = this.eventRepository.findEventByIdOrElseThrow(eventId);
        return this.eventMapper.toEventResponseDto(event);
    }

    @Transactional(readOnly = true)
    public List<EventManagerDTO> getAvailableSectors(Long eventId) {
        this.eventRepository.findEventByIdOrElseThrow(eventId);
        List<EventManager> query = this.eventManagerRepository.findAvailableSectors(eventId);
        return this.eventMapper.toListManagerDTO(query);
    }

    @Transactional(readOnly = true)
    public EventResponseDTO getEventByMatchId(Long matchId) {
        this.matchRepository.findById(matchId).orElseThrow(MatchNotFoundException::new);
        Event event = this.eventRepository.findEventByMatchId(matchId).orElseThrow(EventNotFoundException::new);
        return this.eventMapper.toEventResponseDto(event);
    }
}