package com.mlb.mlbportal.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mlb.mlbportal.dto.stadium.StadiumDTO;
import com.mlb.mlbportal.handler.notFound.StadiumNotFoundException;
import com.mlb.mlbportal.mappers.StadiumMapper;
import com.mlb.mlbportal.models.Stadium;
import com.mlb.mlbportal.repositories.StadiumRepository;

@Service
public class StadiumService {
    private final StadiumRepository stadiumRepository;
    private final StadiumMapper stadiumMapper;

    public StadiumService(StadiumRepository repo, StadiumMapper mapper) {
        this.stadiumRepository = repo;
        this.stadiumMapper = mapper;
    }

    public List<StadiumDTO> getAllStadiums() {
        return this.stadiumMapper.toListStadiumDTO(this.stadiumRepository.findAll());
    }

    public StadiumDTO findStadiumByName(String name) {
        Stadium stadium = this.stadiumRepository.findByName(name).orElseThrow(StadiumNotFoundException::new);
        return this.stadiumMapper.toStadiumDTO(stadium);
    }
}