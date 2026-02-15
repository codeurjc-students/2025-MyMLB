/**
 * DTO that maps complementary information of a Team, it adds the league and division.
 */

package com.mlb.mlbportal.dto.mlbapi.team;

import com.mlb.mlbportal.dto.mlbapi.match.DivisionInfo;
import com.mlb.mlbportal.dto.mlbapi.match.LeagueInfo;

public record TeamDetails(
        int id,
        String name,
        String abbreviation,
        LeagueInfo league,
        DivisionInfo division
) {}