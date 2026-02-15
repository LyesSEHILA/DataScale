package com.cyberscale.backend.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Service
public class KubernetesService {

    private static final Logger logger = LoggerFactory.getLogger(KubernetesService.class);
    private final TemplateGenerator templateGenerator;

    public KubernetesService(TemplateGenerator templateGenerator) {
        this.templateGenerator = templateGenerator;
    }

    public void deployDecoy(String type) {
        try {
            // 1. Générer le YAML unique
            String yamlContent = templateGenerator.generateYaml(type);

            // 2. SÉCURITÉ : Vérifier la présence du runtime sécurisé "kata"
            // (C'est crucial pour l'isolation des conteneurs malveillants)
            if (!yamlContent.contains("runtimeClassName: kata")) {
                logger.error("ALERTE SÉCURITÉ: Tentative de déploiement sans isolation Kata Containers !");
                throw new SecurityException("Déploiement refusé : Runtime Kata manquant.");
            }

            // 3. Écrire le fichier temporaire
            Path tempFile = Files.createTempFile("k8s-decoy-", ".yaml");
            Files.writeString(tempFile, yamlContent);
            logger.info("Fichier YAML temporaire créé : {}", tempFile.toAbsolutePath());

            // 4. Exécuter kubectl apply
            executeKubectlApply(tempFile);

            // 5. Nettoyage (supprimer le fichier temporaire)
            Files.deleteIfExists(tempFile);

        } catch (Exception e) {
            logger.error("Erreur lors du déploiement du leurre {}", type, e);
            throw new RuntimeException("Echec déploiement K8s", e);
        }
    }

    private void executeKubectlApply(Path yamlFile) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "kubectl", "apply", "-f", yamlFile.toAbsolutePath().toString()
        );
        
        // Rediriger les logs du processus vers nos logs Java
        processBuilder.redirectErrorStream(true);
        
        Process process = processBuilder.start();
        
        // Lire la sortie (optionnel mais recommandé pour debug)
        String output = new String(process.getInputStream().readAllBytes());
        
        boolean finished = process.waitFor(10, TimeUnit.SECONDS);
        if (!finished || process.exitValue() != 0) {
            throw new IOException("kubectl a échoué : " + output);
        }
        
        logger.info("Succès kubectl apply: \n{}", output);
    }
}
