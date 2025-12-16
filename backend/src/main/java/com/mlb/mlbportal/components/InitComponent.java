package com.mlb.mlbportal.components;

import org.springframework.stereotype.Component;

import com.mlb.mlbportal.services.DataInitializerService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InitComponent {

    private final DataInitializerService dataInitializerService;

    @PostConstruct
    public void init() {
        this.dataInitializerService.init();   
    }
}