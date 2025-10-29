package com.mlb.mlbportal.services.player;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.mlb.mlbportal.dto.player.PitcherDTO;
import com.mlb.mlbportal.dto.player.PlayerDTO;
import com.mlb.mlbportal.dto.player.PositionPlayerDTO;
import com.mlb.mlbportal.handler.notFound.PlayerNotFoundException;
import com.mlb.mlbportal.mappers.player.PitcherMapper;
import com.mlb.mlbportal.mappers.player.PositionPlayerMapper;
import com.mlb.mlbportal.models.player.Pitcher;
import com.mlb.mlbportal.models.player.Player;
import com.mlb.mlbportal.models.player.PositionPlayer;
import com.mlb.mlbportal.repositories.player.PitcherRepository;
import com.mlb.mlbportal.repositories.player.PlayerRespository;
import com.mlb.mlbportal.repositories.player.PositionPlayerRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PlayerService {
    private final PlayerRespository playerRepository;
    private final PositionPlayerRepository positionPlayerRepository;
    private final PitcherRepository pitcherRepository;

    private final PositionPlayerMapper positionPlayerMapper;
    private final PitcherMapper pitcherMapper;

    public List<PlayerDTO> getAllPlayers() {
        List<PositionPlayer> positionPlayers = this.positionPlayerRepository.findAll();
        List<Pitcher> pitcherList = this.pitcherRepository.findAll();

        positionPlayers.forEach(p -> PlayerServiceOperations.updatePlayerStats(p, positionPlayerRepository, pitcherRepository));
        pitcherList.forEach(p -> PlayerServiceOperations.updatePlayerStats(p, positionPlayerRepository, pitcherRepository));

        List<PositionPlayerDTO> mappedPostionPlayers = this.positionPlayerMapper
                .toListPositionPlayerDTO(positionPlayers);
        List<PitcherDTO> mappedPitchers = this.pitcherMapper.toListPitcherDTO(pitcherList);

        List<PlayerDTO> result = new ArrayList<>();
        result.addAll(mappedPostionPlayers);
        result.addAll(mappedPitchers);

        result.sort((p1, p2) -> p1.name().compareToIgnoreCase(p2.name()));
        return result;
    }

    public List<PositionPlayerDTO> getAllPositionPlayers() {
        List<PositionPlayer> positionPlayers = this.positionPlayerRepository.findAll();
        this.updateAndSortByName(positionPlayers);
        return this.positionPlayerMapper.toListPositionPlayerDTO(positionPlayers);
    }

    public List<PitcherDTO> getAllPitchers() {
        List<Pitcher> pitchers = this.pitcherRepository.findAll();
        this.updateAndSortByName(pitchers);
        return this.pitcherMapper.toListPitcherDTO(pitchers);
    }

    private <T extends Player> void updateAndSortByName(List<T> players) {
        players.forEach(p -> PlayerServiceOperations.updatePlayerStats(p, positionPlayerRepository, pitcherRepository));
        players.sort((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()));
    }

    public PlayerDTO findPlayerByName(String name) {
        Player player = this.playerRepository.findByName(name).orElseThrow(PlayerNotFoundException::new);
        PlayerServiceOperations.updatePlayerStats(player, positionPlayerRepository, pitcherRepository);
        if (player instanceof PositionPlayer positionPlayer) {
            return this.positionPlayerMapper.toPositionPlayerDTO(positionPlayer);
        }
        return this.pitcherMapper.toPitcherDTO((Pitcher) player);
    }
}