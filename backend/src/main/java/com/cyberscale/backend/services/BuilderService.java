package com.cyberscale.backend.services;

import com.cyberscale.backend.dto.builder.NodeDTO;
import com.cyberscale.backend.dto.builder.TopologyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class BuilderService {

    private static final Logger logger = LoggerFactory.getLogger(BuilderService.class);
    private static final String WORKSPACE_DIR = System.getProperty("java.io.tmpdir") + "/cyberscale-labs/";

    public String deployTopology(TopologyRequest topology) throws IOException, InterruptedException {
        String deploymentId = UUID.randomUUID().toString();
        String composeContent = generateDockerComposeYaml(topology, deploymentId);
        
        File deployDir = new File(WORKSPACE_DIR + deploymentId);
        if (!deployDir.exists() && !deployDir.mkdirs()) {
            throw new IOException("Impossible de créer le dossier de travail : " + deployDir.getAbsolutePath());
        }

        File composeFile = new File(deployDir, "docker-compose.yml");
        try (FileWriter writer = new FileWriter(composeFile)) {
            writer.write(composeContent);
        }

        logger.info("📄 Docker Compose généré : {}", composeFile.getAbsolutePath());

        // Appel de la méthode protégée (qu'on pourra surcharger dans les tests)
        executeDockerCompose(deploymentId, composeFile.getAbsolutePath());

        logger.info("✅ Architecture déployée avec succès ! ID: {}", deploymentId);
        return findKaliContainerName(topology, deploymentId);
    }

    /**
     * Méthode extraite pour :
     * 1. Isoler la logique ProcessBuilder (pour les tests)
     * 2. Corriger la faille de sécurité SonarQube sur le PATH
     */
    protected void executeDockerCompose(String deploymentId, String composeFilePath) throws IOException, InterruptedException {
        // Utilisation du chemin absolu (Recommandation Sonar)
        // Note: Sur Alpine/Linux c'est souvent /usr/bin/docker ou /usr/local/bin/docker
        ProcessBuilder pb = new ProcessBuilder("docker", "compose", "-p", "lab_" + deploymentId, "-f", composeFilePath, "up", "-d");
        
        // 🔒 SONAR FIX : Sécurisation du PATH
        Map<String, String> env = pb.environment();
        env.put("PATH", "/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin");
        
        pb.redirectErrorStream(true);
        Process process = pb.start();
        
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            String error = new String(process.getInputStream().readAllBytes());
            logger.error("❌ Erreur Docker Compose : {}", error);
            throw new RuntimeException("Échec du déploiement : " + error);
        }
    }

    // Passé en protected pour pouvoir le tester unitairement si besoin
    protected String generateDockerComposeYaml(TopologyRequest topology, String deploymentId) {
        StringBuilder yaml = new StringBuilder();
        yaml.append("version: '3.8'\n");
        yaml.append("services:\n");

        for (NodeDTO node : topology.nodes()) {
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
            
            String containerName = serviceName + "_" + deploymentId;
            yaml.append("    container_name: ").append(containerName).append("\n");
            yaml.append("    tty: true\n"); 
            
            if ("db".equals(node.type())) {
                 yaml.append("    environment:\n      MYSQL_ROOT_PASSWORD: root\n");
            }
            
            yaml.append("    networks:\n      - cyber_range\n\n");
        }

        yaml.append("networks:\n  cyber_range:\n    driver: bridge\n");
        return yaml.toString();
    }

    private String findKaliContainerName(TopologyRequest topology, String deploymentId) {
        for (NodeDTO node : topology.nodes()) {
            if ("kali".equals(node.type())) {
                String cleanLabel = node.label().toLowerCase().replaceAll("[^a-z0-9]", "");
                String serviceName = cleanLabel + "_" + node.id();
                return serviceName + "_" + deploymentId;
            }
        }
        if (!topology.nodes().isEmpty()) {
            NodeDTO node = topology.nodes().get(0);
            String cleanLabel = node.label().toLowerCase().replaceAll("[^a-z0-9]", "");
            return cleanLabel + "_" + node.id() + "_" + deploymentId;
        }
        return null;
    }
}