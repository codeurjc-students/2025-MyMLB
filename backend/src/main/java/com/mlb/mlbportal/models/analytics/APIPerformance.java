package com.mlb.mlbportal.models.analytics;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

@Entity
@Table(name = "T_API_Performance_Analytics")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class APIPerformance {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private LocalDateTime timeStamp;
    private long totalRequests;
    private long totalErrors;
    private long totalSuccesses;
    private double averageResponseTime;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "performance_id")
    private List<Endpoint> mostDemandedEndpoints = new LinkedList<>();

    @PrePersist
    protected void onCreate() {
        this.timeStamp = LocalDateTime.now();
    }

    public APIPerformance(LocalDateTime timeStamp, Long requests, Long errors, Long successes, Double avg, List<Endpoint> endpoints) {
        this.timeStamp = timeStamp;
        this.totalRequests = requests;
        this.totalErrors = errors;
        this.totalSuccesses = successes;
        this.mostDemandedEndpoints = endpoints;
    }
}