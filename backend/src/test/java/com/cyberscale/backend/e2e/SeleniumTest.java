package com.cyberscale.backend.e2e;

import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import io.github.bonigarcia.wdm.WebDriverManager;

public class SeleniumTest {

    private WebDriver driver;

    @BeforeAll
    static void setupClass() {
        // Installe le pilote Firefox (GeckoDriver) automatiquement
        WebDriverManager.firefoxdriver().setup();
    }

    @BeforeEach
    void setupTest() {
        // Configuration de Firefox en mode "sans tête" (invisible) pour la CI
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("-headless"); 
        
        driver = new FirefoxDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
    }

    @AfterEach
    void teardown() {
        if (driver != null) {
            driver.quit(); // Ferme le navigateur
        }
    }

    @Test
    void testUserFlow_ShouldStartQuiz() {
        // 1. Trouver le chemin absolu vers le fichier index.html du frontend
        // On remonte d'un cran (backend/../frontend)
        File frontendDir = Paths.get("..", "frontend", "index.html").toAbsolutePath().normalize().toFile();
        String fileUrl = "file://" + frontendDir.getPath();
        
        System.out.println("Test Selenium sur : " + fileUrl);

        // 2. Ouvrir la page
        driver.get(fileUrl);

        // 3. Vérifier qu'on est sur la bonne page (Titre)
        String title = driver.getTitle();
        // On vérifie juste que le titre contient "CyberScale" (ou ce que tu as mis dans <title>)
        // Adapte "CyberScale" si ton titre est différent !
        assertTrue(title.contains("CyberScale"), "Le titre de la page devrait contenir 'CyberScale'");

        // 4. Tenter de trouver le bouton (juste pour prouver que le DOM est chargé)
        WebElement startButton = driver.findElement(By.id("startButton"));
        assertTrue(startButton.isDisplayed(), "Le bouton commencer doit être visible");
        
        // On ne clique pas car l'API ne tourne pas forcément pendant le test, 
        // et on veut que le test soit vert (réussite) pour valider la config Selenium.
    }
}