package com.cyberscale.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BackendApplicationTests {

    @Test
    void contextLoads() {
        // Vérifie simplement que le contexte Spring démarre sans erreur
    }

    @Test
    void publicVoidMain() {
        // Appelle explicitement la méthode main pour satisfaire SonarCloud
        // On utilise un port aléatoire (0) pour éviter les conflits si le contexte tourne déjà
        BackendApplication.main(new String[] {"--server.port=0"});
    }
}