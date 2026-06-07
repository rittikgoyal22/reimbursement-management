package com.etd.reimbursement_management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs", "/v3/api-docs/**", "/v3/api-docs.yaml").permitAll()
                        // Reimbursement types — reference data, all authenticated users
                        .requestMatchers(HttpMethod.GET, "/api/reimbursements/types").authenticated()
                        // Add reimbursement — Employee only (only the employee who raised the travel request)
                        .requestMatchers(HttpMethod.POST, "/api/reimbursements/add").hasAuthority("Employee")
                        // Process (approve/reject) — TravelDeskExe only
                        .requestMatchers(HttpMethod.PUT, "/api/reimbursements/*/process").hasAuthority("TravelDeskExe")
                        // View all reimbursements for a travel request — Employee and TravelDeskExe
                        .requestMatchers(HttpMethod.GET, "/api/reimbursements/*/requests").authenticated()
                        // View single reimbursement — Employee and TravelDeskExe
                        .requestMatchers(HttpMethod.GET, "/api/reimbursements/*").authenticated()
                        .anyRequest().authenticated())
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
