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

    // --- Méthodes de base ---

    public String createContainer(String imageName) {
        CreateContainerResponse container = dockerClient.createContainerCmd(imageName)
                .withTty(true)
                .withStdinOpen(true)
                .exec();
        return container.getId();
    }

    public String createChallengeContainer(String challengeId, String dynamicFlag) {
        logger.info("Creating container for challenge {} with dynamic flag", challengeId);
        
        String imageName = "cyberscale/base-challenge"; 

        CreateContainerResponse container = dockerClient.createContainerCmd(imageName)
                .withTty(true)
                .withStdinOpen(true)
                .withEnv("CHALLENGE_FLAG=" + dynamicFlag) 
                .exec();
        
        String containerId = container.getId();
        startContainer(containerId);
        
        return containerId;
    }

    public void startContainer(String containerId) {
        dockerClient.startContainerCmd(containerId).exec();
    }

    public void stopAndRemoveContainer(String containerId) {
        try {
            dockerClient.stopContainerCmd(containerId).exec();
        } catch (NotModifiedException e) {
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

    public String executeCommand(String containerId, String command) {
        // Sécurité basique (Optionnel, selon tes besoins)
        if (isCommandDangerous(command)) {
            return "Command blocked for security reasons.";
        }

        try {
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
            logger.error("Execution failed for command '{}'", command, e);
            return "Error executing command";
        }
    }
    
    public String startChallengeEnvironment(String challengeId) {
        return createChallengeContainer(challengeId, "DEFAULT_FLAG");
    }

    // ✅ La méthode est ici, correctement placée à la fin de la classe
    private boolean isCommandDangerous(String command) {
        // Exemple simple de blocage
        return command.contains("rm -rf /") || command.contains(":(){:|:&};:");
    }
}