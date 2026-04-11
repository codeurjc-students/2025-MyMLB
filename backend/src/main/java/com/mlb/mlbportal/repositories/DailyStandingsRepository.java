package com.mlb.mlbportal.repositories;

import com.mlb.mlbportal.models.DailyStandings;
import com.mlb.mlbportal.models.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface DailyStandingsRepository extends JpaRepository<DailyStandings, Long> {
    boolean existsByTeamAndMatchDate(Team team, LocalDate date);

    @Query("SELECT st FROM DailyStandings st WHERE st.team.name IN :teams AND st.matchDate >= :dateFrom " +
            "ORDER BY st.matchDate ASC"
    )
    List<DailyStandings> findHistoricRanking(@Param("teams")Set<String> teams, @Param("dateFrom")LocalDate dateFrom);
}