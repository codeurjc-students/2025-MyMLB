package com.mlb.mlbportal.mappers.player;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mlb.mlbportal.dto.player.PositionPlayerDTO;
import com.mlb.mlbportal.models.player.PositionPlayer;

@Mapper(componentModel = "spring")
public interface PositionPlayerMapper {
    @Mapping(target = "teamName", source = "team.name")
    PositionPlayerDTO toPositionPlayerDTO(PositionPlayer player);

    List<PositionPlayerDTO> toListPositionPlayerDTO(List<PositionPlayer> playerList);
}