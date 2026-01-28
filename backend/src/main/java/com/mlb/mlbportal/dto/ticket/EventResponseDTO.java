package com.mlb.mlbportal.dto.ticket;

import com.mlb.mlbportal.models.others.PictureInfo;

import java.time.LocalDateTime;
import java.util.List;

public record EventResponseDTO(
        Long id,
        String awayTeamName,
        String homeTeamName,
        String stadiumName,
        LocalDateTime date,
        PictureInfo pictureMap,
        List<EventManagerDTO> sectors
) {}