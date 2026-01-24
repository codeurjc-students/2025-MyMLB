package com.mlb.mlbportal.mappers.ticket;

import com.mlb.mlbportal.dto.ticket.TicketDTO;
import com.mlb.mlbportal.models.ticket.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring")
public interface TicketMapper {
    @Mapping(target = "awayTeamName", source = "eventManager.event.match.awayTeam.name")
    @Mapping(target = "homeTeamName", source = "eventManager.event.match.homeTeam.name")
    @Mapping(target = "stadiumName", source = "eventManager.event.stadium.name")
    @Mapping(target = "price", source = "eventManager.price")
    @Mapping(target = "matchDate", source = "eventManager.event.match.date")
    TicketDTO toTicketDTO(Ticket ticket);

    List<TicketDTO> toListTicketDTO(Collection<Ticket> list);
}