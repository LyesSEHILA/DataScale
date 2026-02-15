package com.cyberscale.backend.services;

import com.cyberscale.backend.exceptions.KubernetesDeploymentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class KubernetesService {

    private static final Logger logger = LoggerFactory.getLogger(KubernetesService.class);
    private static final String KUBECTL_CMD = "/usr/local/bin/kubectl";
    private final TemplateGenerator templateGenerator;

    public KubernetesService(TemplateGenerator templateGenerator) {
        this.templateGenerator = templateGenerator;
    }

    public void deployDecoy(String type) {
        Path tempFile = null;
        try {
            String yamlContent = templateGenerator.generateYaml(type);

            if (!yamlContent.contains("runtimeClassName: kata")) {
                logger.error("ALERTE SÉCURITÉ: Tentative de déploiement sans isolation Kata !");
                throw new SecurityException("Déploiement refusé : Runtime Kata manquant.");
            }

            // 2. SÉCURITÉ : Créer le fichier avec des permissions restrictives (rw-------)
            // Seul le propriétaire (l'app Java) peut lire/écrire ce fichier temporaire.
            Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rw-------");
            tempFile = Files.createTempFile("k8s-decoy-", ".yaml", 
                                          PosixFilePermissions.asFileAttribute(perms));
            
            Files.writeString(tempFile, yamlContent);

            executeKubectlApply(tempFile);

        } catch (InterruptedException e) {
            // 3. RELIABILITY : Gérer correctement l'interruption du Thread
            Thread.currentThread().interrupt();
            throw new KubernetesDeploymentException("Le déploiement a été interrompu", e);
        } catch (IOException | SecurityException e) {
            // 4. MAINTAINABILITY : Utiliser l'exception personnalisée
            logger.error("Erreur lors du déploiement du leurre {}", type, e);
            throw new KubernetesDeploymentException("Echec déploiement K8s pour " + type, e);
        } finally {
            // Nettoyage dans le finally pour être sûr que ça s'exécute
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    logger.warn("Impossible de supprimer le fichier temporaire: {}", tempFile, e);
                }
            }
        }
    }

    private void executeKubectlApply(Path yamlFile) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                KUBECTL_CMD, "apply", "-f", yamlFile.toAbsolutePath().toString()
        );
        
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        
        // Lire la sortie pour le debug
        String output = new String(process.getInputStream().readAllBytes());
        
        boolean finished = process.waitFor(10, TimeUnit.SECONDS);
        if (!finished || process.exitValue() != 0) {
            throw new IOException("kubectl a échoué (Code " + process.exitValue() + "): " + output);
        }
        
        logger.info("Succès kubectl apply: \n{}", output);
    }
}
