package com.mlb.mlbportal.utils;

import com.mlb.mlbportal.dto.match.MatchDTO;
import com.mlb.mlbportal.dto.stadium.StadiumDTO;
import com.mlb.mlbportal.dto.team.TeamDTO;
import com.mlb.mlbportal.dto.team.TeamInfoDTO;
import com.mlb.mlbportal.dto.team.TeamSummary;
import com.mlb.mlbportal.models.Match;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;
import com.mlb.mlbportal.models.enums.MatchStatus;

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

import java.time.LocalDateTime;
import java.time.Year;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BuildMocksFactory {

    private BuildMocksFactory() {}

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

    public static List<TeamInfoDTO> buildTeamInfoDTOMocks(List<Team> teams) {
        List<TeamDTO> mockList = buildTeamDTOMocks(teams);

        StadiumDTO stDto1 = new StadiumDTO(STADIUM1_NAME, STADIUM1_YEAR);
        StadiumDTO stDto2 = new StadiumDTO(STADIUM2_NAME, STADIUM2_YEAR);
        StadiumDTO stDto3 = new StadiumDTO(STADIUM3_NAME, STADIUM3_YEAR);

        TeamInfoDTO dto1 = new TeamInfoDTO(mockList.get(0), TEST_TEAM1_CITY, TEST_TEAM1_INFO, List.of(Year.of(2021), Year.of(1999)), stDto1);
        TeamInfoDTO dto2= new TeamInfoDTO(mockList.get(1), TEST_TEAM2_CITY, TEST_TEAM2_INFO, List.of(Year.of(2010)), stDto2);
        TeamInfoDTO dto3 = new TeamInfoDTO(mockList.get(2), TEST_TEAM3_CITY, TEST_TEAM3_INFO, Collections.emptyList(), stDto3);
        
        return Arrays.asList(dto1, dto2, dto3);
    }

    // Matches Mocks

    public static List<Match> setUpMatches(List<Team> teams, LocalDateTime fixedNow) {
        Match match1 = new Match(teams.get(0), teams.get(1), 0, 0, fixedNow.minusMinutes(5), MatchStatus.SCHEDULED);
        Match match2 = new Match(teams.get(1), teams.get(2), 4, 9, fixedNow.minusMinutes(4), MatchStatus.IN_PROGRESS);
        Match match3 = new Match(teams.get(2), teams.get(0), 10, 14, fixedNow.minusMinutes(3), MatchStatus.FINISHED);
        return Arrays.asList(match1, match2, match3);
    }

    public static List<MatchDTO> buildMatchDTOMocks(List<Team> teams, List<Match> matches) {
        return matches.stream().map(m -> {
            TeamSummary home = new TeamSummary(m.getHomeTeam().getName(), m.getHomeTeam().getAbbreviation(), m.getHomeTeam().getLeague(), m.getHomeTeam().getDivision());
            TeamSummary away = new TeamSummary(m.getAwayTeam().getName(), m.getAwayTeam().getAbbreviation(), m.getAwayTeam().getLeague(), m.getAwayTeam().getDivision());
            return new MatchDTO(home, away, m.getHomeScore(), m.getAwayScore(), m.getDate(), m.getStatus());
        }).collect(Collectors.toList());
    }
}