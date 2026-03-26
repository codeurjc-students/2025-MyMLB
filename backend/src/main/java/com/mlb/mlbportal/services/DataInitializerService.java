package com.mlb.mlbportal.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mlb.mlbportal.dto.stadium.StadiumInitDTO;
import com.mlb.mlbportal.dto.team.TeamInitDTO;
import com.mlb.mlbportal.models.Stadium;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.UserEntity;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;
import com.mlb.mlbportal.repositories.StadiumRepository;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.repositories.UserRepository;
import com.mlb.mlbportal.services.mlbAPI.MatchImportService;
import com.mlb.mlbportal.services.mlbAPI.PlayerImportService;
import com.mlb.mlbportal.services.mlbAPI.TeamImportService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataInitializerService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final TeamRepository teamRepository;
    private final StadiumRepository stadiumRepository;

    private final MatchImportService matchImportService;
    private final TeamImportService teamImportService;
    private final PlayerImportService playerImportService;

    @Value("${init.user1.password}")
    private String fonssiPassword;

    @Value("${init.user2.password}")
    private String arminPassword;

    @Transactional
    public void init() {
        this.createAdmins();
        this.setUpTeams();
        this.syncStatsApiIdWithTeams();
        this.teamRepository.flush();
        this.setUpStadiums();
        this.setUpMatches();
        this.playerImportService.getTeamRoster();
    }

    public boolean isDBEmpty() {
        return this.userRepository.count() == 0;
    }

    private void createAdmins() {
        UserEntity fonssiUser = new UserEntity("fonssitorodriguezgutt@gmail.com", "fonssi29",
                this.passwordEncoder.encode(this.fonssiPassword));
        UserEntity arminUser = new UserEntity("armin@gmail.com", "armiin13",
                this.passwordEncoder.encode(this.arminPassword));

        fonssiUser.getRoles().add("USER");
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

                        Team team = new Team(
                                dto.name(),
                                dto.abbreviation(),
                                dto.city(),
                                dto.generalInfo(),
                                dto.championships() != null ? dto.championships() : new ArrayList<>()
                        );
                        team.setLeague(league);
                        team.setDivision(division);
                        team.setTeamLogo(dto.abbreviation() + ".png");
                        return team;
                    }).collect(Collectors.toCollection(ArrayList::new ));

            this.teamRepository.saveAll(teams);
        } catch (IOException e) {
            throw new UncheckedIOException("Error loading team data from JSON", e);
        }
    }

    private void syncStatsApiIdWithTeams() {
        List<Team> teams = this.teamRepository.findAll();
        Map<String, Integer> map = this.teamImportService.findStatsApiId();
        for (Team team : teams) {
            Integer statApiId = map.get(team.getName());
            if (statApiId != null) {
                team.setStatsApiId(statApiId);
            }
            else {
                log.warn("Cannot find the Stat API ID for team: {}", team.getName());
            }
        }
        this.teamRepository.saveAll(teams);
        this.teamImportService.getTeamStats();
    }

    private void setUpMatches() {
        List<Team> allTeams = this.teamRepository.findAll();
        this.matchImportService.getOfficialMatches(
                LocalDate.of(2026, Month.MARCH, 1),
                LocalDate.of(2026, Month.OCTOBER, 20)
        );
        this.teamRepository.saveAll(allTeams);
    }

    private void setUpStadiums() {
        ObjectMapper mapper = new ObjectMapper();

        try (InputStream input = getClass().getResourceAsStream("/data/stadiums-2025.json")) {
            StadiumInitDTO[] stadiumDtos = mapper.readValue(input, StadiumInitDTO[].class);

            List<Team> allTeams = teamRepository.findAll();
            List<Stadium> stadiums = new ArrayList<>();

            for (StadiumInitDTO dto : stadiumDtos) {
                Team team = allTeams.stream()
                        .filter(t -> t.getName().equalsIgnoreCase(dto.teamName()))
                        .findFirst()
                        .orElse(null);

                Stadium stadium = new Stadium(dto.name(), dto.openingDate(), team);

                if (dto.pictures() != null) {
                    stadium.getPictures().addAll(dto.pictures());
                }

                if (dto.pictureMap() != null) {
                    stadium.setPictureMap(dto.pictureMap());
                }

                if (team != null) {
                    team.setStadium(stadium);
                }

                stadiums.add(stadium);
            }
            this.stadiumRepository.saveAll(stadiums);
            this.teamRepository.saveAll(allTeams);

        } catch (IOException e) {
            throw new UncheckedIOException("Error loading stadium data from JSON", e);
        }
    }
}