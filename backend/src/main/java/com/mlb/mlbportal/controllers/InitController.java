package com.mlb.mlbportal.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mlb.mlbportal.dto.stadium.StadiumInitDTO;
import com.mlb.mlbportal.dto.team.TeamInitDTO;
import com.mlb.mlbportal.handler.notFound.TeamNotFoundException;
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
import com.mlb.mlbportal.repositories.MatchRepository;
import com.mlb.mlbportal.repositories.StadiumRepository;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.repositories.UserRepository;
import com.mlb.mlbportal.repositories.player.PitcherRepository;
import com.mlb.mlbportal.repositories.player.PositionPlayerRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class InitController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final StadiumRepository stadiumRepository;
    private final PositionPlayerRepository positionPlayerRepository;
    private final PitcherRepository pitcherRepository;

    private static final Random RANDOM = new Random();

    @Value("${init.user1.password}")
    private String fonssiPassword;

    @Value("${init.user2.password}")
    private String arminPassword;

    @PostConstruct
    public void init() {
        this.createAdmins();
        this.setUpTeams();
        this.setUpStadiums();
        this.setUpMatches();
        this.setUpPlayers();
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
        mapper.registerModule(new JavaTimeModule());

        try (InputStream input = getClass().getResourceAsStream("/data/teams-2025.json")) {
            TeamInitDTO[] teamDtos = mapper.readValue(input, TeamInitDTO[].class);

            List<Team> teams = Arrays.stream(teamDtos)
                .map(dto -> {
                    League league = League.valueOf(dto.league().toUpperCase());
                    Division division = Division.valueOf(dto.division().toUpperCase());

                    Team team = new Team(
                        dto.name(),
                        dto.abbreviation(),
                        dto.wins(),
                        dto.losses(),
                        dto.city(),
                        dto.generalInfo(),
                        dto.championships() != null ? dto.championships() : List.of(),
                        league,
                        division
                    );

                    int totalGames = dto.wins() + dto.losses();
                    team.setTotalGames(totalGames);
                    team.setPct((double) dto.wins() / totalGames);
                    team.setGamesBehind(0);
                    team.setLastTen("0-0");
                    team.setTeamLogo(dto.abbreviation() + ".png");

                    return team;
                })
                .toList();

            this.teamRepository.saveAll(teams);
        } catch (IOException e) {
            throw new UncheckedIOException("Error loading team data from JSON", e);
        }
    }

    private List<Match> generateBalancedMatches(List<Team> teams) {
        List<Match> matches = new ArrayList<>();
        Set<String> usedPairs = new HashSet<>();

        List<Team> shuffled = new ArrayList<>(teams);
        Collections.shuffle(shuffled);

        int totalDays = 5;
        LocalDateTime baseDate = LocalDateTime.now().minusDays(2);

        for (int day = 0; day < totalDays; day++) {
            generateDailyMatches(matches, shuffled, usedPairs, baseDate, day);
        }

        return matches;
    }

    private void generateDailyMatches(List<Match> matches, List<Team> teams, Set<String> usedPairs, LocalDateTime baseDate, int day) {
        List<Team> available = new ArrayList<>(teams);

        while (available.size() >= 2) {
            Team teamA = available.remove(0);
            Team opponent = findOpponent(teamA, available, usedPairs);

            if (opponent == null) continue;
            available.remove(opponent);

            Match match = createMatch(teamA, opponent, baseDate, day);
            matches.add(match);
        }
    }

    private Team findOpponent(Team teamA, List<Team> available, Set<String> usedPairs) {
        for (Team candidate : available) {
            String key1 = teamA.getAbbreviation() + "-" + candidate.getAbbreviation();
            String key2 = candidate.getAbbreviation() + "-" + teamA.getAbbreviation();

            if (!usedPairs.contains(key1) && !usedPairs.contains(key2)) {
                usedPairs.add(key1);
                return candidate;
            }
        }
        return null;
    }

    private Match createMatch(Team teamA, Team opponent, LocalDateTime baseDate, int day) {
        boolean homeToday = (day % 2 == 0);
        Team home = homeToday ? teamA : opponent;
        Team away = homeToday ? opponent : teamA;

        LocalDateTime matchDate = generateRandomMatchDate(baseDate, day);
        MatchStatus status = determineMatchStatus(matchDate);

        return new Match(
            home,
            away,
            RANDOM.nextInt(10),
            RANDOM.nextInt(10),
            matchDate,
            status
        );
    }

    private LocalDateTime generateRandomMatchDate(LocalDateTime baseDate, int day) {
        int randomHour = 10 + RANDOM.nextInt(13);
        int randomMinute = RANDOM.nextInt(60);
        return baseDate.plusDays(day).withHour(randomHour).withMinute(randomMinute);
    }

    private MatchStatus determineMatchStatus(LocalDateTime matchDate) {
        LocalDateTime now = LocalDateTime.now();
        if (matchDate.isAfter(now)) return MatchStatus.SCHEDULED;
        if (matchDate.plusHours(3).isBefore(now)) return MatchStatus.FINISHED;
        return MatchStatus.IN_PROGRESS;
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

    @SuppressWarnings("unchecked")
    private void setUpPlayers() {
        ObjectMapper mapper = new ObjectMapper();

        try (InputStream input = getClass().getResourceAsStream("/data/players-2025.json")) {
            List<Map<String, Object>> rawPlayers = mapper.readValue(input, List.class);
            Team yankees = this.teamRepository.findByName("New York Yankees")
                    .orElseThrow(() -> new TeamNotFoundException());

            List<PositionPlayer> positionPlayers = new ArrayList<>();
            List<Pitcher> pitchers = new ArrayList<>();

            for (Map<String, Object> raw : rawPlayers) {
                String type = (String) raw.get("type");
                String name = (String) raw.get("name");

                if ("Pitcher".equalsIgnoreCase(type)) {
                    Pitcher pitcher = new Pitcher(
                        name,
                        yankees,
                        PitcherPositions.valueOf(((String) raw.get("position")).toUpperCase()),
                        ((Number) raw.get("games")).intValue(),
                        ((Number) raw.get("wins")).intValue(),
                        ((Number) raw.get("losses")).intValue(),
                        ((Number) raw.get("inningsPitched")).intValue(),
                        ((Number) raw.get("totalStrikeouts")).intValue(),
                        ((Number) raw.get("walks")).intValue(),
                        ((Number) raw.get("hitsAllowed")).intValue(),
                        ((Number) raw.get("runsAllowed")).intValue(),
                        ((Number) raw.get("saves")).intValue(),
                        ((Number) raw.get("saveOpportunities")).intValue()
                    );
                    pitchers.add(pitcher);
                } else {
                    PositionPlayer player = new PositionPlayer(
                        name,
                        yankees,
                        PlayerPositions.fromLabel((String) raw.get("position")),
                        ((Number) raw.get("atBats")).intValue(),
                        ((Number) raw.get("walks")).intValue(),
                        ((Number) raw.get("hits")).intValue(),
                        ((Number) raw.get("doubles")).intValue(),
                        ((Number) raw.get("triples")).intValue(),
                        ((Number) raw.get("homeRuns")).intValue(),
                        ((Number) raw.get("rbis")).intValue()
                    );
                    positionPlayers.add(player);
                }
            }

            this.positionPlayerRepository.saveAll(positionPlayers);
            this.pitcherRepository.saveAll(pitchers);

        } catch (IOException e) {
            throw new UncheckedIOException("Error loading players data", e);
        }
    }
}