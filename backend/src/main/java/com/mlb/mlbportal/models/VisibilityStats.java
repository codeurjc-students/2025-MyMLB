package com.mlb.mlbportal.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "T_Visibility_Stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VisibilityStats {
    @Id
    private LocalDate date;

    private long visualizations = 0;
    private long newUsers = 0;
    private long churnUsers = 0;
}