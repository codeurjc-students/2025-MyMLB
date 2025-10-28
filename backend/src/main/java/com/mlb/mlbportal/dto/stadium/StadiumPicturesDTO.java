package com.mlb.mlbportal.dto.stadium;

import java.util.List;

/*
 * DTO that displays the list of pictures of the stadium
 */
public record StadiumPicturesDTO(
    String name,
    List<String> pictures
) {}