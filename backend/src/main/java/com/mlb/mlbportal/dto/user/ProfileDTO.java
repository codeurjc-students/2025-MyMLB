package com.mlb.mlbportal.dto.user;

import com.mlb.mlbportal.models.others.PictureInfo;

public record ProfileDTO(String email, PictureInfo picture) { }