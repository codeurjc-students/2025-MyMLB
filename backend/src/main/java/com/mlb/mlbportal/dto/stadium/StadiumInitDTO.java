package com.mlb.mlbportal.dto.stadium;

import java.util.List;

public record StadiumInitDTO(
    String name,
    int openingDate,
    String teamName,
    List<String> pictures
) {}