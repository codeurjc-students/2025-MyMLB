package com.mlb.mlbportal.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import com.mlb.mlbportal.models.ticket.Event;
import com.mlb.mlbportal.models.ticket.EventManager;
import com.mlb.mlbportal.models.ticket.Seat;
import com.mlb.mlbportal.models.ticket.Sector;
import com.mlb.mlbportal.repositories.ticket.EventManagerRepository;
import com.mlb.mlbportal.repositories.ticket.EventRepository;
import com.mlb.mlbportal.repositories.ticket.SectorRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mlb.mlbportal.dto.stadium.StadiumInitDTO;
import com.mlb.mlbportal.dto.team.TeamInitDTO;
import com.mlb.mlbportal.models.Match;
import com.mlb.mlbportal.models.Stadium;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.UserEntity;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;
import com.mlb.mlbportal.models.enums.MatchStatus;
import com.mlb.mlbportal.models.enums.PitcherPositions;
import com.mlb.mlbportal.models.enums.PlayerPositions;
import com.mlb.mlbportal.models.others.PictureInfo;
import com.mlb.mlbportal.models.player.Pitcher;
import com.mlb.mlbportal.models.player.PositionPlayer;
import com.mlb.mlbportal.repositories.MatchRepository;
import com.mlb.mlbportal.repositories.StadiumRepository;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DataInitializerService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final StadiumRepository stadiumRepository;

    private final MlbImportService mlbImportService;

    private final EventRepository eventRepository;
    private final EventManagerRepository eventManagerRepository;
    private final SectorRepository sectorRepository;

    private static final Random RANDOM = new Random();

    @Value("${init.user1.password}")
    private String fonssiPassword;

    @Value("${init.user2.password}")
    private String arminPassword;

    @Transactional
    public void init() {
        this.createAdmins();
        this.setUpTeams();
        this.setUpStadiums();
        this.setUpMatches();
        this.setUpPlayers();
        this.setUpEventsForTodayMatches();
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

                        Team team = new Team(
                                dto.name(),
                                dto.abbreviation(),
                                dto.wins(),
                                dto.losses(),
                                dto.city(),
                                dto.generalInfo(),
                                dto.championships() != null ? dto.championships() : new ArrayList<>()
                        );
                        team.setLeague(league);
                        team.setDivision(division);

                        int totalGames = dto.wins() + dto.losses();
                        team.setTotalGames(totalGames);
                        team.setPct((double) dto.wins() / totalGames);
                        team.setGamesBehind(0);
                        team.setLastTen("0-0");
                        team.setTeamLogo(dto.abbreviation() + ".png");

                        return team;
                    }).collect(Collectors.toCollection(ArrayList::new ));

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

        Match match = new Match(
                away,
                home,
                RANDOM.nextInt(10),
                RANDOM.nextInt(10),
                matchDate,
                status
        );
        match.setStadium(home.getStadium());
        return match;
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

    // DELETE
    private void addTestMatch(List<Match> matches, List<Team> allTeams) {
        if (allTeams.size() < 2) return;

        Team home = allTeams.get(0);
        Team away = allTeams.get(1);

        Team aux1 = allTeams.get(3);
        Team aux2 = allTeams.get(4);

        LocalDateTime matchDate = LocalDateTime.now().plusMinutes(15);

        Match testMatch = new Match(
                home,
                away,
                0,
                0,
                matchDate,
                MatchStatus.SCHEDULED
        );

        Match match2 = new Match(
                aux1,
                aux2,
                0,
                0,
                matchDate,
                MatchStatus.SCHEDULED
        );
        matches.add(testMatch);
        matches.add(match2);
    }

    private void setUpMatches() {
        List<Team> allTeams = this.teamRepository.findAll();
        List<Match> matches = this.generateBalancedMatches(allTeams);
        //this.addTestMatch(matches, allTeams);
        this.matchRepository.saveAll(matches);

        this.mlbImportService.getOfficialMatches(
                LocalDate.of(2026, Month.MARCH, 1),
                LocalDate.of(2026, Month.OCTOBER, 1)
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

    @SuppressWarnings("unchecked")
    private void setUpPlayers() {
        ObjectMapper mapper = new ObjectMapper();

        try (InputStream input = getClass().getResourceAsStream("/data/players-2025.json")) {
            List<Map<String, Object>> rawPlayers = mapper.readValue(input, List.class);
            List<Team> allTeams = teamRepository.findAll();

            Map<String, Team> teamMap = allTeams.stream()
                    .collect(Collectors.toMap(t -> t.getName().toLowerCase(), t -> t));

            Map<Team, List<PositionPlayer>> positionPlayersByTeam = new HashMap<>();
            Map<Team, List<Pitcher>> pitchersByTeam = new HashMap<>();

            for (Map<String, Object> raw : rawPlayers) {
                String type = (String) raw.get("type");
                String name = (String) raw.get("name");
                int playerNumber = (int) raw.get("playerNumber");
                String teamName = ((String) raw.get("teamName")).toLowerCase();
                Map<String, Object> pictureMap = (Map<String, Object>) raw.get("picture");
                PictureInfo picture = mapper.convertValue(pictureMap, PictureInfo.class);

                Team team = teamMap.get(teamName);
                if (team == null) continue;

                if ("Pitcher".equalsIgnoreCase(type)) {
                    Pitcher pitcher = new Pitcher(
                            name,
                            playerNumber,
                            team,
                            PitcherPositions.valueOf(((String) raw.get("position")).toUpperCase()),
                            ((Number) raw.get("games")).intValue(),
                            ((Number) raw.get("wins")).intValue(),
                            ((Number) raw.get("losses")).intValue()
                    );
                    pitcher.setInningsPitched(((Number) raw.get("inningsPitched")).intValue());
                    pitcher.setTotalStrikeouts(((Number) raw.get("totalStrikeouts")).intValue());
                    pitcher.setWalks(((Number) raw.get("walks")).intValue());
                    pitcher.setHitsAllowed(((Number) raw.get("hitsAllowed")).intValue());
                    pitcher.setRunsAllowed(((Number) raw.get("runsAllowed")).intValue());
                    pitcher.setSaves(((Number) raw.get("saves")).intValue());
                    pitcher.setSaveOpportunities(((Number) raw.get("saveOpportunities")).intValue());
                    pitchersByTeam.computeIfAbsent(team, t -> new ArrayList<>()).add(pitcher);
                    pitcher.setPicture(picture);
                } else {
                    PositionPlayer player = new PositionPlayer(
                            name,
                            playerNumber,
                            team,
                            PlayerPositions.fromLabel((String) raw.get("position")),
                            ((Number) raw.get("atBats")).intValue(),
                            ((Number) raw.get("walks")).intValue(),
                            ((Number) raw.get("hits")).intValue()
                    );
                    player.setDoubles(((Number) raw.get("doubles")).intValue());
                    player.setTriples(((Number) raw.get("triples")).intValue());
                    player.setHomeRuns(((Number) raw.get("homeRuns")).intValue());
                    player.setRbis(((Number) raw.get("rbis")).intValue());
                    positionPlayersByTeam.computeIfAbsent(team, t -> new ArrayList<>()).add(player);
                    player.setPicture(picture);
                }
            }

            for (Team team : allTeams) {
                List<PositionPlayer> positionPlayers = positionPlayersByTeam.getOrDefault(team, new ArrayList<>());
                List<Pitcher> pitchers = pitchersByTeam.getOrDefault(team, new ArrayList<>());

                team.setPositionPlayers(positionPlayers);
                team.setPitchers(pitchers);
            }
            teamRepository.saveAll(allTeams);

        } catch (IOException e) {
            throw new UncheckedIOException("Error loading players data", e);
        }
    }

    private void setUpEventsForTodayMatches() {
        List<Match> todayMatches = this.matchRepository.findAll().stream()
                .filter(m -> m.getDate().toLocalDate().isEqual(LocalDate.now()))
                //.filter(m -> m.getStatus() == MatchStatus.SCHEDULED)
                .toList();

        List<Event> eventsToSave = new ArrayList<>();

        for (Match match : todayMatches) {
            Stadium stadium = match.getStadium();
            if (stadium == null) continue;

            Event event = new Event();
            event.setMatch(match);

            List<Sector> eventSectors = this.createSectorsForStadium(stadium);

            for (Sector sector : eventSectors) {
                double basePrice = sector.getName().contains("VIP") ? 150.0 : 35.0;
                double finalPrice = basePrice + RANDOM.nextInt(20);

                EventManager em = new EventManager(event, sector, finalPrice);
                event.addEventManager(em);
            }
            eventsToSave.add(event);
        }
        this.eventRepository.saveAll(eventsToSave);
    }

    private List<Sector> createSectorsForStadium(Stadium stadium) {
        List<Sector> sectors = Arrays.asList(
                new Sector(0, "Grada Norte", stadium, new ArrayList<>(), 100),
                new Sector(0, "Grada Sur", stadium, new ArrayList<>(), 100),
                new Sector(0, "Preferencia", stadium, new ArrayList<>(), 50),
                new Sector(0, "Palco VIP", stadium, new ArrayList<>(), 20)
        );
        for (Sector sector : sectors) {
            this.createSeatsForSector(sector);
        }
        return this.sectorRepository.saveAll(sectors);
    }

    private void createSeatsForSector(Sector sector) {
        int capacity = sector.getTotalCapacity();
        String prefix = sector.getName().substring(0, 1).toUpperCase();

        for (int i = 1; i <= capacity; i++) {
            Seat seat = new Seat();
            seat.setName(prefix + "-" + i);
            seat.setOccupied(false);
            sector.addSeat(seat);
        }
    }
}