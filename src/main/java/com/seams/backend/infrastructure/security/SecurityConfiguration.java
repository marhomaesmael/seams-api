package com.seams.backend.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final RateLimitFilter rateLimitFilter;
    private final AuthenticationProvider authenticationProvider;

    @Value("${application.security.cors.allowed-origins}")
    private List<String> allowedOrigins;

    public SecurityConfiguration(JwtAuthenticationFilter jwtAuthFilter, RateLimitFilter rateLimitFilter, AuthenticationProvider authenticationProvider) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.rateLimitFilter = rateLimitFilter;
        this.authenticationProvider = authenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(req ->
                        req.requestMatchers(
                                        "/api/v1/auth/login",
                                        "/api/v1/auth/problem-request",
                                        "/api/v1/auth/problem-request/track/**",
                                        "/api/v1/auth/reset-password",
                                        "/api/v1/auth/discovery/**",
                                        "/api/v1/check/health"
                                ).permitAll()
                                .requestMatchers("/api/v1/sync/pair").permitAll()
                                .requestMatchers("/api/v1/sync/pairing-status/**").permitAll()
                                .requestMatchers("/api/v1/sync/attendance").permitAll()
                                .requestMatchers("/api/v1/sync/attendance/cancel").permitAll()
                                .requestMatchers("/api/v1/sync/attendance/status").permitAll()
                                .requestMatchers("/api/v1/sync/approve/**", "/api/v1/sync/reject/**", "/api/v1/sync/terminate/**").hasAuthority("SUPERVISOR")
                                .requestMatchers("/api/v1/sync/**").hasAnyAuthority("ADMIN", "SUPERVISOR")
                                .requestMatchers("/api/v1/admin/**").hasAnyAuthority("ADMIN", "SUPERVISOR")
                                .requestMatchers("/api/v1/supervisor/**").hasAuthority("SUPERVISOR")
                                .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
