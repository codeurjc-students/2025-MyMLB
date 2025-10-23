package com.mlb.mlbportal.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mlb.mlbportal.dto.match.MatchDTO;
import com.mlb.mlbportal.models.Match;

@Mapper(componentModel = "spring", uses = TeamMapper.class)
public interface MatchMapper {
    @Mapping(target = "homeTeam", source = "homeTeam")
    @Mapping(target = "awayTeam", source = "awayTeam")
    MatchDTO toMatchDTO(Match match);

    List<MatchDTO> toMatchDTOList(List<Match> matchList);
}