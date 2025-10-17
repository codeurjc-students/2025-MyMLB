package com.mlb.mlbportal.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mlb.mlbportal.dto.team.TeamInitDTO;
import com.mlb.mlbportal.handler.notFound.TeamNotFoundException;
import com.mlb.mlbportal.models.Match;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.UserEntity;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;
import com.mlb.mlbportal.repositories.MatchRepository;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.repositories.UserRepository;

import jakarta.annotation.PostConstruct;

@Controller
public class InitController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;

    @Value("${init.user1.password}")
    private String fonssiPassword;

    @Value("${init.user2.password}")
    private String arminPassword;

    public InitController(UserRepository userRepository, PasswordEncoder passsEncoder, TeamRepository teamRepo, MatchRepository matchRepo) {
        this.userRepository = userRepository;
        this.passwordEncoder = passsEncoder;

        this.teamRepository = teamRepo;
        this.matchRepository = matchRepo;
    }

    @PostConstruct
    public void init() {
        this.createAdmins();
        this.setUpTeams();
        this.setUpMatches();
    }

    private void createAdmins() {
        UserEntity fonssiUser = new UserEntity("fonssitorodriguezgutt@gmail.com", "fonssi29",
                this.passwordEncoder.encode(this.fonssiPassword));
        UserEntity arminUser = new UserEntity("armin@gmail.com", "armiin13",
                this.passwordEncoder.encode(this.arminPassword));

        fonssiUser.getRoles().add("ADMIN");
        arminUser.getRoles().add("ADMIN");

        this.userRepository.save(fonssiUser);
        this.userRepository.save(arminUser);
    }

    private void setUpTeams() {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream input = getClass().getResourceAsStream("/data/teams-2025.json")) {
            TeamInitDTO[] teamDtos = mapper.readValue(input, TeamInitDTO[].class);

            List<Team> teams = Arrays.stream(teamDtos)
                    .map(dto -> {
                        League league = League.valueOf(dto.league().toUpperCase());
                        Division division = Division.valueOf(dto.division().toUpperCase());
                        String logo = dto.abbreviation() + ".png";

                        Team team = new Team(dto.name(), dto.abbreviation(), dto.wins(), dto.losses(), league, division,
                                logo);
                        int totalGames = dto.wins() + dto.losses();
                        team.setTotalGames(totalGames);
                        team.setPct((double) dto.wins() / totalGames);
                        team.setGamesBehind(0);
                        team.setLastTen("0-0");

                        return team;
                    })
                    .collect(Collectors.toList());

            this.teamRepository.saveAll(teams);
        } catch (IOException e) {
            throw new RuntimeException("Error loading team data from JSON", e);
        }
    }

    private void setUpMatches() {
        Team yankees = this.teamRepository.findByName("New York Yankees").orElseThrow(TeamNotFoundException::new);
        Team toronto = this.teamRepository.findByName("Toronto Blue Jays").orElseThrow(TeamNotFoundException::new);
        Team dodgers = this.teamRepository.findByName("Los Angeles Dodgers").orElseThrow(TeamNotFoundException::new);
        Team detroit = this.teamRepository.findByName("Detroit Tigers").orElseThrow(TeamNotFoundException::new);

         List<Match> matches = List.of(
            new Match(yankees, toronto, 5, 3),
            new Match(yankees, toronto, 2, 6),
            new Match(yankees, toronto, 4, 1),

            new Match(dodgers, yankees, 5, 2),
            new Match(dodgers, yankees, 0, 2),
            new Match(dodgers, yankees, 3, 4),

            new Match(toronto, detroit, 0, 7),
            new Match(toronto, detroit, 1, 2),
            new Match(toronto, detroit, 5, 8),

            new Match(detroit, yankees, 4, 6),
            new Match(detroit, yankees, 4, 0),
            new Match(detroit, yankees, 7, 4),

            new Match(toronto, yankees, 7, 4),
            new Match(toronto, yankees, 0, 4),
            new Match(toronto, yankees, 2, 6),

            new Match(dodgers, detroit, 3, 4),
            new Match(dodgers, detroit, 6, 9),
            new Match(dodgers, detroit, 0, 7),

            new Match(toronto, dodgers, 0, 7),
            new Match(toronto, dodgers, 5, 2),
            new Match(toronto, dodgers, 4, 2)
        );
        this.matchRepository.saveAll(matches.reversed());

        yankees.getHomeMatches().addAll(matches.stream().filter(m -> m.getHomeTeam().equals(yankees)).toList());
        yankees.getAwayMatches().addAll(matches.stream().filter(m -> m.getAwayTeam().equals(yankees)).toList());

        toronto.getHomeMatches().addAll(matches.stream().filter(m -> m.getHomeTeam().equals(toronto)).toList());
        toronto.getAwayMatches().addAll(matches.stream().filter(m -> m.getAwayTeam().equals(toronto)).toList());

        detroit.getHomeMatches().addAll(matches.stream().filter(m -> m.getHomeTeam().equals(detroit)).toList());
        detroit.getAwayMatches().addAll(matches.stream().filter(m -> m.getAwayTeam().equals(detroit)).toList());

        dodgers.getHomeMatches().addAll(matches.stream().filter(m -> m.getHomeTeam().equals(dodgers)).toList());
        dodgers.getAwayMatches().addAll(matches.stream().filter(m -> m.getAwayTeam().equals(dodgers)).toList());

        this.teamRepository.save(yankees);
        this.teamRepository.save(toronto);
        this.teamRepository.save(detroit);
        this.teamRepository.save(dodgers);
    }
}