package com.mlb.mlbportal.mappers.ticket;

import com.mlb.mlbportal.dto.ticket.EventManagerDTO;
import com.mlb.mlbportal.dto.ticket.EventResponseDTO;
import com.mlb.mlbportal.models.ticket.Event;
import com.mlb.mlbportal.models.ticket.EventManager;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring")
public interface EventMapper {
    @Mapping(target = "awayTeamName", source = "match.awayTeam.name")
    @Mapping(target = "homeTeamName", source = "match.homeTeam.name")
    @Mapping(target = "stadiumName", source = "stadium.name")
    @Mapping(target = "date", source = "match.date")
    @Mapping(target = "sectors", source = "sectorManagers")
    EventResponseDTO toEventResponseDto(Event event);

    @Mapping(target = "sectorName", source = "sector.name")
    @Mapping(target = "totalCapacity", source = "sector.totalCapacity")
    EventManagerDTO toManagerDto(EventManager eventManager);

    List<EventManagerDTO> toListManagerDTO(Collection<EventManager> list);
}