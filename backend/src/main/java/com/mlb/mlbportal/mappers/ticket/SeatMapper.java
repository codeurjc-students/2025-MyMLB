package com.mlb.mlbportal.mappers.ticket;

import com.mlb.mlbportal.dto.ticket.SeatDTO;
import com.mlb.mlbportal.models.ticket.Seat;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SeatMapper {
    SeatDTO toSeatDTO(Seat seat);
}