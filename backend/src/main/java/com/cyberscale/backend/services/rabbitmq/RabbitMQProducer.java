package com.cyberscale.backend.services.rabbitmq;

import com.cyberscale.backend.dto.GameEventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQProducer {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQProducer.class);

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routingkey}")
    private String routingKey;

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendGameEvent(String playerId, String action, String containerId) {
        GameEventDTO event = new GameEventDTO(playerId, action, containerId);
        
        logger.info("📤 Envoi Event RabbitMQ [Exchange: {}, Key: {}] : {}", exchange, routingKey, action);
        
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }
}