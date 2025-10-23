package com.mlb.mlbportal.configuration;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import com.mlb.mlbportal.models.classes.FixedTimeProvider;
import com.mlb.mlbportal.models.interfaces.TimeProvider;


@TestConfiguration
@Profile("test")
public class TestTimeConfig {
    @Bean
    public TimeProvider timeProvider() {
        return new FixedTimeProvider(LocalDateTime.of(2025, 10, 23, 15, 0));
    }

    @Bean
    public Clock clock() {
        return Clock.fixed(
            LocalDateTime.of(2025, 10, 23, 15, 0).atZone(ZoneId.of("Europe/Madrid")).toInstant(),
            ZoneId.of("Europe/Madrid")
        );
    }
}