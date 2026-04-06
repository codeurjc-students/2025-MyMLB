package com.mlb.mlbportal.mappers;

import com.mlb.mlbportal.dto.team.HistoricRankingDTO;
import com.mlb.mlbportal.models.DailyStandings;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = TeamMapper.class)
public interface DailyStandingsMapper {
    @Mapping(target = "teamName", source = "team.name")
    HistoricRankingDTO toHistoricRankingDTO(DailyStandings dailyStandings);
}