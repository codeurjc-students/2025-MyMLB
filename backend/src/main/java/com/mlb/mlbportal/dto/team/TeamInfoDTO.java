package com.mlb.mlbportal.dto.team;

import java.util.List;

import com.mlb.mlbportal.dto.player.pitcher.PitcherSummaryDTO;
import com.mlb.mlbportal.dto.player.position.PositionPlayerSummaryDTO;
import com.mlb.mlbportal.dto.stadium.StadiumDTO;

/**
 * DTO that contains all the relevant information of a Team. Used in the individual team page
 */
public record TeamInfoDTO(
    TeamDTO teamStats,
    String city,
    String generalInfo,
    List<Integer> championships,
    StadiumDTO stadium,
    List<PositionPlayerSummaryDTO> positionPlayers,
    List<PitcherSummaryDTO> pitchers
) {}