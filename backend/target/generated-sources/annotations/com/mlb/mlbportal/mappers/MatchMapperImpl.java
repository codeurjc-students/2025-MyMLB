package com.mlb.mlbportal.mappers;

import com.mlb.mlbportal.dto.match.MatchDTO;
import com.mlb.mlbportal.dto.team.TeamSummary;
import com.mlb.mlbportal.models.Match;
import com.mlb.mlbportal.models.enums.MatchStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-23T20:25:19+0200",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.44.0.v20251001-1143, environment: Java 21.0.8 (Eclipse Adoptium)"
)
@Component
public class MatchMapperImpl implements MatchMapper {

    @Autowired
    private TeamMapper teamMapper;

    @Override
    public MatchDTO toMatchDTO(Match match) {
        if ( match == null ) {
            return null;
        }

        TeamSummary homeTeam = null;
        TeamSummary awayTeam = null;
        int homeScore = 0;
        int awayScore = 0;
        LocalDateTime date = null;
        MatchStatus status = null;

        homeTeam = teamMapper.toTeamSummaryDTO( match.getHomeTeam() );
        awayTeam = teamMapper.toTeamSummaryDTO( match.getAwayTeam() );
        homeScore = match.getHomeScore();
        awayScore = match.getAwayScore();
        date = match.getDate();
        status = match.getStatus();

        MatchDTO matchDTO = new MatchDTO( homeTeam, awayTeam, homeScore, awayScore, date, status );

        return matchDTO;
    }

    @Override
    public List<MatchDTO> toMatchDTOList(List<Match> matchList) {
        if ( matchList == null ) {
            return null;
        }

        List<MatchDTO> list = new ArrayList<MatchDTO>( matchList.size() );
        for ( Match match : matchList ) {
            list.add( toMatchDTO( match ) );
        }

        return list;
    }
}
