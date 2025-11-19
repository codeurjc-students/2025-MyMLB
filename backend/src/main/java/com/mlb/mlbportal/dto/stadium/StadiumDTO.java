package com.mlb.mlbportal.dto.stadium;

import java.util.List;

import com.mlb.mlbportal.models.others.PictureInfo;

/**
 * General DTO for Stadium
 */
public record StadiumDTO(
    String name,
    int openingDate,
    List<PictureInfo> pictures
) {}