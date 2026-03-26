/**
 * DTO that maps team stats from the API.
 */

package com.mlb.mlbportal.dto.mlbapi.team;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TeamRecords(
        TeamRecordsGeneralInfo team,
        Integer gamesPlayed,
        Integer wins,
        Integer losses,
        String winningPercentage,
        String divisionGamesBack,
        RecordsWrapper records
) {
    public String getLastTenGames() {
        if (records == null || records.splitRecords() == null) {
            return "0-0";
        }
        return records.splitRecords().stream().filter(r -> "lastTen".equals(r.type()))
                .findFirst().map(sR -> sR.wins() + "-" + sR.losses()).orElse("0-0");
    }
}