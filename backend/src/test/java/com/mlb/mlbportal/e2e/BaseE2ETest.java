package com.mlb.mlbportal.e2e;

import java.time.LocalDateTime;
import java.util.List;

import com.mlb.mlbportal.models.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mlb.mlbportal.models.Match;
import com.mlb.mlbportal.models.Stadium;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;
import com.mlb.mlbportal.models.enums.MatchStatus;
import com.mlb.mlbportal.models.enums.PitcherPositions;
import com.mlb.mlbportal.models.enums.PlayerPositions;
import com.mlb.mlbportal.models.player.Pitcher;
import com.mlb.mlbportal.models.player.PositionPlayer;
import com.mlb.mlbportal.repositories.MatchRepository;
import com.mlb.mlbportal.repositories.StadiumRepository;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.repositories.UserRepository;
import com.mlb.mlbportal.repositories.player.PitcherRepository;
import com.mlb.mlbportal.repositories.player.PositionPlayerRepository;

import static com.mlb.mlbportal.utils.TestConstants.BASE_URI;

import io.restassured.RestAssured;

public abstract class BaseE2ETest {

    @LocalServerPort
    protected int port;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private StadiumRepository stadiumRepository;

    @Autowired
    private PositionPlayerRepository positionPlayerRepository;

    @Autowired
    private PitcherRepository pitcherRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @BeforeEach
    @SuppressWarnings("unused")
    void setupRestAssured() {
        RestAssured.baseURI = BASE_URI;
        RestAssured.port = port;
    }

    protected void cleanDatabase() {      
        this.positionPlayerRepository.deleteAll(); 
        this.pitcherRepository.deleteAll(); 
        this.matchRepository.deleteAll();
        this.teamRepository.deleteAll();
        this.stadiumRepository.deleteAll();  
        this.userRepository.deleteAll();
    }

    protected UserEntity saveTestUser(String email, String username, String password) {
        UserEntity user = new UserEntity(email, username, passwordEncoder.encode(password));
        user.setRoles(List.of("USER"));
        return this.userRepository.save(user);
    }

    protected Team saveTestTeam(String name, String abbreviation, int wins, int losses, String city, String info, List<Integer> championships, League league,
            Division division) {
        Team team = new Team(name, abbreviation, wins, losses, city, info, championships);
        team.setLeague(league);
        team.setDivision(division);
        return this.teamRepository.save(team);
    }

    protected void saveTestMatches(Team awayTeam, Team homeTeam, int awayScore, int homeScore, LocalDateTime date, MatchStatus status) {
        this.matchRepository.save(new Match(awayTeam, homeTeam, awayScore, homeScore, date, status));
    }

    protected void saveTestStadiums(String name, int openingDate, Team team) {
        Stadium stadium = new Stadium(name, openingDate, team);
        if (team != null) {
            team.setStadium(stadium);
            this.teamRepository.save(team);
        }
        else {
            this.stadiumRepository.save(stadium);
        }
    }

    protected void saveTestPositionPlayers(String name, int number, Team team, int atBats, int walks, int hits, int doubles, int triples, int homeRuns, int rbis) {
        PositionPlayer player = new PositionPlayer(name, number, team, PlayerPositions.CF, atBats, walks, hits, doubles);
        player.setTriples(triples);
        player.setHomeRuns(homeRuns);
        player.setRbis(rbis);
        this.positionPlayerRepository.save(player);
    }

    protected void saveTestPitchers(String name, int number, Team team, int games, int wins, int losses, int innings, int so, int walks, int hitsAllowed, int runsAllowed, int saves, int savesOp) {
        Pitcher pitcher = new Pitcher(name, number, team, PitcherPositions.SP, games, wins, losses, innings);
        pitcher.setTotalStrikeouts(so);
        pitcher.setWalks(walks);
        pitcher.setHitsAllowed(hitsAllowed);
        pitcher.setRunsAllowed(runsAllowed);
        pitcher.setSaves(saves);
        pitcher.setSaveOpportunities(savesOp);
        this.pitcherRepository.save(pitcher);
    }
}