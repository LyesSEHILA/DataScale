package com.cyberscale.backend.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configuration de la securite Spring Security.
 * Definit :
 * - Les regles de filtrage HTTP (qui a le droit d'acceder a quoi).
 * - La configuration CORS (Cross-Origin Resource Sharing).
 * - L'algorithme de hachage des mots de passe.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Definit la chaine de filtres de securite.
     * Actions effectuees :
     * - Désactivation CSRF : Inutile pour une API REST stateless utilisee par un front SPA.
     * - Activation CORS : Utilise la source de configuration definie plus bas.
     * - Autorisations : Permet l'acces anonyme aux endpoints publics, exige l'authentification pour le reste.
     * - Headers : Desactive X-Frame-Options pour permettre l'affichage de la console H2.
     *
     * @param http Le constructeur de securite HTTP.
     * @return La chaine de filtres construite.
     * @throws Exception En cas d'erreur de configuration.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/ws-cyberscale/**").permitAll()
                .requestMatchers("/ws/terminal/**").permitAll()
                .anyRequest().authenticated()
            )
            .headers(headers -> headers.frameOptions(frame -> frame.disable()));
        
        return http.build();
    }

    /**
     * Responsable de l'encodage des mots de passe.
     * Utilise BCrypt, algorithme pour le hachage.
     * @return Une instance de BCryptPasswordEncoder.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configuration globale du CORS.
     * Permet au frontend de communiquer avec ce backend.
     * @return La source de configuration CORS.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}