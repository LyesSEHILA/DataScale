package com.cyberscale.backend.services.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cyberscale.backend.dto.GameEventDTO;

@Service
public class RabbitMQProducer {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQProducer.class);

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routingkey}")
    private String routingKey;

    @Value("${app.rabbitmq.routingkey.infra}")
    private String infraRoutingKey;

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendGameEvent(String playerId, String action, String containerId) {
        GameEventDTO event = new GameEventDTO(playerId, action, containerId);
        logger.info("📤 Envoi Event RabbitMQ : {}", action);
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }

    public void sendInfraCommand(String command) {
        logger.info("💀 [Red Team] Envoi contre-attaque vers infra : {}", command);
        rabbitTemplate.convertAndSend(exchange, infraRoutingKey, command);
    }
}