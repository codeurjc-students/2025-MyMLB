package com.mlb.mlbportal.services.player;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.mlb.mlbportal.dto.player.PitcherDTO;
import com.mlb.mlbportal.dto.player.PitcherSummaryDTO;
import com.mlb.mlbportal.dto.player.PlayerDTO;
import com.mlb.mlbportal.dto.player.PositionPlayerDTO;
import com.mlb.mlbportal.dto.player.PositionPlayerSummaryDTO;
import com.mlb.mlbportal.handler.notFound.PlayerNotFoundException;
import com.mlb.mlbportal.handler.notFound.TeamNotFoundException;
import com.mlb.mlbportal.mappers.player.PitcherMapper;
import com.mlb.mlbportal.mappers.player.PositionPlayerMapper;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.player.Pitcher;
import com.mlb.mlbportal.models.player.Player;
import com.mlb.mlbportal.models.player.PositionPlayer;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.repositories.player.PitcherRepository;
import com.mlb.mlbportal.repositories.player.PlayerRepository;
import com.mlb.mlbportal.repositories.player.PositionPlayerRepository;

import org.springframework.transaction.annotation.Transactional;
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

    @Transactional
    public List<PlayerDTO> getAllPlayers() {
        List<PositionPlayer> positionPlayers = this.positionPlayerRepository.findAll();
        List<Pitcher> pitcherList = this.pitcherRepository.findAll();

        this.updateAndSaveStats(positionPlayers);
        this.updateAndSaveStats(pitcherList);

        List<PositionPlayerDTO> mappedPostionPlayers = this.positionPlayerMapper
                .toListPositionPlayerDTO(positionPlayers);
        List<PitcherDTO> mappedPitchers = this.pitcherMapper.toListPitcherDTO(pitcherList);

        List<PlayerDTO> result = new ArrayList<>();
        result.addAll(mappedPostionPlayers);
        result.addAll(mappedPitchers);

        result.sort((p1, p2) -> p1.name().compareToIgnoreCase(p2.name()));
        return result;
    }

    @Transactional
    public List<PositionPlayerDTO> getAllPositionPlayers() {
        List<PositionPlayer> positionPlayers = this.positionPlayerRepository.findAll();
        this.updateAndSaveStats(positionPlayers);
        positionPlayers.sort((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()));
        return this.positionPlayerMapper.toListPositionPlayerDTO(positionPlayers);
    }

    @Transactional
    public List<PitcherDTO> getAllPitchers() {
        List<Pitcher> pitchers = this.pitcherRepository.findAll();
        this.updateAndSaveStats(pitchers);
        pitchers.sort((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()));
        return this.pitcherMapper.toListPitcherDTO(pitchers);
    }

    private <T extends Player> void updateAndSaveStats(List<T> players) {
        players.forEach(p -> {
            boolean hasChanged = PlayerServiceOperations.updatePlayerStats(p);
            if (hasChanged) {
                switch (p) {
                    case PositionPlayer positionPlayer -> this.positionPlayerRepository.save(positionPlayer);
                    case Pitcher pitcher -> this.pitcherRepository.save(pitcher);
                    default -> throw new IllegalArgumentException("Unexpected Player subtype: " + p.getClass().getName());
                }
            }
        });
    }

    @Transactional
    public PlayerDTO findPlayerByName(String name) {
        Player player = this.playerRepository.findByName(name).orElseThrow(PlayerNotFoundException::new);

        boolean hasChanged = PlayerServiceOperations.updatePlayerStats(player);
        if (hasChanged) {
            switch (player) {
                case PositionPlayer positionPlayer -> this.positionPlayerRepository.save(positionPlayer);
                case Pitcher pitcher -> this.pitcherRepository.save(pitcher);
                default -> throw new IllegalArgumentException("Unexpected Player subtype: " + player.getClass().getName());
            }
        }

        if (player instanceof PositionPlayer positionPlayer) {
            return this.positionPlayerMapper.toPositionPlayerDTO(positionPlayer);
        }
        return this.pitcherMapper.toPitcherDTO((Pitcher) player);
    }

    @Transactional
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

    @Transactional
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
        Team team = this.teamRepository.findByName(teamName).orElseThrow(TeamNotFoundException::new);
        List<PositionPlayer> players = this.getUpdatedPositionPlayersOfTeam(team);

        Pageable pageable = PageRequest.of(page, size);
        int start = Math.min((int) pageable.getOffset(), players.size());
        int end = Math.min(start + pageable.getPageSize(), players.size());

        List<PositionPlayerSummaryDTO> result = players.subList(start, end).stream()
                .map(this.positionPlayerMapper::toPositionPlayerSummaryDTO).toList();

        return new PageImpl<>(result, pageable, players.size());
    }

    @Transactional
    public Page<PitcherSummaryDTO> getAllPitchersOfATeam(String teamName, int page, int size) {
        Team team = this.teamRepository.findByName(teamName).orElseThrow(TeamNotFoundException::new);
        List<Pitcher> pitchers = this.getUpdatedPitchersOfTeam(team);

        Pageable pageable = PageRequest.of(page, size);
        int start = Math.min((int) pageable.getOffset(), pitchers.size());
        int end = Math.min(start + pageable.getPageSize(), pitchers.size());

        List<PitcherSummaryDTO> result = pitchers.subList(start, end).stream()
                .map(this.pitcherMapper::toPitcherSummaryDTO).toList();

        return new PageImpl<>(result, pageable, pitchers.size());
    }
}