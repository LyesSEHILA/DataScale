package com.cyberscale.backend.config;

import com.cyberscale.backend.socket.TerminalWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket // Active les WebSockets "Bruts" (pour le Terminal)
@EnableWebSocketMessageBroker // Active STOMP (pour le futur Chat/Duel)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer, WebSocketConfigurer {

    // --- 1. Configuration STOMP (Messagerie classique) ---
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-cyberscale")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    // --- 2. Configuration RAW (Pour le Terminal Docker) ---
    // C'est cette partie qui manquait et causait l'erreur 1006 !
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(terminalWebSocketHandler(), "/ws/terminal")
                .setAllowedOrigins("*"); // Important pour autoriser la connexion
    }

    // On déclare le Handler comme un Bean pour que les @Autowired (DockerClient) fonctionnent dedans
    @Bean
    public TerminalWebSocketHandler terminalWebSocketHandler() {
        return new TerminalWebSocketHandler();
    }
}