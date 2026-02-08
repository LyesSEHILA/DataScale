package com.cyberscale.backend.services;

import com.cyberscale.backend.dto.builder.NodeDTO;
import com.cyberscale.backend.dto.builder.TopologyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

@Service
public class BuilderService {

    private static final Logger logger = LoggerFactory.getLogger(BuilderService.class);
    private static final String WORKSPACE_DIR = System.getProperty("java.io.tmpdir") + "/cyberscale-labs/";

    public String deployTopology(TopologyRequest topology) throws IOException, InterruptedException {
        // 1. Créer un ID unique pour CE déploiement
        String deploymentId = UUID.randomUUID().toString();
        
        // 2. Générer le YAML en utilisant cet ID unique (pour éviter les conflits de noms)
        String composeContent = generateDockerComposeYaml(topology, deploymentId);
        
        // 3. Créer le dossier
        File deployDir = new File(WORKSPACE_DIR + deploymentId);
        if (!deployDir.exists()) deployDir.mkdirs();

        // 4. Écrire le fichier
        File composeFile = new File(deployDir, "docker-compose.yml");
        try (FileWriter writer = new FileWriter(composeFile)) {
            writer.write(composeContent);
        }

        logger.info("📄 Docker Compose généré : {}", composeFile.getAbsolutePath());

        // 5. Lancer Docker Compose
        // Utilisation de "-p" (project name) pour isoler les réseaux
        ProcessBuilder pb = new ProcessBuilder("docker", "compose", "-p", "lab_" + deploymentId, "-f", composeFile.getAbsolutePath(), "up", "-d");
        pb.redirectErrorStream(true);
        Process process = pb.start();
        
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            String error = new String(process.getInputStream().readAllBytes());
            logger.error("❌ Erreur Docker Compose : {}", error);
            throw new RuntimeException("Échec du déploiement : " + error);
        }

        logger.info("✅ Architecture déployée avec succès ! ID: {}", deploymentId);
        
        // 6. Retourner le nom du conteneur Kali pour la connexion WebSocket
        return findKaliContainerName(topology, deploymentId);
    }

    // Ajout du paramètre deploymentId ici 👇
    private String generateDockerComposeYaml(TopologyRequest topology, String deploymentId) {
        StringBuilder yaml = new StringBuilder();
        yaml.append("version: '3.8'\n");
        yaml.append("services:\n");

        for (NodeDTO node : topology.nodes()) {
            // Nettoyage du nom
            String cleanLabel = node.label().toLowerCase().replaceAll("[^a-z0-9]", "");
            String serviceName = cleanLabel + "_" + node.id();
            
            yaml.append("  ").append(serviceName).append(":\n");
            
            String image = switch(node.type()) {
                case "kali" -> "kalilinux/kali-rolling"; 
                case "server" -> "httpd:alpine";         
                case "db" -> "mysql:5.7";
                default -> "alpine";
            };
            
            yaml.append("    image: ").append(image).append("\n");
            
            // CORRECTION CRITIQUE : Utilisation de deploymentId pour l'unicité
            // Ex: attaquantmoi_1_9851df97... au lieu de attaquantmoi_1_4
            String containerName = serviceName + "_" + deploymentId;
            
            yaml.append("    container_name: ").append(containerName).append("\n");
            yaml.append("    tty: true\n"); 
            
            if (node.type().equals("db")) {
                 yaml.append("    environment:\n      MYSQL_ROOT_PASSWORD: root\n");
            }
            
            yaml.append("    networks:\n      - cyber_range\n\n");
        }

        yaml.append("networks:\n  cyber_range:\n    driver: bridge\n");
        return yaml.toString();
    }

    // Ajout du paramètre deploymentId ici aussi 👇
    private String findKaliContainerName(TopologyRequest topology, String deploymentId) {
        for (NodeDTO node : topology.nodes()) {
            if ("kali".equals(node.type())) {
                String cleanLabel = node.label().toLowerCase().replaceAll("[^a-z0-9]", "");
                String serviceName = cleanLabel + "_" + node.id();
                // On doit reconstruire le même nom que celui généré dans le YAML
                return serviceName + "_" + deploymentId;
            }
        }
        // Fallback
        if (!topology.nodes().isEmpty()) {
            NodeDTO node = topology.nodes().get(0);
            String cleanLabel = node.label().toLowerCase().replaceAll("[^a-z0-9]", "");
            return cleanLabel + "_" + node.id() + "_" + deploymentId;
        }
        return null;
    }
}