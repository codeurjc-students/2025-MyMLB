/**
 * DTO that maps team stats from the API.
 */

package com.mlb.mlbportal.dto.mlbapi.team;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TeamRecords(
        TeamRecordsGeneralInfo team,
        String divisionRank,
        Integer gamesPlayed,
        Integer wins,
        Integer losses,
        String winningPercentage,
        String divisionGamesBack,
        Integer runsScored,
        Integer runsAllowed,
        Integer runDifferential,
        RecordsWrapper records
) {
    public Optional<SplitRecords> getHomeSplit() {
        if (records == null || records.splitRecords() == null) {
            return Optional.empty();
        }
        return records.splitRecords().stream().filter(r -> "home".equals(r.type())).findFirst();
    }

    public Optional<SplitRecords> getAwaySplit() {
        if (records == null || records.splitRecords() == null) {
            return Optional.empty();
        }
        return records.splitRecords().stream().filter(r -> "away".equals(r.type())).findFirst();
    }

    public String getLastTenGames() {
        if (records == null || records.splitRecords() == null) {
            return "0-0";
        }
        return records.splitRecords().stream().filter(r -> "lastTen".equals(r.type()))
                .findFirst().map(sR -> sR.wins() + "-" + sR.losses()).orElse("0-0");
    }
}