package com.cyberscale.backend.listeners;

import com.cyberscale.backend.config.rabbitmq.RabbitMQConfig;
import com.cyberscale.backend.dto.ExecutionRequest;
import com.cyberscale.backend.services.ContainerService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class InfraListener {

    private final ContainerService containerService;

    public InfraListener(ContainerService containerService) {
        this.containerService = containerService;
    }

    @RabbitListener(queues = RabbitMQConfig.EXECUTION_QUEUE)
    public void processExecution(ExecutionRequest request) {
        System.out.println("📥 Commande reçue : " + request.command());
        containerService.executeCommand(request.containerId(), request.command());
    }
}
