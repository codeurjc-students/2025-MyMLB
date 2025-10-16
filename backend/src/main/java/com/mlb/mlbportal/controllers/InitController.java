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
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.UserEntity;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.repositories.UserRepository;

import jakarta.annotation.PostConstruct;

@Controller
public class InitController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final TeamRepository teamRepository;

    @Value("${init.user1.password}")
    private String fonssiPassword;

    @Value("${init.user2.password}")
    private String arminPassword;

    public InitController(UserRepository userRepository, PasswordEncoder passsEncoder, TeamRepository teamRepo) {
        this.userRepository = userRepository;
        this.passwordEncoder = passsEncoder;

        this.teamRepository = teamRepo;
    }

    @PostConstruct
    public void init() {
        this.createAdmins();
        this.setUpTeams();
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
                        team.setLastTen(0);

                        return team;
                    })
                    .collect(Collectors.toList());

            this.teamRepository.saveAll(teams);
        } catch (IOException e) {
            throw new RuntimeException("Error loading team data from JSON", e);
        }
    }
}