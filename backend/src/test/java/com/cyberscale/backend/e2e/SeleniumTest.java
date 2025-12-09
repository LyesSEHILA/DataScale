package com.cyberscale.backend.e2e;

import java.io.File;
import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys; // <--- IMPORT CRUCIAL
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
        
        driver.get(fileUrl);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("localStorage.setItem('userId', '999');");
        js.executeScript("localStorage.setItem('userName', 'SeleniumTester');");
        driver.get(fileUrl);

        WebElement terminalContainer = driver.findElement(By.id("terminal"));
        assertTrue(terminalContainer.isDisplayed(), "Le conteneur du terminal doit être visible");

        WebElement xtermScreen = driver.findElement(By.className("xterm-screen"));
        assertTrue(xtermScreen.isDisplayed(), "Le terminal Xterm.js doit être actif");
    }

    // --- LE TEST CTF CORRIGÉ ---
    @Test
    void testArena_ScenarioCTF_ShouldRevealFlag() throws InterruptedException {
        String fileUrl = getFrontendFileUrl("arena.html");
        
        // 1. Auth & Chargement
        driver.get(fileUrl);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("localStorage.setItem('userId', '999');");
        js.executeScript("localStorage.setItem('userName', 'Hacker');");
        driver.get(fileUrl);

        WebElement terminalInput = driver.findElement(By.cssSelector("textarea.xterm-helper-textarea"));

        // 3. 'ls'
        terminalInput.sendKeys("ls" + Keys.ENTER); // Utilisation de Keys.ENTER
        Thread.sleep(1000); // Délai augmenté pour laisser Xterm réagir

        // 4. 'cat shadow' (Doit échouer)
        terminalInput.sendKeys("cat shadow" + Keys.ENTER);
        Thread.sleep(1000);
        
        String terminalText = driver.findElement(By.className("xterm-screen")).getText();
        // Debug : Affiche ce que Selenium voit pour aider si ça échoue encore
        System.out.println("Terminal Output: " + terminalText);
        
        assertTrue(terminalText.contains("Permission denied"), "cat shadow devrait être refusé");

        // 5. 'sudo cat shadow' (Doit réussir)
        terminalInput.sendKeys("sudo cat shadow" + Keys.ENTER);
        Thread.sleep(1500); // Sudo a un petit délai simulé

        // 6. Vérif Flag
        terminalText = driver.findElement(By.className("xterm-screen")).getText();
        assertTrue(terminalText.contains("CTF{LINUX_MASTER_2025}"), "Le flag doit être visible après sudo");
    }
}