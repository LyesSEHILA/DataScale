package com.cyberscale.backend.config;

import com.cyberscale.backend.socket.TerminalWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

/**
 * Configuration des WebSockets de l'application.
 * Gère deux types de connexions :
 * - STOMP : Pour les notifications temps reel, le chat, et les mises a jour d'etat (Quiz, Examens).
 * - Raw WebSocket : Pour le terminal interactif (shell Docker) qui necessite un flux binaire/texte brut.
 */
@Configuration
@EnableWebSocket 
@EnableWebSocketMessageBroker 
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer, WebSocketConfigurer {
    
    /**
     * Configuration du Message Broker (STOMP).
     * Definit les prefixes pour le routage des messages.
     * @param config Le registre du courtier de messages.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Enregistrement des endpoints STOMP.
     * C'est le point d'entree principal pour le frontend.
     * @param registry Le registre des endpoints STOMP.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-cyberscale")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    /**
     * Enregistrement des handlers WebSocket bruts.
     * Utilise specifiquement pour le streaming du terminal.
     * @param registry Le registre des handlers.
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(terminalWebSocketHandler(), "/ws/terminal")
                .setAllowedOrigins("*"); 
    }

    /**
     * Definition du Bean pour le gestionnaire du terminal.
     * Permet a Spring d'injecter d'autres dependances (ex: DockerClient) dans le handler si besoin.
     * @return Une nouvelle instance de TerminalWebSocketHandler.
     */
    @Bean
    public TerminalWebSocketHandler terminalWebSocketHandler() {
        return new TerminalWebSocketHandler();
    }
}