package com.mlb.mlbportal.mappers;

import com.mlb.mlbportal.dto.support.SupportMessageDTO;
import com.mlb.mlbportal.models.support.SupportMessage;
import org.mapstruct.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring")
public interface SupportMessageMapper {
    SupportMessageDTO toSupportMessageDTO(SupportMessage message);

    List<SupportMessageDTO> toListSupportMessageDTO(Collection<SupportMessage> list);
}