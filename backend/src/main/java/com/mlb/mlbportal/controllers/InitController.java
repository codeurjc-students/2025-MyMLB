package com.mlb.mlbportal.controllers;

import org.springframework.stereotype.Controller;

import com.mlb.mlbportal.services.DataInitializerService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class InitController {

    private final DataInitializerService dataInitializerService;

    @PostConstruct
    public void init() {
        this.dataInitializerService.init();   
    }
}