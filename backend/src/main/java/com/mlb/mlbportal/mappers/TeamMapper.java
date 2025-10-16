package com.mlb.mlbportal.mappers;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mlb.mlbportal.dto.team.TeamDTO;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;

@Mapper(componentModel = "spring")
public interface TeamMapper {
    TeamDTO toTeamDTO(Team team);

    List<TeamDTO> toTeamDTOList(Collection<Team> teams);

    default Map<League, Map<Division, List<TeamDTO>>> toGroupedStandings(
            Map<League, Map<Division, List<Team>>> groupedTeams) {
        return groupedTeams.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        leagueEntry -> leagueEntry.getValue().entrySet().stream()
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        divisionEntry -> toTeamDTOList(divisionEntry.getValue())))));
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "gamesBehind", ignore = true)
    @Mapping(target = "totalGames", ignore = true)
    Team toDomainFromTeamStandings(TeamDTO teamStandings);
}