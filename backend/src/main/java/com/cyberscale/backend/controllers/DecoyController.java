package com.cyberscale.backend.controllers;

import com.cyberscale.backend.config.rabbitmq.RabbitMQConfig;
import com.cyberscale.backend.dto.DeployDecoyRequest;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/decoy")
public class DecoyController {

    private final RabbitTemplate rabbitTemplate;

    public DecoyController(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostMapping
    public ResponseEntity<String> deployDecoy(@RequestBody DeployDecoyRequest request) {
        // On envoie le message dans l'échange, avec la clé de routage "infra.deploy"
        // Note: Assurez-vous que votre RabbitMQConfig lie bien cette clé à la queue
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME, 
                "infra.deploy", 
                request
        );
        
        return ResponseEntity.accepted().body("Déploiement du leurre " + request.decoyType() + " initié.");
    }
}