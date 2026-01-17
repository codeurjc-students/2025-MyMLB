package com.mlb.mlbportal.services.player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.mlb.mlbportal.dto.player.PlayerDTO;
import com.mlb.mlbportal.dto.player.pitcher.CreatePitcherRequest;
import com.mlb.mlbportal.dto.player.pitcher.EditPitcherRequest;
import com.mlb.mlbportal.dto.player.pitcher.PitcherDTO;
import com.mlb.mlbportal.dto.player.pitcher.PitcherSummaryDTO;
import com.mlb.mlbportal.dto.player.position.CreatePositionPlayerRequest;
import com.mlb.mlbportal.dto.player.position.EditPositionPlayerRequest;
import com.mlb.mlbportal.dto.player.position.PositionPlayerDTO;
import com.mlb.mlbportal.dto.player.position.PositionPlayerSummaryDTO;
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
import com.mlb.mlbportal.services.utilities.PaginationHandlerService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PlayerService {
    private final PlayerRepository playerRepository;
    private final PositionPlayerRepository positionPlayerRepository;
    private final PitcherRepository pitcherRepository;

    private final PositionPlayerMapper positionPlayerMapper;
    private final PitcherMapper pitcherMapper;

    private final TeamRepository teamRepository;

    private final PictureService pictureService;

    private final PaginationHandlerService paginationHandlerService;

    @Transactional
    public Page<PlayerDTO> getAllPlayers(int page, int size) {
        List<PositionPlayer> positionPlayers = this.positionPlayerRepository.findAll();
        List<Pitcher> pitcherList = this.pitcherRepository.findAll();

        this.updateAndSaveStats(positionPlayers);
        this.updateAndSaveStats(pitcherList);

        List<PositionPlayerDTO> mappedPositionPlayers = this.positionPlayerMapper
                .toListPositionPlayerDTO(positionPlayers);
        List<PitcherDTO> mappedPitchers = this.pitcherMapper.toListPitcherDTO(pitcherList);

        List<PlayerDTO> result = new ArrayList<>();
        result.addAll(mappedPositionPlayers);
        result.addAll(mappedPitchers);

        result.sort((p1, p2) -> p1.name().compareToIgnoreCase(p2.name()));
        return this.paginationHandlerService.paginateAndMap(result, page, size, Function.identity());
    }

    @Transactional
    public Page<PositionPlayerDTO> getAllPositionPlayers(int page, int size) {
        List<PositionPlayer> positionPlayers = this.positionPlayerRepository.findAll();
        this.updateAndSaveStats(positionPlayers);
        positionPlayers.sort((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()));
        return this.paginationHandlerService.paginateAndMap(positionPlayers, page, size, this.positionPlayerMapper::toPositionPlayerDTO);
    }

    @Transactional
    public Page<PitcherDTO> getAllPitchers(int page, int size) {
        List<Pitcher> pitchers = this.pitcherRepository.findAll();
        this.updateAndSaveStats(pitchers);
        pitchers.sort((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()));
        return this.paginationHandlerService.paginateAndMap(pitchers, page, size, this.pitcherMapper::toPitcherDTO);
    }

    /**
     * Updates and persists a player if their stats have changed.
     *
     * @param player the player to check and save
     * @throws IllegalArgumentException if the player subtype is not supported
     */
    private void saveIfStatsChanged(Player player) {
        if (PlayerServiceOperations.updatePlayerStats(player)) {
            switch (player) {
                case PositionPlayer pp -> this.positionPlayerRepository.save(pp);
                case Pitcher p -> this.pitcherRepository.save(p);
                default -> throw new IllegalArgumentException("Unexpected subtype: " + player.getClass().getName());
            }
        }
    }

    private <T extends Player> void updateAndSaveStats(List<T> players) {
        players.forEach(this::saveIfStatsChanged);
    }

    @Transactional
    public PlayerDTO findPlayerByName(String name) {
        Player player = this.playerRepository.findByNameOrThrow(name);
        this.saveIfStatsChanged(player);
        return (player instanceof PositionPlayer pp)
                ? positionPlayerMapper.toPositionPlayerDTO(pp)
                : pitcherMapper.toPitcherDTO((Pitcher) player);
    }

    public List<PositionPlayer> getUpdatedPositionPlayersOfTeam(Team team) {
        List<PositionPlayer> players = this.positionPlayerRepository.findByTeamOrderByNameAsc(team);

        players.forEach(p -> {
            boolean hasChanged = PlayerServiceOperations.updatePlayerStats(p);
            if (hasChanged) {
                this.positionPlayerRepository.save(p);
            }
        });
        return players;
    }
    
    public List<Pitcher> getUpdatedPitchersOfTeam(Team team) {
        List<Pitcher> pitchers = this.pitcherRepository.findByTeamOrderByNameAsc(team);

        pitchers.forEach(p -> {
            boolean hasChanged = PlayerServiceOperations.updatePlayerStats(p);
            if (hasChanged) {
                this.pitcherRepository.save(p);
            }
        });
        return pitchers;
    }

    @Transactional
    public Page<PositionPlayerSummaryDTO> getAllPositionPlayersOfATeam(String teamName, int page, int size) {
        Team team = this.teamRepository.findByNameOrThrow(teamName);
        List<PositionPlayer> players = this.getUpdatedPositionPlayersOfTeam(team);
        return this.paginationHandlerService.paginateAndMap(players, page, size, this.positionPlayerMapper::toPositionPlayerSummaryDTO);
    }

    @Transactional
    public Page<PitcherSummaryDTO> getAllPitchersOfATeam(String teamName, int page, int size) {
        Team team = this.teamRepository.findByNameOrThrow(teamName);
        List<Pitcher> players = this.getUpdatedPitchersOfTeam(team);
        return this.paginationHandlerService.paginateAndMap(players, page, size, this.pitcherMapper::toPitcherSummaryDTO);
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
    public PositionPlayerDTO createPositionPlayer(CreatePositionPlayerRequest request) {
        Team team = this.playerCreationValidations(request.name(), request.teamName());
        PositionPlayer newPlayer = new PositionPlayer(request.name(), request.playerNumber(), team, request.position());
        team.addPositionPlayer(newPlayer);
        this.playerRepository.save(newPlayer);
        return this.positionPlayerMapper.toPositionPlayerDTO(newPlayer);
    }

    /**
     * @implNote Creating a player means that the player has been promoted to the MLB team.
     * At creation time, the team must not be null; however, once the player has played in the MLB,
     * the team may be null, which implies that the player has been sent down to the minors or designated for assignment (DFA).
     */
    @Transactional
    public PitcherDTO createPitcher(CreatePitcherRequest request) {
        Team team = this.playerCreationValidations(request.name(), request.teamName());
        Pitcher newPlayer = new Pitcher(request.name(), request.playerNumber(), team, request.position());
        team.addPitcher(newPlayer);
        this.playerRepository.save(newPlayer);
        return this.pitcherMapper.toPitcherDTO(newPlayer);
    }

    @Transactional
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
        this.updateTeamIfNeeded(player, request.teamName());

        this.playerRepository.save(player);
    }

    @Transactional
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
        this.updateTeamIfNeeded(player, request.teamName());

        this.playerRepository.save(player);
    }

    /**
     * @implNote Deleting a player means that the player has retired from the MLB
     */
    @Transactional
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