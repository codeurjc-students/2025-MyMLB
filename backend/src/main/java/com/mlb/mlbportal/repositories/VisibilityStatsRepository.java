package com.mlb.mlbportal.repositories;

import com.mlb.mlbportal.models.VisibilityStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VisibilityStatsRepository extends JpaRepository<VisibilityStats, LocalDate> {

    @Query("SELECT vS FROM VisibilityStats vS WHERE (vS.date >= :dateFrom AND vS.date <= :dateTo) ORDER BY vS.date ASC")
    List<VisibilityStats> findStatsByRange(@Param("dateFrom")LocalDate dateFrom, @Param("dateTo")LocalDate dateTo);

    @Modifying(clearAutomatically = true)
    @Query(nativeQuery = true, value =
            "INSERT INTO t_visibility_stats (date, visualizations, new_users, churn_users) " +
                    "VALUES (:date, 1, 0, 0) " +
                    "ON CONFLICT (date) DO UPDATE SET visualizations = t_visibility_stats.visualizations + 1"
    )
    void increaseVisualizations(@Param("date")LocalDate date);

    @Modifying(clearAutomatically = true)
    @Query(nativeQuery = true, value =
            "INSERT INTO t_visibility_stats (date, visualizations, new_users, churn_users) " +
                    "VALUES (:date, 0, 1, 0) " +
                    "ON CONFLICT (date) DO UPDATE SET newUsers = t_visibility_stats.new_users + 1"
    )
    void increaseNewUsers(@Param("date")LocalDate date);

    @Modifying(clearAutomatically = true)
    @Query(nativeQuery = true, value =
            "INSERT INTO t_visibility_stats (date, visualizations, new_users, churn_users) " +
                    "VALUES (:date, 0, 0, 1) " +
                    "ON CONFLICT (date) DO UPDATE SET churnUsers = t_visibility_stats.churn_users + 1"
    )
    void increaseChurnUsers(@Param("date")LocalDate date);
}