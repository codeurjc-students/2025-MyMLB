package com.mlb.mlbportal.utils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.mlb.mlbportal.dto.match.MatchDTO;
import com.mlb.mlbportal.dto.player.PitcherDTO;
import com.mlb.mlbportal.dto.player.PitcherSummaryDTO;
import com.mlb.mlbportal.dto.player.PositionPlayerDTO;
import com.mlb.mlbportal.dto.player.PositionPlayerSummaryDTO;
import com.mlb.mlbportal.dto.stadium.StadiumDTO;
import com.mlb.mlbportal.dto.stadium.StadiumInitDTO;
import com.mlb.mlbportal.dto.team.TeamDTO;
import com.mlb.mlbportal.dto.team.TeamInfoDTO;
import com.mlb.mlbportal.dto.team.TeamSummary;
import com.mlb.mlbportal.models.Match;
import com.mlb.mlbportal.models.Stadium;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.UserEntity;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;
import com.mlb.mlbportal.models.enums.MatchStatus;
import com.mlb.mlbportal.models.enums.PitcherPositions;
import com.mlb.mlbportal.models.enums.PlayerPositions;
import com.mlb.mlbportal.models.player.Pitcher;
import com.mlb.mlbportal.models.player.PositionPlayer;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER1_AT_BATS;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER1_DOUBLES;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER1_HITS;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER1_HOME_RUNS;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER1_NAME;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER1_RBIS;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER1_TRIPLES;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER1_WALKS;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER2_AT_BATS;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER2_DOUBLES;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER2_HITS;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER2_HOME_RUNS;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER2_NAME;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER2_RBIS;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER2_TRIPLES;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER2_WALKS;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER3_GAMES;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER3_HITS_ALLOWED;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER3_INNINGS;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER3_LOSSES;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER3_NAME;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER3_RUNS_ALLOWED;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER3_SAVES;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER3_SAVES_OPORTUNITIES;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER3_SO;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER3_WALKS;
import static com.mlb.mlbportal.utils.TestConstants.PLAYER3_WINS;
import static com.mlb.mlbportal.utils.TestConstants.STADIUM1_NAME;
import static com.mlb.mlbportal.utils.TestConstants.STADIUM1_YEAR;
import static com.mlb.mlbportal.utils.TestConstants.STADIUM2_NAME;
import static com.mlb.mlbportal.utils.TestConstants.STADIUM2_YEAR;
import static com.mlb.mlbportal.utils.TestConstants.STADIUM3_NAME;
import static com.mlb.mlbportal.utils.TestConstants.STADIUM3_YEAR;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_ABBREVIATION;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_CITY;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_INFO;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_LOGO;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_LOSSES;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_WINS;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_ABBREVIATION;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_CITY;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_INFO;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_LOGO;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_LOSSES;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_WINS;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM3_ABBREVIATION;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM3_CITY;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM3_INFO;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM3_LOGO;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM3_LOSSES;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM3_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM3_WINS;
import static com.mlb.mlbportal.utils.TestConstants.TEST_USER_EMAIL;
import static com.mlb.mlbportal.utils.TestConstants.TEST_USER_PASSWORD;
import static com.mlb.mlbportal.utils.TestConstants.TEST_USER_USERNAME;
import static com.mlb.mlbportal.utils.TestConstants.USER1_EMAIL;
import static com.mlb.mlbportal.utils.TestConstants.USER1_USERNAME;
import static com.mlb.mlbportal.utils.TestConstants.USER2_EMAIL;
import static com.mlb.mlbportal.utils.TestConstants.USER2_USERNAME;

public class BuildMocksFactory {

    private BuildMocksFactory() {}

    public static List<UserEntity> setUpUsers() {
        UserEntity user1 = new UserEntity(USER1_EMAIL, USER1_USERNAME);
        UserEntity user2 =  new UserEntity(USER2_EMAIL, USER2_USERNAME);
        UserEntity user3 =  new UserEntity(TEST_USER_EMAIL, TEST_USER_USERNAME, TEST_USER_PASSWORD);

        return Arrays.asList(user1, user2, user3);
    }

    public static List<Team> setUpTeamMocks() {
        Team team1 = buildTeam(TEST_TEAM1_NAME, TEST_TEAM1_ABBREVIATION, TEST_TEAM1_WINS, TEST_TEAM1_LOSSES, League.AL, Division.EAST, TEST_TEAM1_LOGO);
        Team team2 = buildTeam(TEST_TEAM2_NAME, TEST_TEAM2_ABBREVIATION, TEST_TEAM2_WINS, TEST_TEAM2_LOSSES, League.NL, Division.CENTRAL, TEST_TEAM2_LOGO);
        Team team3 = buildTeam(TEST_TEAM3_NAME, TEST_TEAM3_ABBREVIATION, TEST_TEAM3_WINS, TEST_TEAM3_LOSSES, League.AL, Division.WEST, TEST_TEAM3_LOGO);

        return Arrays.asList(team1, team2, team3);
    }

    private static Team buildTeam(String name, String abbr, int wins, int losses, League league, Division division, String logo) {
        Team team = new Team(name, abbr, wins, losses, league, division, logo);
        int totalGames = wins + losses;
        team.setTotalGames(totalGames);
        team.setPct((double) wins / totalGames);
        return team;
    }

    public static List<TeamDTO> buildTeamDTOMocks(List<Team> teams) {
        Team team1 = teams.get(0);
        Team team2 = teams.get(1);
        Team team3 = teams.get(2);

        TeamDTO dto1 = new TeamDTO(TEST_TEAM1_NAME, TEST_TEAM1_ABBREVIATION, team1.getLeague(), team1.getDivision(), team1.getTotalGames(), team1.getWins(), team1.getLosses(), team1.getPct(), 1.0, "0-0");
        TeamDTO dto2 = new TeamDTO(TEST_TEAM2_NAME, TEST_TEAM2_ABBREVIATION, team2.getLeague(), team2.getDivision(), team2.getTotalGames(), team2.getWins(), team2.getLosses(), team2.getPct(), 0.0, "0-0");
        TeamDTO dto3 = new TeamDTO(TEST_TEAM3_NAME, TEST_TEAM3_ABBREVIATION, team3.getLeague(), team3.getDivision(), team3.getTotalGames(), team3.getWins(), team3.getLosses(), team3.getPct(), 41.0, "0-0");
        return Arrays.asList(dto1, dto2, dto3);
    }

    public static List<TeamSummary> buildTeamSummaryMocks(List<Team> teams) {
        Team team1 = teams.get(0);
        Team team2 = teams.get(1);
        Team team3 = teams.get(2);

        TeamSummary dto1 = new TeamSummary(team1.getName(), team1.getAbbreviation(), team1.getLeague(), team1.getDivision());
        TeamSummary dto2 = new TeamSummary(team2.getName(), team2.getAbbreviation(), team2.getLeague(), team2.getDivision());
        TeamSummary dto3 = new TeamSummary(team3.getName(), team3.getAbbreviation(), team3.getLeague(), team3.getDivision());
        return Arrays.asList(dto1, dto2, dto3);
    }

    public static List<TeamInfoDTO> buildTeamInfoDTOMocks(List<Team> teams) {
        List<TeamDTO> mockList = buildTeamDTOMocks(teams);

        StadiumDTO stDto1 = new StadiumDTO(STADIUM1_NAME, STADIUM1_YEAR, Arrays.asList(""));
        StadiumDTO stDto2 = new StadiumDTO(STADIUM2_NAME, STADIUM2_YEAR, Arrays.asList(""));
        StadiumDTO stDto3 = new StadiumDTO(STADIUM3_NAME, STADIUM3_YEAR, Arrays.asList(""));

        TeamInfoDTO dto1 = new TeamInfoDTO(mockList.get(0), TEST_TEAM1_CITY, TEST_TEAM1_INFO, List.of(2021, 1999), stDto1, null, null);
        TeamInfoDTO dto2= new TeamInfoDTO(mockList.get(1), TEST_TEAM2_CITY, TEST_TEAM2_INFO, List.of(2010), stDto2,null, null);
        TeamInfoDTO dto3 = new TeamInfoDTO(mockList.get(2), TEST_TEAM3_CITY, TEST_TEAM3_INFO, Collections.emptyList(), stDto3,null, null);
        
        return Arrays.asList(dto1, dto2, dto3);
    }

    // Matches Mocks

    public static List<Match> setUpMatches(List<Team> teams, LocalDateTime fixedNow) {
        Match match1 = new Match(teams.get(0), teams.get(1), 0, 0, fixedNow.minusMinutes(5), MatchStatus.SCHEDULED);
        Match match2 = new Match(teams.get(1), teams.get(2), 4, 9, fixedNow.minusMinutes(4), MatchStatus.IN_PROGRESS);
        Match match3 = new Match(teams.get(2), teams.get(0), 10, 14, fixedNow.minusMinutes(3), MatchStatus.FINISHED);
        return Arrays.asList(match1, match2, match3);
    }

    public static List<MatchDTO> buildMatchDTOMocks(List<Match> matches) {
        return matches.stream().map(m -> {
            TeamSummary home = new TeamSummary(m.getHomeTeam().getName(), m.getHomeTeam().getAbbreviation(), m.getHomeTeam().getLeague(), m.getHomeTeam().getDivision());
            TeamSummary away = new TeamSummary(m.getAwayTeam().getName(), m.getAwayTeam().getAbbreviation(), m.getAwayTeam().getLeague(), m.getAwayTeam().getDivision());
            return new MatchDTO(home, away, m.getHomeScore(), m.getAwayScore(), m.getDate(), m.getStatus());
        }).toList();
    }

    // Stadium Mocks

    public static List<Stadium> setUpStadiums() {
        List<Team> teamList = setUpTeamMocks();
        Stadium stadium1 = new Stadium(STADIUM1_NAME, STADIUM1_YEAR, teamList.get(0));
        Stadium stadium2 = new Stadium(STADIUM2_NAME, STADIUM2_YEAR, teamList.get(1));
        Stadium stadium3 = new Stadium(STADIUM3_NAME, STADIUM3_YEAR, teamList.get(2));

        teamList.get(0).setStadium(stadium1);
        stadium1.setTeam(teamList.get(0));
        teamList.get(1).setStadium(stadium2);
        stadium2.setTeam(teamList.get(1));
        teamList.get(2).setStadium(stadium3);
        stadium3.setTeam(teamList.get(2));

        return Arrays.asList(stadium1, stadium2, stadium3);
    }

    public static List<StadiumInitDTO> buildStadiumInitDTOMocks() {
        List<Team> teams = setUpTeamMocks();
        StadiumInitDTO dto1 = new StadiumInitDTO(STADIUM1_NAME, STADIUM1_YEAR, teams.get(0).getName(), Collections.emptyList());
        StadiumInitDTO dto2 = new StadiumInitDTO(STADIUM2_NAME, STADIUM2_YEAR, teams.get(1).getName(), Collections.emptyList());
        StadiumInitDTO dto3 = new StadiumInitDTO(STADIUM3_NAME, STADIUM3_YEAR, teams.get(2).getName(), Collections.emptyList());

        return Arrays.asList(dto1, dto2, dto3);
    }

    public static List<Stadium> setUpStadiums(List<Team> teamList) {
        Stadium stadium1 = new Stadium(STADIUM1_NAME, STADIUM1_YEAR, teamList.get(0));
        Stadium stadium2 = new Stadium(STADIUM2_NAME, STADIUM2_YEAR, teamList.get(1));
        Stadium stadium3 = new Stadium(STADIUM3_NAME, STADIUM3_YEAR, teamList.get(2));

        return Arrays.asList(stadium1, stadium2, stadium3);
    }

    public static List<StadiumInitDTO> buildStadiumInitDTOMocks(List<Team> teams) {
        StadiumInitDTO dto1 = new StadiumInitDTO(STADIUM1_NAME, STADIUM1_YEAR, teams.get(0).getName(), Collections.emptyList());
        StadiumInitDTO dto2 = new StadiumInitDTO(STADIUM2_NAME, STADIUM2_YEAR, teams.get(1).getName(), Collections.emptyList());
        StadiumInitDTO dto3 = new StadiumInitDTO(STADIUM3_NAME, STADIUM3_YEAR, teams.get(2).getName(), Collections.emptyList());

        return Arrays.asList(dto1, dto2, dto3);
    }

    // Players
    public static List<PositionPlayer> buildPositionPlayers(List<Team> teamList) {
        PositionPlayer p1 = new PositionPlayer(PLAYER1_NAME, teamList.get(0), PlayerPositions.CF, PLAYER1_AT_BATS, PLAYER1_WALKS, PLAYER1_HITS, PLAYER1_DOUBLES);
        p1.setTriples(PLAYER1_TRIPLES);
        p1.setHomeRuns(PLAYER1_HOME_RUNS);
        p1.setRbis(PLAYER1_RBIS);

        PositionPlayer p2 = new PositionPlayer(PLAYER2_NAME, teamList.get(0), PlayerPositions.SS, PLAYER2_AT_BATS, PLAYER2_WALKS, PLAYER2_HITS, PLAYER2_DOUBLES);
        p2.setTriples(PLAYER2_TRIPLES);
        p2.setHomeRuns(PLAYER2_HOME_RUNS);
        p2.setRbis(PLAYER2_RBIS);

        Team team1 = teamList.get(0);
        team1.setPositionPlayers(Arrays.asList(p1, p2));
        return Arrays.asList(p1, p2);
    }

    public static List<PositionPlayerDTO> buildPositionPlayerDTOs() {
        List<Team> teamList = setUpTeamMocks();
        PositionPlayerDTO dto1 = new PositionPlayerDTO(PLAYER1_NAME, teamList.get(0).getName(), PlayerPositions.CF, PLAYER1_AT_BATS, PLAYER1_WALKS, PLAYER1_HITS, PLAYER1_DOUBLES, PLAYER1_TRIPLES, PLAYER1_HOME_RUNS, PLAYER1_RBIS, 0.0, 0.0, 0.0, 0.0);
        PositionPlayerDTO dto2 = new PositionPlayerDTO(PLAYER2_NAME, teamList.get(0).getName(), PlayerPositions.SS, PLAYER2_AT_BATS, PLAYER2_WALKS, PLAYER2_HITS, PLAYER2_DOUBLES, PLAYER2_TRIPLES, PLAYER2_HOME_RUNS, PLAYER2_RBIS, 0.0, 0.0, 0.0, 0.0);
        return Arrays.asList(dto1, dto2);
    }

    public static List<PositionPlayerSummaryDTO> buildPositionPlayerSummaryDTOs() {
        PositionPlayerSummaryDTO dto1 = new PositionPlayerSummaryDTO(PLAYER1_NAME, PlayerPositions.CF, PLAYER1_AT_BATS, PLAYER1_WALKS, PLAYER1_HITS, PLAYER1_DOUBLES, PLAYER1_TRIPLES, PLAYER1_HOME_RUNS, PLAYER1_RBIS, 0.0, 0.0, 0.0, 0.0);
        PositionPlayerSummaryDTO dto2 = new PositionPlayerSummaryDTO(PLAYER2_NAME, PlayerPositions.SS, PLAYER2_AT_BATS, PLAYER2_WALKS, PLAYER2_HITS, PLAYER2_DOUBLES, PLAYER2_TRIPLES, PLAYER2_HOME_RUNS, PLAYER2_RBIS, 0.0, 0.0, 0.0, 0.0);
        return Arrays.asList(dto1, dto2);
    }

    // Pitcher mocks
    public static List<Pitcher> buildPitchers(List<Team> teamList) {
        Pitcher p3 = new Pitcher(PLAYER3_NAME, teamList.get(1), PitcherPositions.SP, PLAYER3_GAMES, PLAYER3_WINS, PLAYER3_LOSSES, PLAYER3_INNINGS);
        p3.setTotalStrikeouts(PLAYER3_SO);
        p3.setWalks(PLAYER3_WALKS);
        p3.setHitsAllowed(PLAYER3_HITS_ALLOWED);
        p3.setRunsAllowed(PLAYER3_RUNS_ALLOWED);
        p3.setSaves(PLAYER3_SAVES);
        p3.setSaveOpportunities(PLAYER3_SAVES_OPORTUNITIES);
        Team team1 = teamList.get(1);
        team1.setPitchers(Arrays.asList(p3));
        return Arrays.asList(p3);
    }

    public static List<PitcherDTO> buildPitcherDTOs() {
        List<Team> teamList = setUpTeamMocks();
        PitcherDTO dto3 = new PitcherDTO(PLAYER3_NAME, teamList.get(1).getName(), PitcherPositions.SP, PLAYER3_GAMES, PLAYER3_WINS, PLAYER3_LOSSES, 0.0, PLAYER3_INNINGS, PLAYER3_SO, PLAYER3_WALKS, PLAYER3_HITS_ALLOWED, PLAYER3_RUNS_ALLOWED, 0.0, PLAYER3_SAVES, PLAYER3_SAVES_OPORTUNITIES);
        return Arrays.asList(dto3);
    }

    public static List<PitcherSummaryDTO> buildPitcherSummaryDTOs() {
        PitcherSummaryDTO dto3 = new PitcherSummaryDTO(PLAYER3_NAME, PitcherPositions.SP, PLAYER3_GAMES, PLAYER3_WINS, PLAYER3_LOSSES, 0.0, PLAYER3_INNINGS, PLAYER3_SO, PLAYER3_WALKS, PLAYER3_HITS_ALLOWED, PLAYER3_RUNS_ALLOWED, 0.0, PLAYER3_SAVES, PLAYER3_SAVES_OPORTUNITIES);
        return Arrays.asList(dto3);
    }   
}