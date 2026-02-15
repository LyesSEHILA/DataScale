package com.cyberscale.backend.listeners;

import com.cyberscale.backend.config.rabbitmq.RabbitMQConfig;
import com.cyberscale.backend.dto.DeployDecoyRequest;
import com.cyberscale.backend.dto.ExecutionRequest;
import com.cyberscale.backend.services.ContainerService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import com.cyberscale.backend.dto.DeployDecoyRequest;
import com.cyberscale.backend.services.KubernetesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class InfraListener {

    private final ContainerService containerService;
    private final KubernetesService kubernetesService;
    private static final Logger logger = LoggerFactory.getLogger(InfraListener.class);

    public InfraListener(ContainerService containerService, KubernetesService kubernetesService) {
        this.containerService = containerService;
        this.kubernetesService = kubernetesService; 
    }

    @RabbitListener(queues = RabbitMQConfig.EXECUTION_QUEUE)
    public void processExecution(ExecutionRequest request) {
        logger.info("📥 Commande reçue : {}", request.command());
        containerService.executeCommand(request.containerId(), request.command());
    }

    @RabbitListener(queues = RabbitMQConfig.DEPLOY_QUEUE) 
    public void handleDeployRequest(DeployDecoyRequest request) {
        logger.info("Reçu demande de déploiement leurre: {}", request.decoyType());
        
        try {
            kubernetesService.deployDecoy(request.decoyType());
            logger.info("Déploiement terminé pour {}", request.decoyType());
        } catch (Exception e) {
            logger.error("Echec traitement message déploiement", e);
        }
    }
}
