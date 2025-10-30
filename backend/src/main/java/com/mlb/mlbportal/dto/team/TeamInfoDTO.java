package com.mlb.mlbportal.dto.team;

import java.time.Year;
import java.util.List;

import com.mlb.mlbportal.dto.player.PitcherSummaryDTO;
import com.mlb.mlbportal.dto.player.PositionPlayerSummaryDTO;
import com.mlb.mlbportal.dto.stadium.StadiumDTO;

/**
 * DTO that contains all the relevant information of a Team. Used in the individual team page
 */
public record TeamInfoDTO(
    TeamDTO teamDTO,
    String city,
    String generalInfo,
    List<Year> championships,
    StadiumDTO stadium,
    List<PositionPlayerSummaryDTO> positionPlayers,
    List<PitcherSummaryDTO> pitchers
) {}