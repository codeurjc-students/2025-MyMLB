package com.mlb.mlbportal.services;

import java.util.List;
import java.util.function.Function;

import com.mlb.mlbportal.dto.player.PlayerDTO;
import com.mlb.mlbportal.dto.team.TeamInfoDTO;

import com.mlb.mlbportal.models.player.Player;
import com.mlb.mlbportal.services.player.PlayerService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.mlb.mlbportal.dto.stadium.StadiumInitDTO;
import com.mlb.mlbportal.mappers.StadiumMapper;
import com.mlb.mlbportal.mappers.TeamMapper;
import com.mlb.mlbportal.repositories.StadiumRepository;
import com.mlb.mlbportal.repositories.TeamRepository;

import org.springframework.transaction.annotation.Transactional;

import com.mlb.mlbportal.models.Stadium;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.repositories.player.PitcherRepository;
import com.mlb.mlbportal.repositories.player.PositionPlayerRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SearchService {
    private final TeamRepository teamRepository;
    private final PositionPlayerRepository positionPlayerRepository;
    private final PitcherRepository pitcherRepository;
    private final StadiumRepository stadiumRepository;

    private final StadiumMapper stadiumMapper;
    private final TeamMapper teamMapper;
    private final PlayerService playerService;

    @Transactional(readOnly = true)
    @Cacheable(value = "search-stadium", key = "{#input, #page, #size}")
    public Page<StadiumInitDTO> searchStadiums(String input, int page, int size) {
        List<Stadium> stadiums = this.stadiumRepository.findByNameContainingIgnoreCase(input);
        return this.paginateAndMap(stadiums, this.stadiumMapper::toStadiumInitDTO, page, size);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "search-team", key = "{#input, #page, #size}")
    public Page<TeamInfoDTO> searchTeams(String input, int page, int size) {
        List<Team> teams = this.teamRepository.findByNameContainingIgnoreCase(input);
        return this.paginateAndMap(teams, this.teamMapper::toTeamInfoDTO, page, size);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "search-player", key = "{#type, #input, #page, #size}")
    public Page<PlayerDTO> searchPlayer(String type, String input, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        Page<? extends Player> players;
        if ("position".equalsIgnoreCase(type)) {
            players = this.positionPlayerRepository.findByNameContainingIgnoreCase(input, pageable);
        }
        else {
            players = this.pitcherRepository.findByNameContainingIgnoreCase(input, pageable);
        }
        return players.map(this.playerService::mapToDTO);
    }

    private <E, D> Page<D> paginateAndMap(List<E> entities, Function<E, D> mapper, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        int start = Math.min((int) pageable.getOffset(), entities.size());
        int end = Math.min(start + pageable.getPageSize(), entities.size());

        List<D> result = entities.subList(start, end).stream().map(mapper).toList();

        return new PageImpl<>(result, pageable, entities.size());
    }
}