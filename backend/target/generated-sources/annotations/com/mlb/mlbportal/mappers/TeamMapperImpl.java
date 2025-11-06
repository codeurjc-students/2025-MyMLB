package com.mlb.mlbportal.mappers;

import com.mlb.mlbportal.dto.player.PitcherSummaryDTO;
import com.mlb.mlbportal.dto.player.PositionPlayerSummaryDTO;
import com.mlb.mlbportal.dto.stadium.StadiumDTO;
import com.mlb.mlbportal.dto.team.TeamDTO;
import com.mlb.mlbportal.dto.team.TeamInfoDTO;
import com.mlb.mlbportal.dto.team.TeamSummary;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;
import com.mlb.mlbportal.models.enums.PitcherPositions;
import com.mlb.mlbportal.models.enums.PlayerPositions;
import com.mlb.mlbportal.models.player.Pitcher;
import com.mlb.mlbportal.models.player.PositionPlayer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-06T09:14:20+0100",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.44.0.v20251023-0518, environment: Java 21.0.8 (Eclipse Adoptium)"
)
@Component
public class TeamMapperImpl implements TeamMapper {

    @Autowired
    private StadiumMapper stadiumMapper;

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

    @Override
    public TeamInfoDTO toTeamInfoDTO(Team team) {
        if ( team == null ) {
            return null;
        }

        TeamDTO teamStats = null;
        List<PositionPlayerSummaryDTO> positionPlayers = null;
        List<PitcherSummaryDTO> pitchers = null;
        String city = null;
        String generalInfo = null;
        List<Integer> championships = null;
        StadiumDTO stadium = null;

        teamStats = toTeamDTO( team );
        positionPlayers = positionPlayerListToPositionPlayerSummaryDTOList( team.getPositionPlayers() );
        pitchers = pitcherListToPitcherSummaryDTOList( team.getPitchers() );
        city = team.getCity();
        generalInfo = team.getGeneralInfo();
        List<Integer> list2 = team.getChampionships();
        if ( list2 != null ) {
            championships = new ArrayList<Integer>( list2 );
        }
        stadium = stadiumMapper.toStadiumDTO( team.getStadium() );

        TeamInfoDTO teamInfoDTO = new TeamInfoDTO( teamStats, city, generalInfo, championships, stadium, positionPlayers, pitchers );

        return teamInfoDTO;
    }

    @Override
    public List<TeamInfoDTO> toTeamInfoDTOList(List<Team> teamList) {
        if ( teamList == null ) {
            return null;
        }

        List<TeamInfoDTO> list = new ArrayList<TeamInfoDTO>( teamList.size() );
        for ( Team team : teamList ) {
            list.add( toTeamInfoDTO( team ) );
        }

        return list;
    }

    protected PositionPlayerSummaryDTO positionPlayerToPositionPlayerSummaryDTO(PositionPlayer positionPlayer) {
        if ( positionPlayer == null ) {
            return null;
        }

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

        name = positionPlayer.getName();
        position = positionPlayer.getPosition();
        atBats = positionPlayer.getAtBats();
        walks = positionPlayer.getWalks();
        hits = positionPlayer.getHits();
        doubles = positionPlayer.getDoubles();
        triples = positionPlayer.getTriples();
        homeRuns = positionPlayer.getHomeRuns();
        rbis = positionPlayer.getRbis();
        average = positionPlayer.getAverage();
        obp = positionPlayer.getObp();
        ops = positionPlayer.getOps();
        slugging = positionPlayer.getSlugging();

        PositionPlayerSummaryDTO positionPlayerSummaryDTO = new PositionPlayerSummaryDTO( name, position, atBats, walks, hits, doubles, triples, homeRuns, rbis, average, obp, ops, slugging );

        return positionPlayerSummaryDTO;
    }

    protected List<PositionPlayerSummaryDTO> positionPlayerListToPositionPlayerSummaryDTOList(List<PositionPlayer> list) {
        if ( list == null ) {
            return null;
        }

        List<PositionPlayerSummaryDTO> list1 = new ArrayList<PositionPlayerSummaryDTO>( list.size() );
        for ( PositionPlayer positionPlayer : list ) {
            list1.add( positionPlayerToPositionPlayerSummaryDTO( positionPlayer ) );
        }

        return list1;
    }

    protected PitcherSummaryDTO pitcherToPitcherSummaryDTO(Pitcher pitcher) {
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

    protected List<PitcherSummaryDTO> pitcherListToPitcherSummaryDTOList(List<Pitcher> list) {
        if ( list == null ) {
            return null;
        }

        List<PitcherSummaryDTO> list1 = new ArrayList<PitcherSummaryDTO>( list.size() );
        for ( Pitcher pitcher : list ) {
            list1.add( pitcherToPitcherSummaryDTO( pitcher ) );
        }

        return list1;
    }
}
