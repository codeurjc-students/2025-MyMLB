package com.mlb.mlbportal.dto.team;

import java.time.Year;
import java.util.List;

import com.mlb.mlbportal.dto.stadium.StadiumInitDTO;

/**
 * DTO used for data inicialization of a team
 */
public record TeamInitDTO(
    String name,
    String abbreviation,
    int wins,
    int losses,
    String league,
    String division,
    String city,
    String generalInfo,
    List<Year> championships,
    StadiumInitDTO stadium
) {}