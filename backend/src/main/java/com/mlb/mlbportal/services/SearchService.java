package com.mlb.mlbportal.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mlb.mlbportal.dto.stadium.StadiumInitDTO;
import com.mlb.mlbportal.mappers.StadiumMapper;
import com.mlb.mlbportal.repositories.StadiumRepository;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.repositories.player.PlayerRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SearchService {
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final StadiumRepository stadiumRepository;

    private final StadiumMapper stadiumMapper;

    public List<StadiumInitDTO> searchStadiums(String input) {
        return this.stadiumMapper.toListStadiumInitDTO(this.stadiumRepository.findByNameContainingIgnoreCase(input));
    }
}