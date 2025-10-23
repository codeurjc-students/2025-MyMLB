package com.mlb.mlbportal.mappers;

import com.mlb.mlbportal.dto.team.TeamDTO;
import com.mlb.mlbportal.dto.team.TeamSummary;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-23T20:25:19+0200",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.44.0.v20251001-1143, environment: Java 21.0.8 (Eclipse Adoptium)"
)
@Component
public class TeamMapperImpl implements TeamMapper {

    @Override
    public TeamDTO toTeamDTO(Team team) {
        if ( team == null ) {
            return null;
        }

        String name = null;
        String abbreviation = null;
        League league = null;
        Division division = null;
        int totalGames = 0;
        int wins = 0;
        int losses = 0;
        double pct = 0.0d;
        double gamesBehind = 0.0d;
        String lastTen = null;

        name = team.getName();
        abbreviation = team.getAbbreviation();
        league = team.getLeague();
        division = team.getDivision();
        totalGames = team.getTotalGames();
        wins = team.getWins();
        losses = team.getLosses();
        pct = team.getPct();
        gamesBehind = team.getGamesBehind();
        lastTen = team.getLastTen();

        TeamDTO teamDTO = new TeamDTO( name, abbreviation, league, division, totalGames, wins, losses, pct, gamesBehind, lastTen );

        return teamDTO;
    }

    @Override
    public List<TeamDTO> toTeamDTOList(Collection<Team> teams) {
        if ( teams == null ) {
            return null;
        }

        List<TeamDTO> list = new ArrayList<TeamDTO>( teams.size() );
        for ( Team team : teams ) {
            list.add( toTeamDTO( team ) );
        }

        return list;
    }

    @Override
    public Team toDomainFromTeamStandings(TeamDTO teamStandings) {
        if ( teamStandings == null ) {
            return null;
        }

        Team team = new Team();

        team.setAbbreviation( teamStandings.abbreviation() );
        team.setDivision( teamStandings.division() );
        team.setLastTen( teamStandings.lastTen() );
        team.setLeague( teamStandings.league() );
        team.setLosses( teamStandings.losses() );
        team.setName( teamStandings.name() );
        team.setPct( teamStandings.pct() );
        team.setWins( teamStandings.wins() );

        return team;
    }

    @Override
    public TeamSummary toTeamSummaryDTO(Team team) {
        if ( team == null ) {
            return null;
        }

        String name = null;
        String abbreviation = null;
        League league = null;
        Division division = null;

        name = team.getName();
        abbreviation = team.getAbbreviation();
        league = team.getLeague();
        division = team.getDivision();

        TeamSummary teamSummary = new TeamSummary( name, abbreviation, league, division );

        return teamSummary;
    }
}
