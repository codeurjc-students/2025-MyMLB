package com.mlb.mlbportal.mappers;

import java.util.List;

import org.mapstruct.Mapper;

import com.mlb.mlbportal.dto.stadium.StadiumDTO;
import com.mlb.mlbportal.models.Stadium;

@Mapper(componentModel = "spring")
public interface StadiumMapper {
    StadiumDTO toStadiumDTO (Stadium stadium);
    List<StadiumDTO> toListStadiumDTO(List<Stadium> stadiumList);
}