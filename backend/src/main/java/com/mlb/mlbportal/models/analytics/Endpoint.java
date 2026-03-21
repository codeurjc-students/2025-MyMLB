package com.mlb.mlbportal.models.analytics;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "T_Endpoint")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Endpoint {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String uri;
    private long count;

    public Endpoint(String uri, long count) {
        this.uri = uri;
        this.count = count;
    }
}