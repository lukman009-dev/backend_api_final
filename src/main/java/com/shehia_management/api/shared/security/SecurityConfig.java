package com.shehia_management.api.shared.security;

import com.shehia_management.api.shared.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * NOTE: This is the ONLY CORS configuration in the application.
 * The previous separate WebConfig.addCorsMappings(...) has been removed
 * because Spring Security's CORS filter (wired here via .cors(...)) is the
 * one that actually governs security-filtered requests — a second,
 * independent WebMvcConfigurer-based CORS mapping was redundant and, since
 * it used allowedOrigins("*"), was also more permissive than intended.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // Configurable via application.properties / environment, defaults to the
    // previous localhost-only behaviour for local development.
    // e.g. app.cors.allowed-origins=https://portal.shehia.go.tz,https://admin.shehia.go.tz
    @Value("${app.cors.allowed-origins:http://localhost:*,http://127.0.0.1:*}")
    private List<String> allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()
                        // NEW: uploaded photos/documents (ID scans, proof of residence, letter
                        // supporting docs, issue photos) are served from here — kept public,
                        // like the existing generated-letter PDFs, so the admin app's plain
                        // <a href> "Open document" links keep working without extra auth wiring.
                        // Filenames are random UUIDs, so this doesn't expose a directory listing
                        // or make documents guessable.
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/api/v1/auth/login", "/api/v1/resident/register", "/api/v1/staff/register", "/api/v1/public/**").permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/staff/**").hasRole("STAFF")
                        .requestMatchers("/api/v1/resident/**").hasRole("RESIDENT")
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        configuration.setExposedHeaders(List.of("Content-Disposition"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
