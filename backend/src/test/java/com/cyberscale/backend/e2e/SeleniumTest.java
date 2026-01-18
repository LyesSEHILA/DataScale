package com.cyberscale.backend.e2e;

import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor; // <--- Import nécessaire
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

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
    }

    @AfterEach
    void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void testUserFlow_ShouldStartQuiz() {
        // 1. CHEMIN DU FICHIER
        File file = Paths.get("..", "frontend", "quiz-intro.html").toAbsolutePath().normalize().toFile();
        if (!file.exists()) {
            file = Paths.get("frontend", "quiz-intro.html").toAbsolutePath().normalize().toFile();
        }
        if (!file.exists()) fail("❌ ERREUR : Fichier HTML introuvable");

        String fileUrl = "file://" + file.getPath();
        System.out.println("✅ Navigation vers : " + fileUrl);

        // 2. OUVERTURE INITIALE (Pour charger le contexte)
        driver.get(fileUrl);

        // --- CORRECTIF AUTHENTIFICATION ---
        // On injecte le token pour passer le auth-guard.js
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("localStorage.setItem('userId', '999');");
        js.executeScript("localStorage.setItem('userName', 'TestUser');");
        System.out.println("✅ Token d'authentification injecté.");

        // On recharge la page pour que le auth-guard nous laisse passer cette fois
        driver.get(fileUrl);
        // ----------------------------------

        // 3. ATTENTE ET INTERACTION
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try {
            // Maintenant, on devrait rester sur quiz-intro.html
            WebElement ageInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("ageInput")));
            
            ageInput.clear();
            ageInput.sendKeys("25");

            WebElement startButton = driver.findElement(By.id("startButton"));
            if(startButton.isDisplayed()) {
                startButton.click();
                System.out.println("✅ Test réussi : Formulaire rempli et bouton cliqué.");
            } else {
                fail("Le bouton n'est pas visible.");
            }
            
        } catch (Exception e) {
            System.err.println("❌ ÉCHEC : Élément introuvable.");
            System.err.println("URL actuelle : " + driver.getCurrentUrl()); // Utile pour voir si on est sur login.html
            System.err.println("Source page :\n" + driver.getPageSource());
            throw e;
        }
    }
}