package com.mlb.mlbportal.mappers.player;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mlb.mlbportal.dto.player.PitcherDTO;
import com.mlb.mlbportal.models.player.Pitcher;

@Mapper(componentModel = "spring")
public interface PitcherMapper {
    @Mapping(target = "teamName", source = "team.name")
    PitcherDTO toPitcherDTO(Pitcher pitcher);

    List<PitcherDTO> toListPitcherDTO(List<Pitcher> pitcherList);
}