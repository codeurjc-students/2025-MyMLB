package com.mlb.mlbportal.mappers;

import com.mlb.mlbportal.dto.support.SupportTicketDTO;
import com.mlb.mlbportal.models.support.SupportTicket;
import org.mapstruct.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring")
public interface SupportTicketMapper {
    SupportTicketDTO toSupportThreadDTO(SupportTicket thread);

    List<SupportTicketDTO> toListSupportThreadDTO(Collection<SupportTicket> list);
}