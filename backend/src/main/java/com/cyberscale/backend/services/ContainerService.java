package com.cyberscale.backend.services;

import org.springframework.stereotype.Service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.exception.DockerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service gérant le cycle de vie des conteneurs Docker pour les challenges.
 * Il permet de créer, démarrer et nettoyer des environnements isolés.
 */
@Service
public class ContainerService {

    private static final Logger logger = LoggerFactory.getLogger(ContainerService.class);

    private final DockerClient dockerClient;

    // Injection DockerClient
    public ContainerService(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    /**
     * Instancie un conteneur Docker à partir d'une image donnée.
     * @param imageId L'identifiant ou le nom de l'image Docker.
     * @return L'identifiant unique du conteneur créé.
     * @throws RuntimeException Si l'API Docker renvoie une erreur lors de la création.
     */
    public String createContainer(String imageId) {
        try {
            CreateContainerResponse container = dockerClient.createContainerCmd(imageId)
                    .withTty(true)
                    .withStdinOpen(true)
                    .exec();

            return container.getId();
        } catch (DockerException e) {
            throw new RuntimeException("Erreur lors de la création du conteneur : " + e.getMessage(), e);
        }
    }

    /**
     * Démarre un conteneur existant qui a été créé précédemment.
     * @param containerId L'identifiant du conteneur à démarrer.
     * @throws RuntimeException Si le démarrage échoue.
     */
    public void startContainer(String containerId) {
        try {
            dockerClient.startContainerCmd(containerId).exec();
        } catch (DockerException e) {
            throw new RuntimeException("Erreur lors du démarrage du conteneur : " + e.getMessage(), e);
        }
    }

    /**
     * Arrête et supprime définitivement un conteneur.
     * @param containerId L'identifiant du conteneur à nettoyer.
     */
    public void stopAndRemoveContainer(String containerId) {
        try {
            dockerClient.stopContainerCmd(containerId).exec();
            
            dockerClient.removeContainerCmd(containerId).exec();
        } catch (DockerException e) {
            logger.error("Erreur lors du nettoyage du conteneur {}", containerId, e);
        }
    }
}