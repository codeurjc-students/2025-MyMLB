package com.mlb.mlbportal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(exclude = {ErrorMvcAutoConfiguration.class})
@EnableSpringDataWebSupport(
    pageSerializationMode= EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO
)
@EnableAsync
public class MlbPortalApplication {
    public static void main(String[] args) {
        SpringApplication.run(MlbPortalApplication.class, args);
    }
}