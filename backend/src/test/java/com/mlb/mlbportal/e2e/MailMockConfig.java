package com.mlb.mlbportal.e2e;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;

@TestConfiguration
public class MailMockConfig {
    @Bean
    @Primary
    public JavaMailSender mailSender() {
        return Mockito.mock(JavaMailSender.class);
    }
}