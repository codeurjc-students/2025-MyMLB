package com.mlb.mlbportal.dto.stadium;

import java.util.List;

import com.mlb.mlbportal.models.others.PictureInfo;

public record StadiumInitDTO(
    String name,
    int openingDate,
    String teamName,
    List<PictureInfo> pictures
) {}