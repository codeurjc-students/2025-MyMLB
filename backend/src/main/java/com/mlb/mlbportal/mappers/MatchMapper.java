package com.mlb.mlbportal.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mlb.mlbportal.dto.match.MatchDTO;
import com.mlb.mlbportal.models.Match;

@Mapper(componentModel = "spring", uses = {TeamMapper.class, StadiumMapper.class})
public interface MatchMapper {
    @Mapping(target = "homeTeam", source = "homeTeam")
    @Mapping(target = "awayTeam", source = "awayTeam")
    @Mapping(target = "stadiumName", source = "stadium.name")
    MatchDTO toMatchDTO(Match match);
}