package com.mlb.mlbportal.dto.team;

import java.time.Year;
import java.util.List;

import com.mlb.mlbportal.dto.stadium.StadiumDTO;

public record TeamInfoDTO(
    TeamDTO teamDTO,
    String city,
    String generalInfo,
    List<Year> championships,
    StadiumDTO stadium
) {}