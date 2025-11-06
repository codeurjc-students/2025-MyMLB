package com.mlb.mlbportal.mappers.player;

import com.mlb.mlbportal.dto.player.PositionPlayerDTO;
import com.mlb.mlbportal.dto.player.PositionPlayerSummaryDTO;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.PlayerPositions;
import com.mlb.mlbportal.models.player.PositionPlayer;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-06T09:14:20+0100",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.44.0.v20251023-0518, environment: Java 21.0.8 (Eclipse Adoptium)"
)
@Component
public class PositionPlayerMapperImpl implements PositionPlayerMapper {

    @Override
    public PositionPlayerDTO toPositionPlayerDTO(PositionPlayer player) {
        if ( player == null ) {
            return null;
        }

        String teamName = null;
        String name = null;
        PlayerPositions position = null;
        int atBats = 0;
        int walks = 0;
        int hits = 0;
        int doubles = 0;
        int triples = 0;
        int homeRuns = 0;
        int rbis = 0;
        double average = 0.0d;
        double obp = 0.0d;
        double ops = 0.0d;
        double slugging = 0.0d;

        teamName = playerTeamName( player );
        name = player.getName();
        position = player.getPosition();
        atBats = player.getAtBats();
        walks = player.getWalks();
        hits = player.getHits();
        doubles = player.getDoubles();
        triples = player.getTriples();
        homeRuns = player.getHomeRuns();
        rbis = player.getRbis();
        average = player.getAverage();
        obp = player.getObp();
        ops = player.getOps();
        slugging = player.getSlugging();

        PositionPlayerDTO positionPlayerDTO = new PositionPlayerDTO( name, teamName, position, atBats, walks, hits, doubles, triples, homeRuns, rbis, average, obp, ops, slugging );

        return positionPlayerDTO;
    }

    @Override
    public List<PositionPlayerDTO> toListPositionPlayerDTO(List<PositionPlayer> playerList) {
        if ( playerList == null ) {
            return null;
        }

        List<PositionPlayerDTO> list = new ArrayList<PositionPlayerDTO>( playerList.size() );
        for ( PositionPlayer positionPlayer : playerList ) {
            list.add( toPositionPlayerDTO( positionPlayer ) );
        }

        return list;
    }

    @Override
    public PositionPlayerSummaryDTO toPositionPlayerSummaryDTO(PositionPlayer player) {
        if ( player == null ) {
            return null;
        }

        double average = 0.0d;
        double obp = 0.0d;
        double slugging = 0.0d;
        double ops = 0.0d;
        String name = null;
        PlayerPositions position = null;
        int atBats = 0;
        int walks = 0;
        int hits = 0;
        int doubles = 0;
        int triples = 0;
        int homeRuns = 0;
        int rbis = 0;

        average = player.getAverage();
        obp = player.getObp();
        slugging = player.getSlugging();
        ops = player.getOps();
        name = player.getName();
        position = player.getPosition();
        atBats = player.getAtBats();
        walks = player.getWalks();
        hits = player.getHits();
        doubles = player.getDoubles();
        triples = player.getTriples();
        homeRuns = player.getHomeRuns();
        rbis = player.getRbis();

        PositionPlayerSummaryDTO positionPlayerSummaryDTO = new PositionPlayerSummaryDTO( name, position, atBats, walks, hits, doubles, triples, homeRuns, rbis, average, obp, ops, slugging );

        return positionPlayerSummaryDTO;
    }

    @Override
    public List<PositionPlayerSummaryDTO> toListPositionPlayerSummaryDTO(List<PositionPlayer> playerList) {
        if ( playerList == null ) {
            return null;
        }

        List<PositionPlayerSummaryDTO> list = new ArrayList<PositionPlayerSummaryDTO>( playerList.size() );
        for ( PositionPlayer positionPlayer : playerList ) {
            list.add( toPositionPlayerSummaryDTO( positionPlayer ) );
        }

        return list;
    }

    private String playerTeamName(PositionPlayer positionPlayer) {
        Team team = positionPlayer.getTeam();
        if ( team == null ) {
            return null;
        }
        return team.getName();
    }
}
