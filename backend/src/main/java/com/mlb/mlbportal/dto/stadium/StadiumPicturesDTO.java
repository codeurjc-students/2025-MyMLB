package com.mlb.mlbportal.dto.stadium;

import java.util.List;

public record StadiumPicturesDTO(
    String name,
    List<String> pictures
) {}