package com.mlb.mlbportal.services.utilities;

import com.mlb.mlbportal.models.ticket.Seat;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatBatchGenerationService {
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void batchGeneration(List<Seat> seats, Long sectorId) {
        String query = "INSERT INTO T_SEAT (name, is_occupied, sector_id) VALUES (?, ?, ?)";
        this.jdbcTemplate.batchUpdate(query, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Seat seat = seats.get(i);
                ps.setString(1, seat.getName());
                ps.setBoolean(2, seat.isOccupied());
                ps.setLong(3, sectorId);
            }

            @Override
            public int getBatchSize() {
                return seats.size();
            }
        });
    }
}