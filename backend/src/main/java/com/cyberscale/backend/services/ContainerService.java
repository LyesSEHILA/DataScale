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
            // Déjà stoppé, on log en DEBUG seulement
            logger.debug("Container {} already stopped", containerId);
        } catch (Exception e) {
            logger.error("Error stopping container {}", containerId, e);
        }

        try {
            dockerClient.removeContainerCmd(containerId).exec();
        } catch (Exception e) {
            logger.error("Error removing container {}", containerId, e);
        }
    }

    // --- TICKET W-02 ---

    public String executeCommand(String containerId, String command) {
        try {
            // Sonar n'aime pas split(" ") simple, mais pour un MVP c'est toléré.
            // On log l'action
            logger.info("Executing command '{}' on container {}", command, containerId);

            String[] commandArray = command.split(" ");

            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .withCmd(commandArray)
                    .exec();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            dockerClient.execStartCmd(execCreateCmdResponse.getId())
                    .exec(new ExecStartResultCallback(outputStream, null))
                    .awaitCompletion(5, TimeUnit.SECONDS);

            return outputStream.toString(StandardCharsets.UTF_8);

        } catch (Exception e) {
            // ✅ CORRECTION CRITIQUE : Plus de printStackTrace()
            logger.error("Execution failed for command '{}'", command, e);
            return "Error executing command"; // On ne renvoie plus e.getMessage() complet
        }
    }
    
    public String startChallengeEnvironment(String challengeId) {
        // Pour éviter l'avertissement "Parameter unused", on loggue l'ID
        logger.info("Starting environment for challenge {}", challengeId);
        String containerId = createContainer("cyberscale/base-challenge");
        startContainer(containerId);
        return containerId;
    }
}