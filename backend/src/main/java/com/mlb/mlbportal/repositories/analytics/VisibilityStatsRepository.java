package com.mlb.mlbportal.repositories.analytics;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mlb.mlbportal.models.analytics.VisibilityStats;

@Repository
public interface VisibilityStatsRepository extends JpaRepository<VisibilityStats, LocalDate> {

    @Query("SELECT v FROM VisibilityStats v WHERE (v.date BETWEEN :dateFrom AND :dateTo) ORDER BY v.date ASC")
    List<VisibilityStats> findStatsByRange(@Param("dateFrom")LocalDate dateFrom, @Param("dateTo")LocalDate dateTo);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE VisibilityStats v SET v.visualizations = v.visualizations + 1 WHERE v.date = :date")
    int increaseVisualizations(@Param("date") LocalDate date);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE VisibilityStats v SET v.newUsers = v.newUsers + 1 WHERE v.date = :date")
    int increaseNewUsers(@Param("date") LocalDate date);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE VisibilityStats v SET v.deletedUsers = v.deletedUsers + 1 WHERE v.date = :date")
    int increaseDeletedUsers(@Param("date") LocalDate date);
}