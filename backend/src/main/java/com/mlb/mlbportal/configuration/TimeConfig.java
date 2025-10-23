package com.mlb.mlbportal.configuration;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mlb.mlbportal.models.classes.SystemTimeProvider;
import com.mlb.mlbportal.models.interfaces.TimeProvider;

@Configuration
public class TimeConfig {
    @Bean
    public TimeProvider timeProvider() {
        return new SystemTimeProvider(ZoneId.of("Europe/Madrid"));
    }

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("Europe/Madrid"));
    }
}