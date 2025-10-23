package com.mlb.mlbportal.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mlb.mlbportal.dto.team.TeamInitDTO;
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
    public void init() throws IOException {
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

    private List<Match> generateBalancedMatches(List<Team> teams) {
        List<Match> matches = new ArrayList<>();
        int totalDays = 5;
        LocalDateTime baseDate = LocalDateTime.now().minusDays(2);

        List<Team> shuffled = new ArrayList<>(teams);
        Collections.shuffle(shuffled);

        Set<String> usedPairs = new HashSet<>();
        Random random = new Random();

        for (int day = 0; day < totalDays; day++) {
            List<Team> available = new ArrayList<>(shuffled);
            while (available.size() >= 2) {
                Team teamA = available.remove(0);
                Team opponent = null;

                for (Team candidate : available) {
                    String key1 = teamA.getAbbreviation() + "-" + candidate.getAbbreviation();
                    String key2 = candidate.getAbbreviation() + "-" + teamA.getAbbreviation();
                    if (!usedPairs.contains(key1) && !usedPairs.contains(key2)) {
                        opponent = candidate;
                        usedPairs.add(key1);
                        break;
                    }
                }

                if (opponent == null) continue;
                available.remove(opponent);

                boolean homeToday = (day % 2 == 0);
                Team home = homeToday ? teamA : opponent;
                Team away = homeToday ? opponent : teamA;

                // Generates a random hour between 10 and 10pm
                int randomHour = 10 + random.nextInt(13);
                int randomMinute = random.nextInt(60);
                LocalDateTime matchDate = baseDate.plusDays(day).withHour(randomHour).withMinute(randomMinute);

                // Determine the game status
                LocalDateTime now = LocalDateTime.now();
                MatchStatus status;
                if (matchDate.isAfter(now)) {
                    status = MatchStatus.Scheduled;
                } else if (matchDate.plusHours(3).isBefore(now)) {
                    status = MatchStatus.Finished;
                } else {
                    status = MatchStatus.InProgress;
                }

                Match match = new Match(
                    home,
                    away,
                    random.nextInt(10),
                    random.nextInt(10),
                    matchDate,
                    status
                );
                matches.add(match);
            }
        }
        return matches;
    }

    private void setUpMatches() {
        List<Team> allTeams = this.teamRepository.findAll();
        List<Match> matches = this.generateBalancedMatches(allTeams);
        this.matchRepository.saveAll(matches);

        for (Team team : allTeams) {
            team.getHomeMatches().addAll(matches.stream().filter(m -> m.getHomeTeam().equals(team)).toList());
            team.getAwayMatches().addAll(matches.stream().filter(m -> m.getAwayTeam().equals(team)).toList());
        }
        this.teamRepository.saveAll(allTeams);
    }
}