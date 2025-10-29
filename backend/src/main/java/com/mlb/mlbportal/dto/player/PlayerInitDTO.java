package com.mlb.mlbportal.dto.player;

public record PlayerInitDTO(
    String name,
    String teamName,
    String position,
    String type,
    int atBats,
    int walks,
    int hits,
    int doubles,
    int triples,
    int homeRuns,
    int rbis,
    int games,
    int wins,
    int losses,
    int inningsPitched,
    int totalStrikeouts,
    int hitsAllowed,
    int runsAllowed,
    int saves,
    int saveOpportunities
) {}