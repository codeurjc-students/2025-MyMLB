package com.mlb.mlbportal.services.ticket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mlb.mlbportal.dto.ticket.EditEventRequest;
import com.mlb.mlbportal.dto.ticket.EventCreateRequest;
import com.mlb.mlbportal.dto.ticket.EventManagerDTO;
import com.mlb.mlbportal.dto.ticket.EventResponseDTO;
import com.mlb.mlbportal.dto.ticket.SeatDTO;
import com.mlb.mlbportal.dto.ticket.SectorCreateRequest;
import com.mlb.mlbportal.handler.badRequest.EventRequestMissMatchException;
import com.mlb.mlbportal.handler.notFound.EventNotFoundException;
import com.mlb.mlbportal.handler.notFound.MatchNotFoundException;
import com.mlb.mlbportal.handler.notFound.SectorNotFoundException;
import com.mlb.mlbportal.mappers.ticket.EventMapper;
import com.mlb.mlbportal.mappers.ticket.SeatMapper;
import com.mlb.mlbportal.models.Match;
import com.mlb.mlbportal.models.ticket.Event;
import com.mlb.mlbportal.models.ticket.EventManager;
import com.mlb.mlbportal.models.ticket.Seat;
import com.mlb.mlbportal.models.ticket.Sector;
import com.mlb.mlbportal.repositories.MatchRepository;
import com.mlb.mlbportal.repositories.ticket.EventManagerRepository;
import com.mlb.mlbportal.repositories.ticket.EventRepository;
import com.mlb.mlbportal.repositories.ticket.SeatRepository;
import com.mlb.mlbportal.repositories.ticket.SectorRepository;
import com.mlb.mlbportal.services.utilities.PaginationHandlerService;
import com.mlb.mlbportal.services.utilities.SeatBatchGenerationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final EventManagerRepository eventManagerRepository;
    private final MatchRepository matchRepository;
    private final SectorRepository sectorRepository;
    private final SeatRepository seatRepository;
    private final EventMapper eventMapper;
    private final SeatMapper seatMapper;
    private final PaginationHandlerService paginationHandlerService;
    private final SeatBatchGenerationService seatBatchGenerationService;

    @Transactional(readOnly = true)
    public Page<EventResponseDTO> getAllEvents(int page, int size) {
        List<Event> events = this.eventRepository.findAll();
        return this.paginationHandlerService.paginateAndMap(events, page, size, this.eventMapper::toEventResponseDto);
    }

    @Transactional(readOnly = true)
    public EventResponseDTO getEvent(Long eventId) {
        Event event = this.eventRepository.findEventByIdOrElseThrow(eventId);
        return this.eventMapper.toEventResponseDto(event);
    }

    @Transactional(readOnly = true)
    public Page<EventManagerDTO> getAvailableSectors(Long eventId, int page, int size) {
        this.eventRepository.findEventByIdOrElseThrow(eventId);
        List<EventManager> query = this.eventManagerRepository.findAvailableSectors(eventId);
        return this.paginationHandlerService.paginateAndMap(query, page, size, this.eventMapper::toManagerDto);
    }

    @Transactional(readOnly = true)
    public Page<SeatDTO> getAvailableSeats(Long sectorId, Long eventId, int page, int size) {
        List<Seat> query = this.seatRepository.findAvailableSeats(sectorId, eventId);
        return this.paginationHandlerService.paginateAndMap(query, page, size, this.seatMapper::toSeatDTO);
    }

    @Transactional(readOnly = true)
    public EventResponseDTO getEventByMatchId(Long matchId) {
        this.matchRepository.findById(matchId).orElseThrow(MatchNotFoundException::new);
        Event event = this.eventRepository.findEventByMatchId(matchId).orElseThrow(EventNotFoundException::new);
        return this.eventMapper.toEventResponseDto(event);
    }

    @Transactional
    public EventResponseDTO createEvent(EventCreateRequest request) {
        List<Double> prices = request.prices();
        List<SectorCreateRequest> sectors = request.sectors();
        if (prices.size() != sectors.size()) {
            throw new EventRequestMissMatchException("The number of sectors selected for the creation process does not match the number of new prices");
        }
        Match match = this.matchRepository.findById(request.matchId()).orElseThrow(MatchNotFoundException::new);
        Event newEvent = new Event(match);
        for (int i = 0; i < sectors.size(); i++) {
            SectorCreateRequest sector = sectors.get(i);
            Double price = prices.get(i);
            Sector newSector = new Sector(sector.name(), sector.totalCapacity(), match.getStadium());
            this.sectorRepository.save(newSector);
            List<Seat> newSeats = this.generateSeats(newSector);
            this.seatBatchGenerationService.batchGeneration(newSeats, newSector.getId());
            EventManager manager = new EventManager(newEvent, newSector, price);
            newEvent.addEventManager(manager);
        }
        this.eventRepository.save(newEvent);
        return this.eventMapper.toEventResponseDto(newEvent);
    }

    private List<Seat> generateSeats(Sector sector) {
        String prefix = Arrays.stream(sector.getName().split(" "))
                .map(word -> word.substring(0,1).toUpperCase()).collect(Collectors.joining());

        List<Seat> newSeats = new ArrayList<>(sector.getTotalCapacity());
        for (int i = 1; i <= sector.getTotalCapacity(); i++) {
            String name = prefix + "-" + i;
            newSeats.add(new Seat(name, sector, false));
        }
        return newSeats;
    }

    public EventResponseDTO editEvent(EditEventRequest request) {
        List<Long> sectorIds = request.sectorIds();
        List<Double> prices = request.prices();
        if (sectorIds.size() != prices.size()) {
            throw new EventRequestMissMatchException("The number of sectors selected for editing does not match the number of new prices");
        }
        Event event = this.eventRepository.findEventByIdOrElseThrow(request.eventId());
        for (int i = 0; i < sectorIds.size(); i++) {
            Long sectorId = sectorIds.get(i);
            boolean validId = event.getEventManagers().stream().anyMatch(manager -> manager.getSector().getId() == sectorId);
            if (!validId) {
                throw new SectorNotFoundException(sectorId);
            }
            Double newPrice = prices.get(i);
            event.getEventManagers().stream()
                    .filter(manager -> manager.getSector().getId() == sectorId)
                    .findFirst().ifPresent(manager -> manager.setPrice(newPrice));
        }
        this.eventRepository.save(event);
        return this.eventMapper.toEventResponseDto(event);
    }

    public EventResponseDTO deleteEvent(Long eventId) {
        Event event = this.eventRepository.findEventByIdOrElseThrow(eventId);
        this.eventRepository.delete(event);
        return this.eventMapper.toEventResponseDto(event);
    }
}