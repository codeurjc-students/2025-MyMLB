package com.mlb.mlbportal.repositories.ticket;

import com.mlb.mlbportal.models.ticket.Sector;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SectorRepository extends JpaRepository<Sector, Long> {
    List<Sector> findByStadiumId(long stadiumId);
}