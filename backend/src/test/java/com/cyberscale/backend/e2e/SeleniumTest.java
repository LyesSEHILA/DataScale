package com.cyberscale.backend.e2e;

import java.io.File;
import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor; // <--- Import Important
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
        
        // CRUCIAL : Permet au localStorage de fonctionner correctement entre fichiers locaux (file://)
        options.addPreference("security.fileuri.strict_origin_policy", false);
        options.addPreference("dom.storage.enabled", true);
        
        driver = new FirefoxDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
    }

    @AfterEach
    void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private String getFrontendFileUrl(String filename) {
        String currentDir = System.getProperty("user.dir");
        File projectRoot;
        if (currentDir.endsWith("backend")) {
            projectRoot = new File(currentDir).getParentFile();
        } else {
            projectRoot = new File(currentDir);
        }
        File file = new File(projectRoot, "frontend/" + filename);
        assertTrue(file.exists(), "ERREUR: Fichier introuvable -> " + file.getAbsolutePath());
        return "file://" + file.getAbsolutePath();
    }

    @Test
    void testUserFlow_ShouldStartQuiz() {
        String fileUrl = getFrontendFileUrl("quiz-intro.html");
        driver.get(fileUrl);

        WebElement ageInput = driver.findElement(By.id("ageInput"));
        ageInput.clear();
        ageInput.sendKeys("25");

        WebElement startButton = driver.findElement(By.id("startButton"));
        assertTrue(startButton.isDisplayed(), "Le bouton commencer doit être visible");
    }

    @Test
    void testArena_ShouldLoadTerminal() {
        String fileUrl = getFrontendFileUrl("arena.html");
        
        // 1. On charge la page une première fois (ce qui déclenche la redirection vers login.html)
        driver.get(fileUrl);

        // 2. On injecte le cookie/token utilisateur via Javascript
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("localStorage.setItem('userId', '999');");
        js.executeScript("localStorage.setItem('userName', 'SeleniumTester');");

        // 3. On RECHARGE la page (maintenant l'AuthGuard va nous laisser passer !)
        driver.get(fileUrl);

        // 4. Vérifications
        WebElement terminalContainer = driver.findElement(By.id("terminal"));
        assertTrue(terminalContainer.isDisplayed(), "Le conteneur du terminal doit être visible");

        WebElement xtermScreen = driver.findElement(By.className("xterm-screen"));
        assertTrue(xtermScreen.isDisplayed(), "Le terminal Xterm.js doit être actif");
    }
}