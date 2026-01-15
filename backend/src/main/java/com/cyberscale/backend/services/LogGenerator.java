package com.cyberscale.backend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;


/**
 * Service responsable de la génération de logs Apache simulés.
 * Il crée un mélange de trafic "normal" et injecte une anomalie (tentative d'attaque).
 */
@Service
public class LogGenerator {

    static final int TOTAL_LOGS = 500;
    static final int ANOMALY_LOGS = 50;
    
    static final String DEFAULT_ATTACKER_IP = "192.0.2.66"; 
    
    static final String TARGET_URL = "/admin/secret_config.php";
    static final int ANOMALY_STATUS = 404;

    private static final int HISTORY_WINDOW_MINUTES = 30;
    private static final int ATTACK_DELAY_MINUTES = 15;
    private static final int SECONDS_IN_MINUTE = 60;
    private static final int MIN_PACKET_SIZE = 200;
    private static final int PACKET_SIZE_VARIANCE = 5000;
    private static final int ANOMALY_PACKET_SIZE = 198;
    private static final int IP_MAX_VAL = 256;

    private static final DateTimeFormatter APACHE_FORMATTER = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss", Locale.ENGLISH);

    private static final String[] METHODS = {"GET", "POST", "PUT", "DELETE"};
    private static final String[] URLS = {
        "/index.html", "/login", "/dashboard", "/api/user", 
        "/assets/style.css", "/images/logo.png", "/about", "/contact"
    };
    private static final int[] STATUS_CODES = {200, 200, 200, 201, 304, 400, 401, 403, 404, 500};

    private final SecureRandom random = new SecureRandom();

    @Value("${cyberscale.simulation.attacker-ip:192.0.2.66}") 
    private String attackerIp;


    /**
     * Permet de modifier l'IP de l'attaquant manuellement.
     * @param attackerIp L'IP de l'attaquant
     */
    public void setAttackerIp(String attackerIp) {
        this.attackerIp = attackerIp;
    }
    
    /**
     * Récupère l'IP de l'attaquant configurée.
     * @return L'adresse IP actuelle de l'attaquant.
     */
    public String getAttackerIp() {
        return attackerIp;
    }

    /**
     * Génère la liste complète des logs (trafic normal + anomalie).
     * @return Une liste de chaînes de caractères au format Log Apache.
     */
    public List<String> generateLogs() {
        List<String> logs = new ArrayList<>();

        int noiseLogs = TOTAL_LOGS - ANOMALY_LOGS;
        LocalDateTime baseTime = LocalDateTime.now().minusMinutes(HISTORY_WINDOW_MINUTES);
        
        for (int i = 0; i < noiseLogs; i++) {
            long randomSeconds = random.nextInt(HISTORY_WINDOW_MINUTES * SECONDS_IN_MINUTE);
            LocalDateTime logTime = baseTime.plusSeconds(randomSeconds);
            logs.add(generateRandomLogEntry(logTime));
        }

        LocalDateTime attackStartTime = LocalDateTime.now().minusMinutes(ATTACK_DELAY_MINUTES);

        for (int i = 0; i < ANOMALY_LOGS; i++) {
            LocalDateTime logTime = attackStartTime.plusSeconds(i + (long)random.nextInt(2));
            String timestamp = formatApacheDate(logTime);
            
            String currentIp = (attackerIp != null) ? attackerIp : DEFAULT_ATTACKER_IP;

            String anomalyLog = String.format("%s - - [%s] \"GET %s HTTP/1.1\" %d %d",
                    currentIp, timestamp, TARGET_URL, ANOMALY_STATUS, ANOMALY_PACKET_SIZE);
            
            logs.add(anomalyLog);
        }

        logs.sort(Comparator.comparing(this::extractDateFromLog));
        return logs;
    }

    /**
     * Génère une ligne de log aléatoire "normale".
     * @param time La date et l'heure à laquelle le log a été généré.
     * @return Une chaîne formatée représentant une ligne de log Apache standard.
     */
    private String generateRandomLogEntry(LocalDateTime time) {
        String ip = random.nextInt(IP_MAX_VAL) + "." + random.nextInt(IP_MAX_VAL) + "." + 
                   random.nextInt(IP_MAX_VAL) + "." + random.nextInt(IP_MAX_VAL);
        
        String timestamp = formatApacheDate(time);
        String method = METHODS[random.nextInt(METHODS.length)];
        String url = URLS[random.nextInt(URLS.length)];
        int status = STATUS_CODES[random.nextInt(STATUS_CODES.length)];
        int size = random.nextInt(PACKET_SIZE_VARIANCE) + MIN_PACKET_SIZE;

        return String.format("%s - - [%s] \"%s %s HTTP/1.1\" %d %d",
                ip, timestamp, method, url, status, size);
    }

    /**
     * Formate une date en format date Apache (ex: 10/Oct/2023:13:55:36 +0000).
     * @param date L'objet LocalDateTime à formater.
     * @return La date sous forme de chaîne respectant le format Common Log Format.
     */
    private String formatApacheDate(LocalDateTime date) {
        return date.format(APACHE_FORMATTER) + " +0000";
    }

    /**
     * Extrait la date d'une ligne de log pour permettre le tri.
     * @param logLine La ligne de log contenant la date.
     * @return L'objet LocalDateTime correspondant à la date du log.
     */
    private LocalDateTime extractDateFromLog(String logLine) {
        int start = logLine.indexOf('[') + 1;
        int end = logLine.indexOf(']');
        String dateStr = logLine.substring(start, end).split(" ")[0];
        return LocalDateTime.parse(dateStr, APACHE_FORMATTER);
    }
}