package com.mlb.mlbportal.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.mlb.mlbportal.dto.stadium.StadiumDTO;
import com.mlb.mlbportal.dto.stadium.StadiumInitDTO;
import com.mlb.mlbportal.models.Stadium;

@Mapper(componentModel = "spring")
public interface StadiumMapper {
    StadiumDTO toStadiumDTO (Stadium stadium);
    List<StadiumDTO> toListStadiumDTO(List<Stadium> stadiumList);
    
    @Mapping(target = "teamName", source = "team.name")
    StadiumInitDTO toStadiumInitDTO(Stadium stadium);

    List<StadiumInitDTO> toListStadiumInitDTO(List<Stadium> stadiumList);
}