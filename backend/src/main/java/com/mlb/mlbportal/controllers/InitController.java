package com.mlb.mlbportal.controllers;

import org.springframework.stereotype.Component;

import com.mlb.mlbportal.services.DataInitializerService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InitController {

    private final DataInitializerService dataInitializer;

    @PostConstruct
    public void init() {
        this.dataInitializer.init();
    }
}