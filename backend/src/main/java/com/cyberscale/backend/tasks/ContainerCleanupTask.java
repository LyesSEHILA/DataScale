package com.cyberscale.backend.tasks;

import com.cyberscale.backend.services.ContainerService;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tâche planifiée agissant comme un "Garbage Collector" pour les conteneurs Docker.
 * Scanne périodiquement les conteneurs lancés par l'application et supprime ceux
 * qui ont dépassé la durée de vie maximale autorisée.
 */
@Component
public class ContainerCleanupTask {

    private static final Logger logger = LoggerFactory.getLogger(ContainerCleanupTask.class);
    
    @Autowired
    private DockerClient dockerClient;

    @Autowired
    private ContainerService containerService;

    private static final long MAX_AGE_SECONDS = 30L * 60;

    /**
     * Methode de nettoyage executee automatiquement par Spring.
     * Frequence : Toutes les 10 minutes.
     * Algorithme :
     * 1. Lister tous les conteneurs.
     * 2. Filtrer pour ne cibler que les images liees aux challenges.
     * 3. Calculer l'age du conteneur.
     * 4. Si l'age depasse le seuil, demander la suppression au service.
     */
    @Scheduled(fixedRate = 600000)
    public void cleanupOldContainers() {
        logger.info("Vérification des conteneurs expirés...");

        try {
            List<Container> containers = dockerClient.listContainersCmd()
                    .withShowAll(true)
                    .exec();

            long now = Instant.now().getEpochSecond();

            for (Container c : containers) {
                String imageName = c.getImage();
                if (imageName != null && (imageName.contains("cyberscale") || imageName.contains("alpine"))) {
                    if ((now - c.getCreated()) > MAX_AGE_SECONDS) {
                        logger.info("Suppression du conteneur expiré : {} ({})", c.getId(), imageName);
                    
                        containerService.stopAndRemoveContainer(c.getId());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Erreur lors du nettoyage Docker", e);
        }
    }
}