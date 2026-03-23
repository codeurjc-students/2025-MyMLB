package com.mlb.mlbportal.repositories.analytics;

import com.mlb.mlbportal.models.analytics.APIPerformance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface APIPerformanceRepository extends JpaRepository<APIPerformance, Long> {
    List<APIPerformance> findAllByTimeStampAfterOrderByTimeStampAsc(LocalDateTime timeStamp);
}