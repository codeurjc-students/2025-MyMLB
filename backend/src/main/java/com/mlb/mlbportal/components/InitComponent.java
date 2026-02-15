package com.mlb.mlbportal.components;

import com.mlb.mlbportal.services.MatchService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.mlb.mlbportal.services.DataInitializerService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Profile({"dev", "docker", "docker-prod"})
public class InitComponent {

    private final DataInitializerService dataInitializerService;
    private final MatchService matchService;

    @PostConstruct
    public void init() {
        if (this.dataInitializerService.isDBEmpty()) {
            this.dataInitializerService.init();
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void triggerNotification() {
        this.matchService.notificateMatchStart();
    }
}