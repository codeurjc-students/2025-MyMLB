package com.mlb.mlbportal.unit.player;

import com.mlb.mlbportal.models.enums.Division;
import com.mlb.mlbportal.models.enums.League;
import com.mlb.mlbportal.services.player.QueryBuilder;
import jakarta.persistence.Query;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM1_NAME;
import static com.mlb.mlbportal.utils.TestConstants.TEST_TEAM2_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class QueryBuilderTest {

    @Mock
    private Query dataQuery;

    @Mock
    private Query countQuery;

    @Test
    @DisplayName("Should build data query for position player")
    void testBuildDataQueryPositionPlayer() {
        String result = QueryBuilder.buildDataQuery("average", "PositionPlayer", true, false, false);

        assertThat(result)
                .contains("SELECT new com.mlb.mlbportal.dto.player.PlayerRankingsDTO")
                .contains("FROM PositionPlayer p")
                .contains("JOIN p.team t")
                .contains("(1.0 * p.atBats + 1.0 * p.walks) >= (t.totalGames * 3.1)")
                .contains("t.name IN :teamNames")
                .contains("ORDER BY p.average DESC");
    }

    @Test
    @DisplayName("Should build data query for pitcher")
    void testBuildDataQueryPitcherERA() {
        String result = QueryBuilder.buildDataQuery("era", "Pitcher", false, true, true);

        assertThat(result)
                .contains("FROM Pitcher p")
                .contains("p.inningsPitched >= (t.totalGames * 1.0)")
                .contains("t.league = :league")
                .contains("t.division = :division")
                .contains("ORDER BY p.era ASC");
    }

    @Test
    @DisplayName("Should build count query correctly")
    void testBuildCountQuery() {
        String result = QueryBuilder.buildCountQuery("homeRuns", "PositionPlayer", false, false, false);

        assertThat(result)
                .startsWith("SELECT count(p.id)")
                .contains("FROM PositionPlayer p")
                .contains("(p.atBats + p.walks) >= 1");
    }

    @Test
    @DisplayName("Should set query parameters correctly when all filters are active")
    void testSetQueryParams() {
        List<String> teams = List.of(TEST_TEAM1_NAME, TEST_TEAM2_NAME);
        QueryBuilder.setQueryParams(dataQuery, countQuery, teams, League.AL, Division.EAST, true, true, true);

        verify(dataQuery).setParameter("teamNames", teams);
        verify(countQuery).setParameter("teamNames", teams);
        verify(dataQuery).setParameter("league", League.AL);
        verify(dataQuery).setParameter("division", Division.EAST);
    }

    @ParameterizedTest
    @CsvSource({
            "average, position, true",
            "era, pitcher, true",
            "nonExistent, position, false"
    })
    @DisplayName("Should validate if a stat exists in the entity class")
    void testIsValidStat(String stat, String type, boolean expected) {
        assertThat(QueryBuilder.isValidStat(stat, type)).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should return correct table name or throw exception")
    void testGetTableName() {
        assertThat(QueryBuilder.getTableName("position")).isEqualTo("PositionPlayer");
        assertThat(QueryBuilder.getTableName("pitcher")).isEqualTo("Pitcher");
        assertThatThrownBy(() -> QueryBuilder.getTableName("invalid"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should return the correct list of stats for each player type")
    void testGetPlayerStats() {
        List<String> positionStats = QueryBuilder.getPlayerStats("position");
        List<String> pitcherStats = QueryBuilder.getPlayerStats("pitcher");

        assertThat(positionStats).contains("average", "homeRuns", "ops");
        assertThat(pitcherStats).contains("era", "whip", "inningsPitched");
    }
}