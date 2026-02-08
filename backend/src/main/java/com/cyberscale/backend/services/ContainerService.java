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
import java.util.List;
import java.util.Arrays;


@Service
public class ContainerService {

    private static final Logger logger = LoggerFactory.getLogger(ContainerService.class);
    private final DockerClient dockerClient;

    private final List<String> FORBIDDEN_PATTERNS = Arrays.asList(
        "rm -rf /", 
        ":(){ :|:& };:", // Fork bomb
        "mkfs", 
        "dd if=/dev/zero"
    );

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

    // --- TICKET: EXÉCUTION SÉCURISÉE (BRAS ARMÉ) ---

    /**
     * Exécute une commande shell à l'intérieur d'un conteneur spécifique.
     * @param containerId L'ID du conteneur cible.
     * @param command La commande à exécuter (ex: "ls -la").
     * @return Le résultat (texte) de la commande ou un message d'erreur.
     */
    public String executeCommand(String containerId, String command) {
        // 1. SÉCURITÉ : On vérifie si la commande est dangereuse avant de parler à Docker
        if (isCommandDangerous(command)) {
            logger.warn("⚠️ ALERTE SÉCURITÉ : Commande bloquée -> {}", command);
            return "ERREUR : Commande interdite par la politique de sécurité.";
        }

        try {
            logger.info("Exécution commande sur {}: {}", containerId, command);

            // 2. PRÉPARATION (ExecCreate)
            // On utilise "sh -c" pour que Linux interprète correctement les espaces et arguments
            ExecCreateCmdResponse execResponse = dockerClient.execCreateCmd(containerId)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .withCmd("sh", "-c", command) 
                    .exec();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            // 3. LANCEMENT (ExecStart)
            // On attend jusqu'à 10 secondes que la commande finisse
            dockerClient.execStartCmd(execResponse.getId())
                    .exec(new ExecStartResultCallback(outputStream, outputStream))
                    .awaitCompletion(10, TimeUnit.SECONDS);

            // On convertit le flux de données (bytes) en texte lisible (String)
            return outputStream.toString(StandardCharsets.UTF_8);

        } catch (Exception e) {
            logger.error("Erreur d'exécution pour la commande: " + command, e);
            return "ERREUR D'EXÉCUTION : " + e.getMessage();
        }
    }

    /**
     * Vérifie si la commande contient des mots-clés interdits (Blacklist).
     */
    private boolean isCommandDangerous(String command) {
        if (command == null || command.trim().isEmpty()) return true;

        // Liste des commandes destructrices à bloquer
        List<String> blacklist = List.of(
            "rm -rf",           // Suppression récursive
            "mkfs",             // Formatage disque
            ":(){ :|:& };:",    // Fork bomb (crash serveur)
            "> /dev/sd",        // Écriture directe sur disque
            "shutdown",         // Extinction
            "reboot"            // Redémarrage
        );

        // Si la commande contient l'un de ces termes, on renvoie "true" (C'est dangereux !)
        return blacklist.stream().anyMatch(command::contains);
    }
    
    public String startChallengeEnvironment(String challengeId) {
        // Pour éviter l'avertissement "Parameter unused", on loggue l'ID
        logger.info("Starting environment for challenge {}", challengeId);
        String containerId = createContainer("cyberscale/base-challenge");
        startContainer(containerId);
        return containerId;
    }
}