package com.mlb.mlbportal.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.mlb.mlbportal.security.jwt.JwtRequestFilter;
import com.mlb.mlbportal.security.jwt.UnauthorizedHandlerJwt;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    private final JwtRequestFilter jwtRequestFilter;
    private final UnauthorizedHandlerJwt unauthorizedHandlerJwt;
    private final SecurityUserDetails securityUserDetails;

    public WebSecurityConfig(JwtRequestFilter jwtRequestFilter, UnauthorizedHandlerJwt unauthorizedHandlerJwt,
        SecurityUserDetails securityUserDetails) {
        this.jwtRequestFilter = jwtRequestFilter;
        this.unauthorizedHandlerJwt = unauthorizedHandlerJwt;
        this.securityUserDetails = securityUserDetails;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(securityUserDetails);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    @Order(1)
    @Profile("!test")
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http.authenticationProvider(authenticationProvider());
        http
                .securityMatcher("/api/**")
                .exceptionHandling(handling -> handling.authenticationEntryPoint(unauthorizedHandlerJwt));

        http
                .authorizeHttpRequests(authorize -> authorize
                    // Authentication Endpoints
                    .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/auth/refresh").authenticated()
                    
                    // User Endpoints
                    .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")

                    // Team Endpoints
                    .requestMatchers(HttpMethod.GET, "/api/teams").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/teams/standings").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/teams/{teamName}").permitAll()

                    // Stadium Endpoints
                    .requestMatchers(HttpMethod.GET, "/api/stadiums").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/stadiums/{name}").permitAll()

                    // Matches Endpoints
                    .requestMatchers(HttpMethod.GET, "/api/matches/today").permitAll()
                    
                    // API Docs Endpoints
                    .requestMatchers("/v3/api-docs*/**").permitAll()
                    .requestMatchers("/swagger-ui.html").permitAll()
                    .requestMatchers("/swagger-ui/**").permitAll()
                
                
                    // PUBLIC ENDPOINTS:
                    .anyRequest().permitAll());

        // Disable Form login Authentication
        http.formLogin(AbstractHttpConfigurer::disable);

        // Disable CSRF protection.
        http.csrf(AbstractHttpConfigurer::disable);

        // Disable Basic Authentication.
        http.httpBasic(AbstractHttpConfigurer::disable);

        // Stateless session.
        http.sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Add JWT Token filter
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}