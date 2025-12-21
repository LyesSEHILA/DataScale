package com.cyberscale.backend.tasks;

import com.cyberscale.backend.services.ContainerService;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class ContainerCleanupTask {

    @Autowired
    private DockerClient dockerClient;

    @Autowired
    private ContainerService containerService;

    // Seuil : 30 minutes (en secondes)
    private static final long MAX_AGE_SECONDS = 30 * 60;

    // S'exécute toutes les 600 000 ms (10 minutes)
    @Scheduled(fixedRate = 600000)
    public void cleanupOldContainers() {
        System.out.println("🧹 [Cleanup] Vérification des conteneurs expirés...");

        try {
            // 1. Lister tous les conteneurs (actifs ou arrêtés)
            List<Container> containers = dockerClient.listContainersCmd()
                    .withShowAll(true)
                    .exec();

            long now = Instant.now().getEpochSecond();

            for (Container c : containers) {
                // 2. Filtrer pour ne pas supprimer n'importe quoi !
                // On ne touche qu'aux images de nos challenges (alpine, cyberscale...)
                String imageName = c.getImage();
                if (imageName != null && (imageName.contains("cyberscale") || imageName.contains("alpine"))) {

                    // 3. Vérifier l'âge
                    // c.getCreated() retourne un timestamp en secondes
                    if ((now - c.getCreated()) > MAX_AGE_SECONDS) {
                        System.out.println("🗑️ Suppression du conteneur expiré : " + c.getId() + " (" + imageName + ")");
                        
                        // Appel à votre service existant pour nettoyer proprement
                        containerService.stopAndRemoveContainer(c.getId());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Erreur lors du nettoyage Docker : " + e.getMessage());
        }
    }
}