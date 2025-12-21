package com.cyberscale.backend.services;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import org.springframework.stereotype.Service;

@Service
public class ContainerService {

    private final DockerClient dockerClient;

    // Injection DockerClient
    public ContainerService(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    /**
     * Instancie un conteneur à partir d'une image.
     * @param imageId L'id ou le nom de l'image
     * @return L'id du conteneur créé.
     */
    public String createContainer(String imageId) {
        try {
            CreateContainerResponse container = dockerClient.createContainerCmd(imageId)
                    .withTty(true) 
                    .exec();

            return container.getId();
        } catch (DockerException e) {
            throw new RuntimeException("Erreur lors de la création du conteneur : " + e.getMessage(), e);
        }
    }

    /**
     * Lance un conteneur existant.
     * @param containerId L'id du conteneur retourné par createContainer.
     */
    public void startContainer(String containerId) {
        try {
            dockerClient.startContainerCmd(containerId).exec();
        } catch (DockerException e) {
            throw new RuntimeException("Erreur lors du démarrage du conteneur : " + e.getMessage(), e);
        }
    }

    /**
     * Arrête et supprime le conteneur.
     * @param containerId L'id du conteneur à nettoyer.
     */
    public void stopAndRemoveContainer(String containerId) {
        try {
            dockerClient.stopContainerCmd(containerId).exec();
            
            dockerClient.removeContainerCmd(containerId).exec();
        } catch (DockerException e) {
            System.err.println("Erreur lors du nettoyage du conteneur " + containerId + ": " + e.getMessage());
        }
    }
}