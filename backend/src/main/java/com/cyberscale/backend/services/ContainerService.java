package com.cyberscale.backend.services;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.exception.NotModifiedException;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Service
public class ContainerService {

    private static final Logger logger = LoggerFactory.getLogger(ContainerService.class);
    private final DockerClient dockerClient;

    public ContainerService(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    // --- Méthodes existantes ---

    public String createContainer(String imageName) {
        CreateContainerResponse container = dockerClient.createContainerCmd(imageName)
                .withTty(true)
                .withStdinOpen(true)
                .exec();
        return container.getId();
    }

    public void startContainer(String containerId) {
        dockerClient.startContainerCmd(containerId).exec();
    }

    public void stopAndRemoveContainer(String containerId) {
        try {
            dockerClient.stopContainerCmd(containerId).exec();
        } catch (NotModifiedException e) {
            // Déjà stoppé, on ignore (Debug seulement pour ne pas polluer)
            logger.debug("Le conteneur {} était déjà arrêté.", containerId);
        } catch (Exception e) {
            logger.error("Erreur lors de l'arrêt du conteneur {}: {}", containerId, e.getMessage());
        }

        try {
            dockerClient.removeContainerCmd(containerId).exec();
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression du conteneur {}: {}", containerId, e.getMessage());
        }
    }

    // --- 👇 NOUVELLE MÉTHODE POUR LE TICKET W-02 👇 ---

    public String executeCommand(String containerId, String command) {
        try {
            // 1. Préparer la commande (ExecCreate)
            String[] commandArray = command.split(" ");

            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .withCmd(commandArray)
                    .exec();

            // 2. Démarrer l'exécution et capturer la sortie (ExecStart)
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            dockerClient.execStartCmd(execCreateCmdResponse.getId())
                    .exec(new ExecStartResultCallback(outputStream, null))
                    .awaitCompletion(5, TimeUnit.SECONDS);

            // 3. Retourner le résultat
            return outputStream.toString(StandardCharsets.UTF_8);

        } catch (Exception e) {
            // ✅ CORRECTION : Usage propre des logs au lieu de printStackTrace
            logger.error("Erreur d'exécution de la commande '{}' dans le conteneur {}: ", command, containerId, e);
            return "Erreur d'exécution : " + e.getMessage();
        }
    }
    
    public String startChallengeEnvironment(String challengeId) {
        // Logique simplifiée
        String containerId = createContainer("cyberscale/base-challenge");
        startContainer(containerId);
        return containerId;
    }
}