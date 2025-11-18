package com.mlb.mlbportal.configuration;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.IOException;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Configuration
@Profile("test")
public class MockCloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() throws IOException {
        Uploader uploader = mock(Uploader.class);
        when(uploader.upload(any(byte[].class), any(Map.class)))
            .thenReturn(Map.of("secure_url", "http://fake.cloudinary.com/test.jpg", "public_id", "fake123"));
        when(uploader.destroy(any(String.class), any(Map.class)))
            .thenReturn(Map.of("result", "ok"));

        Cloudinary cloudinary = mock(Cloudinary.class);
        when(cloudinary.uploader()).thenReturn(uploader);
        return cloudinary;
    }
}