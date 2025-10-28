package com.mlb.mlbportal.e2e;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;

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
import com.mlb.mlbportal.repositories.MatchRepository;
import com.mlb.mlbportal.repositories.StadiumRepository;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.repositories.UserRepository;
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
    protected PasswordEncoder passwordEncoder;

    @BeforeEach
    @SuppressWarnings("unused")
    void setupRestAssured() {
        RestAssured.baseURI = BASE_URI;
        RestAssured.port = port;
    }

    protected void cleanDatabase() {
        this.matchRepository.deleteAll();
        this.userRepository.deleteAll();
        this.teamRepository.deleteAll();
        this.stadiumRepository.deleteAll();
    }

    protected void saveTestUser(String email, String username, String password) {
        this.userRepository
                .save(new com.mlb.mlbportal.models.UserEntity(email, username, passwordEncoder.encode(password)));
    }

    protected Team saveTestTeam(String name, String abbreviation, int wins, int losses, String city, String info, List<Year> championships, League league,
            Division division) {
        return this.teamRepository.save(new Team(name, abbreviation, wins, losses, city, info, championships, league, division));
    }

    protected void saveTestMatches(Team awayTeam, Team homeTeam, int awayScore, int homeScore, LocalDateTime date, MatchStatus status) {
        this.matchRepository.save(new Match(awayTeam, homeTeam, awayScore, homeScore, date, status));
    }

    protected void saveTestStadiums(String name, int openingDate, Team team) {
        Stadium stadium = new Stadium(name, openingDate, team);
        team.setStadium(stadium);
        this.teamRepository.save(team);
    }
}