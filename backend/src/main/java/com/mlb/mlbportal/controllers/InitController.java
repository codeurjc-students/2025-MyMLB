package com.mlb.mlbportal.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
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
import com.mlb.mlbportal.models.enums.MatchStatus;
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

        LocalDateTime now = LocalDateTime.now();

        List<Match> matches = List.of(
            new Match(yankees, toronto, 3, 5, now.minusDays(21), MatchStatus.Finished),
            new Match(yankees, toronto, 6, 2, now.minusDays(20), MatchStatus.Finished),
            new Match(yankees, toronto, 1, 4, now.minusDays(19), MatchStatus.Finished),
                
            new Match(yankees, dodgers, 2, 5, now.minusDays(18), MatchStatus.Finished),
            new Match(yankees, dodgers, 2, 0, now.minusDays(17), MatchStatus.Finished),
            new Match(yankees, dodgers, 4, 3, now.minusDays(16), MatchStatus.Finished),
                
            new Match(detroit, toronto, 7, 0, now.minusDays(15), MatchStatus.Finished),
            new Match(detroit, toronto, 2, 1, now.minusDays(14), MatchStatus.Finished),
            new Match(detroit, toronto, 8, 5, now.minusDays(13), MatchStatus.Finished),
                
            new Match(yankees, detroit, 6, 4, now.minusDays(12), MatchStatus.Finished),
            new Match(yankees, detroit, 0, 4, now.minusDays(11), MatchStatus.Finished),
            new Match(yankees, detroit, 4, 7, now.minusDays(10), MatchStatus.Finished),
                
            new Match(yankees, toronto, 4, 7, now.minusDays(9), MatchStatus.Finished),
            new Match(yankees, toronto, 4, 0, now.minusDays(8), MatchStatus.Finished),
            new Match(yankees, toronto, 6, 2, now.minusDays(7), MatchStatus.Finished),
                
            new Match(detroit, dodgers, 4, 3, now.minusDays(6), MatchStatus.Finished),
            new Match(detroit, dodgers, 9, 6, now.minusDays(5), MatchStatus.Finished),
            new Match(detroit, dodgers, 7, 0, now.minusDays(4), MatchStatus.Finished),
            
            new Match(dodgers, toronto, 7, 0, now.minusDays(3), MatchStatus.Finished),
            new Match(dodgers, toronto, 2, 5, now.minusDays(2), MatchStatus.Finished),
            new Match(dodgers, toronto, 2, 4, now.minusDays(1), MatchStatus.Finished),

            new Match(yankees, dodgers, 0, 0, now, MatchStatus.Scheduled),
            new Match(detroit, toronto, 1, 0, LocalDateTime.of(2025, 10, 20, 00, 00), MatchStatus.InProgress),
            new Match(detroit, yankees, 3, 9, LocalDateTime.of(2025, 10, 20, 9, 30), MatchStatus.Finished)
        );
        this.matchRepository.saveAll(matches);

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