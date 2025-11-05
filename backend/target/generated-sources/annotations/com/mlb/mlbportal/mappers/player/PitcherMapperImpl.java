package com.mlb.mlbportal.mappers.player;

import com.mlb.mlbportal.dto.player.PitcherDTO;
import com.mlb.mlbportal.dto.player.PitcherSummaryDTO;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.PitcherPositions;
import com.mlb.mlbportal.models.player.Pitcher;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-05T20:11:42+0100",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.44.0.v20251023-0518, environment: Java 21.0.8 (Eclipse Adoptium)"
)
@Component
public class PitcherMapperImpl implements PitcherMapper {

    @Override
    public PitcherDTO toPitcherDTO(Pitcher pitcher) {
        if ( pitcher == null ) {
            return null;
        }

        String teamName = null;
        String name = null;
        PitcherPositions position = null;
        int games = 0;
        int wins = 0;
        int losses = 0;
        double era = 0.0d;
        int inningsPitched = 0;
        int totalStrikeouts = 0;
        int walks = 0;
        int hitsAllowed = 0;
        int runsAllowed = 0;
        double whip = 0.0d;
        int saves = 0;
        int saveOpportunities = 0;

        teamName = pitcherTeamName( pitcher );
        name = pitcher.getName();
        position = pitcher.getPosition();
        games = pitcher.getGames();
        wins = pitcher.getWins();
        losses = pitcher.getLosses();
        era = pitcher.getEra();
        inningsPitched = (int) pitcher.getInningsPitched();
        totalStrikeouts = pitcher.getTotalStrikeouts();
        walks = pitcher.getWalks();
        hitsAllowed = pitcher.getHitsAllowed();
        runsAllowed = pitcher.getRunsAllowed();
        whip = pitcher.getWhip();
        saves = pitcher.getSaves();
        saveOpportunities = pitcher.getSaveOpportunities();

        PitcherDTO pitcherDTO = new PitcherDTO( name, teamName, position, games, wins, losses, era, inningsPitched, totalStrikeouts, walks, hitsAllowed, runsAllowed, whip, saves, saveOpportunities );

        return pitcherDTO;
    }

    @Override
    public List<PitcherDTO> toListPitcherDTO(List<Pitcher> pitcherList) {
        if ( pitcherList == null ) {
            return null;
        }

        List<PitcherDTO> list = new ArrayList<PitcherDTO>( pitcherList.size() );
        for ( Pitcher pitcher : pitcherList ) {
            list.add( toPitcherDTO( pitcher ) );
        }

        return list;
    }

    @Override
    public PitcherSummaryDTO toPitcherSummaryDTO(Pitcher pitcher) {
        if ( pitcher == null ) {
            return null;
        }

        String name = null;
        PitcherPositions position = null;
        int games = 0;
        int wins = 0;
        int losses = 0;
        double era = 0.0d;
        int inningsPitched = 0;
        int totalStrikeouts = 0;
        int walks = 0;
        int hitsAllowed = 0;
        int runsAllowed = 0;
        double whip = 0.0d;
        int saves = 0;
        int saveOpportunities = 0;

        name = pitcher.getName();
        position = pitcher.getPosition();
        games = pitcher.getGames();
        wins = pitcher.getWins();
        losses = pitcher.getLosses();
        era = pitcher.getEra();
        inningsPitched = (int) pitcher.getInningsPitched();
        totalStrikeouts = pitcher.getTotalStrikeouts();
        walks = pitcher.getWalks();
        hitsAllowed = pitcher.getHitsAllowed();
        runsAllowed = pitcher.getRunsAllowed();
        whip = pitcher.getWhip();
        saves = pitcher.getSaves();
        saveOpportunities = pitcher.getSaveOpportunities();

        PitcherSummaryDTO pitcherSummaryDTO = new PitcherSummaryDTO( name, position, games, wins, losses, era, inningsPitched, totalStrikeouts, walks, hitsAllowed, runsAllowed, whip, saves, saveOpportunities );

        return pitcherSummaryDTO;
    }

    @Override
    public List<PitcherSummaryDTO> toListPitcherSummaryDTO(List<Pitcher> pitcherList) {
        if ( pitcherList == null ) {
            return null;
        }

        List<PitcherSummaryDTO> list = new ArrayList<PitcherSummaryDTO>( pitcherList.size() );
        for ( Pitcher pitcher : pitcherList ) {
            list.add( toPitcherSummaryDTO( pitcher ) );
        }

        return list;
    }

    private String pitcherTeamName(Pitcher pitcher) {
        Team team = pitcher.getTeam();
        if ( team == null ) {
            return null;
        }
        return team.getName();
    }
}
