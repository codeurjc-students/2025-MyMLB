package com.mlb.mlbportal.configuration;

import static org.mockito.Mockito.mock;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.mlb.mlbportal.services.EmailService;

@TestConfiguration
public class DummyEmailServiceConfig  {

    @Bean
    public EmailService emailService() {
        return mock(EmailService.class);
    }
}