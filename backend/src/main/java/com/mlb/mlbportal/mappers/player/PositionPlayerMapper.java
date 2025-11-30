package com.mlb.mlbportal.mappers.player;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mlb.mlbportal.dto.player.position.PositionPlayerDTO;
import com.mlb.mlbportal.dto.player.position.PositionPlayerSummaryDTO;
import com.mlb.mlbportal.models.player.PositionPlayer;

@Mapper(componentModel = "spring")
public interface PositionPlayerMapper {
    @Mapping(target = "teamName", source = "team.name")
    PositionPlayerDTO toPositionPlayerDTO(PositionPlayer player);

    List<PositionPlayerDTO> toListPositionPlayerDTO(List<PositionPlayer> playerList);

    @Mapping(target = "average", source = "average")
    @Mapping(target = "obp", source = "obp")
    @Mapping(target = "slugging", source = "slugging")
    @Mapping(target = "ops", source = "ops")
    PositionPlayerSummaryDTO toPositionPlayerSummaryDTO(PositionPlayer player);

    List<PositionPlayerSummaryDTO> toListPositionPlayerSummaryDTO(List<PositionPlayer> playerList);
}