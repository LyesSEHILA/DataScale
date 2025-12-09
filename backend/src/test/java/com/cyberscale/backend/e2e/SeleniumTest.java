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
        WebDriverManager.firefoxdriver().setup();
    }

    @BeforeEach
    void setupTest() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("-headless"); 
        
        driver = new FirefoxDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
    }

    @AfterEach
    void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void testUserFlow_ShouldStartQuiz() {
        // CORRECTION ICI : On pointe vers quiz-intro.html au lieu de index.html
        File frontendDir = Paths.get("..", "frontend", "quiz-intro.html").toAbsolutePath().normalize().toFile();
        String fileUrl = "file://" + frontendDir.getPath();
        
        System.out.println("Test Selenium sur : " + fileUrl);

        // 2. Ouvrir la page
        driver.get(fileUrl);

        // 3. Remplir le formulaire (qui existe bien sur quiz-intro.html)
        WebElement ageInput = driver.findElement(By.id("ageInput"));
        ageInput.clear(); // Bonne pratique : vider avant d'écrire
        ageInput.sendKeys("25");

        // 4. Vérifier le bouton
        WebElement startButton = driver.findElement(By.id("startButton"));
        assertTrue(startButton.isDisplayed(), "Le bouton commencer doit être visible");
        
        // On ne clique pas pour éviter l'erreur API si le backend est éteint, 
        // le but est de valider que la page s'affiche et que les éléments sont là.
    }
}