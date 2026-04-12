package com.mlb.mlbportal.services.player;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import com.mlb.mlbportal.dto.player.CreatePlayerRequest;
import com.mlb.mlbportal.dto.player.PlayerRankingsDTO;
import com.mlb.mlbportal.handler.badRequest.InvalidTypeException;
import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;
import com.mlb.mlbportal.models.enums.PitcherPositions;
import com.mlb.mlbportal.models.enums.PlayerPositions;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.mlb.mlbportal.dto.player.PlayerDTO;
import com.mlb.mlbportal.dto.player.pitcher.EditPitcherRequest;
import com.mlb.mlbportal.dto.player.pitcher.PitcherDTO;
import com.mlb.mlbportal.dto.player.position.EditPositionPlayerRequest;
import com.mlb.mlbportal.dto.player.position.PositionPlayerDTO;
import com.mlb.mlbportal.handler.conflict.PlayerAlreadyExistsException;
import com.mlb.mlbportal.handler.conflict.RosterFullException;
import com.mlb.mlbportal.handler.notFound.TeamNotFoundException;
import com.mlb.mlbportal.mappers.player.PitcherMapper;
import com.mlb.mlbportal.mappers.player.PositionPlayerMapper;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.others.PictureInfo;
import com.mlb.mlbportal.models.player.Pitcher;
import com.mlb.mlbportal.models.player.Player;
import com.mlb.mlbportal.models.player.PositionPlayer;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.repositories.player.PitcherRepository;
import com.mlb.mlbportal.repositories.player.PlayerRepository;
import com.mlb.mlbportal.repositories.player.PositionPlayerRepository;
import com.mlb.mlbportal.services.uploader.PictureService;

@Service
@RequiredArgsConstructor
public class PlayerService {
    private final PlayerRepository playerRepository;
    private final PositionPlayerRepository positionPlayerRepository;
    private final PitcherRepository pitcherRepository;

    private final PositionPlayerMapper positionPlayerMapper;
    private final PitcherMapper pitcherMapper;

    private final TeamRepository teamRepository;

    private final PictureService pictureService;

    @PersistenceContext
    private EntityManager entityManager;

    private PlayerService self;

    @Autowired
    public void setSelf(@Lazy PlayerService self) {
        this.self = self;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "get-players", key = "{#playerType, #playerName, #teamName, #page, #size}")
    public Page<PlayerDTO> getAllPlayers(String playerType, String playerName, String teamName, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        // Player by Name
        if (playerName != null && !playerName.isBlank()) {
            Player player = this.playerRepository.findByNameOrThrow(playerName);
            return new PageImpl<>(List.of(this.mapToDTO(player)), pageable, 1);
        }

        // Players of a certain type
        if (playerType != null) {
            return this.getPlayersByTypeAndTeam(playerType, teamName, pageable);
        }

        // All Players (without filters)
        Page<? extends Player> playerPage;
        if (teamName != null) {
            Team team = this.teamRepository.findByNameOrThrow(teamName);
            playerPage = this.playerRepository.findByTeamOrderByNameAsc(team, pageable);
        }
        else {
            playerPage = this.playerRepository.findAll(pageable);
        }
        return playerPage.map(this::mapToDTO);
    }

    /**
     * Helper method that manages the case when the playerType and team filters are active.
     *
     * @param playerType position or pitcher.
     * @param teamName the name of the team to filter.
     * @param pageable page configuration.
     * @return the paginated result.
     */
    private Page<PlayerDTO> getPlayersByTypeAndTeam(String playerType, String teamName, Pageable pageable) {
        Team team = (teamName != null) ? this.teamRepository.findByNameOrThrow(teamName) : null;

        if ("position".equalsIgnoreCase(playerType)) {
            Page<PositionPlayer> players = (team != null)
                    ? this.positionPlayerRepository.findByTeamOrderByNameAsc(team, pageable)
                    : this.positionPlayerRepository.findAll(pageable);
            return players.map(this.positionPlayerMapper::toPositionPlayerDTO);
        }

        if ("pitcher".equalsIgnoreCase(playerType)) {
            Page<Pitcher> players = (team != null)
                    ? this.pitcherRepository.findByTeamOrderByNameAsc(team, pageable)
                    : this.pitcherRepository.findAll(pageable);
            return players.map(this.pitcherMapper::toPitcherDTO);
        }
        throw new InvalidTypeException("Invalid player type: " + playerType);
    }

    /**
     * Manual PlayerDTO mapper.
     *
     * @param player to map
     * @return mapped player
     */
    public PlayerDTO mapToDTO(Player player) {
        if (player instanceof PositionPlayer pp) {
            return this.positionPlayerMapper.toPositionPlayerDTO(pp);
        }
        else if (player instanceof Pitcher p) {
            return this.pitcherMapper.toPitcherDTO(p);
        }
        throw new InvalidTypeException("Unknown player subclass");
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "single-stat-player-rankings", key = "{#stat, #playerType, #teamNames, #league, #division, #page, #size}")
    public Page<PlayerRankingsDTO> getTopPlayersRanking(int page, int size, String playerType, String stat, List<String> teamNames, League league, Division division) {
        String tableName = QueryBuilder.getTableName(playerType);
        if (!QueryBuilder.isValidStat(stat, playerType)) {
            throw new InvalidTypeException("The provided stat is not valid");
        }
        Pageable pageable = PageRequest.of(page, size);
        boolean hasTeamFilter = (teamNames != null && !teamNames.isEmpty());
        boolean hasLeagueFilter = league != null;
        boolean hasDivisionFilter = division != null;

        // Data Query Builder
        String dataQueryString = QueryBuilder.buildDataQuery(stat, tableName, hasTeamFilter, hasLeagueFilter, hasDivisionFilter);
        TypedQuery<PlayerRankingsDTO> dataQuery = this.entityManager.createQuery(dataQueryString, PlayerRankingsDTO.class);

        // Count Query Builder (For pagination)
        String countQueryString = QueryBuilder.buildCountQuery(stat, tableName, hasTeamFilter, hasLeagueFilter, hasDivisionFilter);
        TypedQuery<Long> countQuery = this.entityManager.createQuery(countQueryString, Long.class);
        QueryBuilder.setQueryParams(dataQuery, countQuery, teamNames, league, division, hasTeamFilter, hasLeagueFilter, hasDivisionFilter);

        // Pagination
        dataQuery.setFirstResult((int) pageable.getOffset());
        dataQuery.setMaxResults(pageable.getPageSize());
        List<PlayerRankingsDTO> content = dataQuery.getResultList();
        Long totalElements = countQuery.getSingleResult();

        return new PageImpl<>(content, pageable, totalElements);
    }

    @Cacheable(value = "all-stats-player-rankings", key = "{#playerType, #teamNames, #league, #division}")
    public Map<String, List<PlayerRankingsDTO>> getAllStatsRankings(String playerType, List<String> teamNames, League league, Division division) {
        List<String> allStats = QueryBuilder.getPlayerStats(playerType);
        Map<String, List<PlayerRankingsDTO>> result = new ConcurrentHashMap<>();

        List<CompletableFuture<Void>> futureTask = allStats.stream().map(stat -> CompletableFuture.runAsync(() -> {
            List<PlayerRankingsDTO> rankings = this.self.getTopPlayersRanking(0, 20, playerType, stat, teamNames, league, division).getContent();
            result.put(stat, rankings);
        })).toList();

        CompletableFuture.allOf(futureTask.toArray(new CompletableFuture[0])).join();
        return result;
    }

    /**
     * Helper method that checks whether the given team roster has available slots for new players.
     *
     * @param team the team whose roster size will be validated
     * @throws RosterFullException if the roster already contains 24 or more players
     */
    private void checkTeamAvailability(Team team) {
        int rosterSize = team.getPositionPlayers().size() + team.getPitchers().size();
        if (rosterSize >= 24) {
            throw new RosterFullException(team.getName() + " roster is full");
        }
    }

    /**
     * Validates player creation by checking duplicates and team availability.
     *
     * @param playerName the name of the new player
     * @param teamName the team to assign
     * @return the validated team
     * @throws PlayerAlreadyExistsException if a player with the same name exists
     * @throws TeamNotFoundException if the team does not exist
     * @throws RosterFullException if the team roster is full
     */
    private Team playerCreationValidations(String playerName, String teamName) {
        if (this.playerRepository.findByName(playerName).isPresent()) {
            throw new PlayerAlreadyExistsException();
        }
        Team team = this.teamRepository.findByNameOrThrow(teamName);
        this.checkTeamAvailability(team);
        return team;
    }

    /**
     * @implNote Creating a player means that the player has been promoted to the MLB team.
     * At creation time, the team must not be null; however, once the player has played in the MLB,
     * the team may be null, which implies that the player has been sent down to the minors or designated for assignment (DFA).
     */
    @Transactional
    @CacheEvict(value = {"get-players", "all-stats-player-rankings", "single-stat-player-rankings", "get-teams", "search-player"}, allEntries = true)
    public PlayerDTO createPlayer(String playerType, CreatePlayerRequest<?> request) {
        Team team = this.playerCreationValidations(request.name(), request.teamName());
        Player newPlayer;
        if ("position".equalsIgnoreCase(playerType)) {
            if (!(request.position() instanceof PlayerPositions pos)) {
                throw new InvalidTypeException("Position must be a field position for position players");
            }
            newPlayer = new PositionPlayer(request.name(), request.playerNumber(), team, pos);
            team.addPositionPlayer((PositionPlayer) newPlayer);
        }
        else if ("pitcher".equalsIgnoreCase(playerType)) {
            if (!(request.position() instanceof PitcherPositions pos)) {
                throw new InvalidTypeException("Position must be a pitching position for pitchers");
            }
            newPlayer = new Pitcher(request.name(), request.playerNumber(), team, pos);
            team.addPitcher((Pitcher) newPlayer);
        }
        else {
            throw new InvalidTypeException("Invalid player type: " + playerType);
        }
        newPlayer.setApiDataSource(false);
        this.playerRepository.save(newPlayer);
        return this.mapToDTO(newPlayer);
    }

    @Transactional
    @CacheEvict(value = {"get-players", "all-stats-player-rankings", "single-stat-player-rankings", "get-teams", "search-player"}, allEntries = true)
    public PictureInfo updatePicture(String playerName, MultipartFile file) throws IOException {
        Player player = this.playerRepository.findByNameOrThrow(playerName);
        PictureInfo pictureInfo = this.pictureService.uploadPicture(file);
        player.setPicture(pictureInfo);
        this.playerRepository.save(player);
        return pictureInfo;
    }

    /**
     * Helper method to update the team of a player.
     * @param player the player to update.
     * @param teamNameOpt the request field corresponding to the new possible team.
     */
    private void updateTeamIfNeeded(Player player, Optional<String> teamNameOpt) {
        teamNameOpt.ifPresent(newTeamName -> {
            Team newTeam = this.teamRepository.findByNameOrThrow(newTeamName);
            this.checkTeamAvailability(newTeam);
            Team oldTeam = player.getTeam();
            if (oldTeam != null) {
                switch (player) {
                    case PositionPlayer pos -> oldTeam.removePositionPlayer(pos);
                    case Pitcher pit -> oldTeam.removePitcher(pit);
                    default -> throw new IllegalArgumentException("Unsupported player subtype: " + player.getClass().getName());
                }
            }

            player.setTeam(newTeam);
            switch (player) {
                case PositionPlayer pos -> newTeam.addPositionPlayer(pos);
                case Pitcher pit -> newTeam.addPitcher(pit);
                default -> throw new IllegalArgumentException("Unsupported player subtype: " + player.getClass().getName());
            }
        });
    }

    @Transactional
    @CacheEvict(value = {"get-players", "all-stats-player-rankings", "single-stat-player-rankings", "get-teams", "search-player"}, allEntries = true)
    public void updatePositionPlayer(String playerName, EditPositionPlayerRequest request) {
        PositionPlayer player = this.positionPlayerRepository.findByNameOrThrow(playerName);

        request.playerNumber().ifPresent(player::setPlayerNumber);
        request.position().ifPresent(player::setPosition);
        request.atBats().ifPresent(player::setAtBats);
        request.walks().ifPresent(player::setWalks);
        request.hits().ifPresent(player::setHits);
        request.doubles().ifPresent(player::setDoubles);
        request.triples().ifPresent(player::setTriples);
        request.rbis().ifPresent(player::setRbis);
        request.homeRuns().ifPresent(player::setHomeRuns);
        request.apiDataSource().ifPresent(player::setApiDataSource);
        this.updateTeamIfNeeded(player, request.teamName());

        this.playerRepository.save(player);
    }

    @Transactional
    @CacheEvict(value = {"get-players", "all-stats-player-rankings", "single-stat-player-rankings", "get-teams", "search-player"}, allEntries = true)
    public void updatePitcher(String playerName, EditPitcherRequest request) {
        Pitcher player = this.pitcherRepository.findByNameOrThrow(playerName);

        request.playerNumber().ifPresent(player::setPlayerNumber);
        request.position().ifPresent(player::setPosition);
        request.games().ifPresent(player::setGames);
        request.wins().ifPresent(player::setWins);
        request.losses().ifPresent(player::setLosses);
        request.inningsPitched().ifPresent(player::setInningsPitched);
        request.totalStrikeouts().ifPresent(player::setTotalStrikeouts);
        request.walks().ifPresent(player::setWalks);
        request.hitsAllowed().ifPresent(player::setHitsAllowed);
        request.runsAllowed().ifPresent(player::setRunsAllowed);
        request.saves().ifPresent(player::setSaves);
        request.saveOpportunities().ifPresent(player::setSaveOpportunities);
        request.apiDataSource().ifPresent(player::setApiDataSource);
        this.updateTeamIfNeeded(player, request.teamName());

        this.playerRepository.save(player);
    }

    /**
     * @implNote Deleting a player means that the player has retired from the MLB
     */
    @Transactional
    @CacheEvict(value = {"get-players", "all-stats-player-rankings", "single-stat-player-rankings", "get-teams", "search-player"}, allEntries = true)
    public PlayerDTO deletePlayer(String playerName) {
        Player player = this.playerRepository.findByNameOrThrow(playerName);
        Team team = this.teamRepository.findByNameOrThrow(player.getTeam().getName());

        PlayerDTO result = switch (player) {
            case PositionPlayer p -> {
                PositionPlayerDTO dto = this.positionPlayerMapper.toPositionPlayerDTO(p);
                team.removePositionPlayer(p);
                yield dto;
            }
            case Pitcher p -> {
                PitcherDTO dto = this.pitcherMapper.toPitcherDTO(p);
                team.removePitcher(p);
                yield dto;
            }
            default -> throw new IllegalArgumentException("Unexpected subtype: " + player.getClass().getName());
        };
        this.playerRepository.delete(player);
        return result;
    }
}