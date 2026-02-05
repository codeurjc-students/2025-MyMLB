package com.mlb.mlbportal.configuration;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class TestTimeConfig {
    @Bean
    @Primary
    public Clock clock() {
        return Clock.fixed(
            LocalDateTime.of(2025, 10, 23, 15, 0).atZone(ZoneId.of("Europe/Madrid")).toInstant(),
            ZoneId.of("Europe/Madrid")
        );
    }
}