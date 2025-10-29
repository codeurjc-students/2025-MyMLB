package com.mlb.mlbportal.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mlb.mlbportal.dto.stadium.StadiumInitDTO;
import com.mlb.mlbportal.handler.notFound.StadiumNotFoundException;
import com.mlb.mlbportal.mappers.StadiumMapper;
import com.mlb.mlbportal.models.Stadium;
import com.mlb.mlbportal.repositories.StadiumRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class StadiumService {
    private final StadiumRepository stadiumRepository;
    private final StadiumMapper stadiumMapper;

    public List<StadiumInitDTO> getAllStadiums() {
        return this.stadiumMapper.toListStadiumInitDTO(this.stadiumRepository.findAll());
    }

    public StadiumInitDTO findStadiumByName(String name) {
        Stadium stadium = this.stadiumRepository.findByName(name).orElseThrow(StadiumNotFoundException::new);
        return this.stadiumMapper.toStadiumInitDTO(stadium);
    }
}