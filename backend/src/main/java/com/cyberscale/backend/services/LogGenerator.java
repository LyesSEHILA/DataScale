package com.cyberscale.backend.services;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

@Service
public class LogGenerator {

    private static final String[] METHODS = {"GET", "POST", "PUT", "DELETE"};
    private static final String[] URLS = {
        "/index.html", "/login", "/dashboard", "/api/user", 
        "/assets/style.css", "/images/logo.png", "/about", "/contact"
    };
    private static final int[] STATUS_CODES = {200, 200, 200, 201, 304, 400, 401, 403, 404, 500}; // Plus de 200 pour le réalisme

    /**
     * Génère une liste de logs Apache simulés avec une anomalie injectée.
     * @return List<String> où chaque string est une ligne de log
     */
    public List<String> generateLogs() {
        List<String> logs = new ArrayList<>();
        Random random = new Random();

        // Génération du bruit de fond
        // On étale ça sur les 30 dernières minutes
        LocalDateTime baseTime = LocalDateTime.now().minusMinutes(30);
        
        for (int i = 0; i < 450; i++) {
            // Date aléatoire dans les 30 dernières minutes
            LocalDateTime logTime = baseTime.plusSeconds(random.nextInt(30 * 60));
            logs.add(generateRandomLogEntry(random, logTime));
        }

        // Injection de l'anomalie
        // Scénario : Attaque Force Brute / Scan sur "/admin" qui n'existe pas
        String attackerIp = "192.168.1.66"; // L'IP à trouver
        LocalDateTime attackStartTime = LocalDateTime.now().minusMinutes(15); // L'attaque a eu lieu il y a 15 min

        for (int i = 0; i < 50; i++) {
            // Les requêtes sont très rapprochées
            LocalDateTime logTime = attackStartTime.plusSeconds(i + random.nextInt(2));
            
            String timestamp = formatApacheDate(logTime);
            
            // Format : IP - - [Date] "METHOD URL HTTP/1.1" Status Size
            // Anomalie : Toujours la même IP, toujours 404, URL suspecte
            String anomalyLog = String.format("%s - - [%s] \"GET /admin/secret_config.php HTTP/1.1\" 404 198",
                    attackerIp, timestamp);
            
            logs.add(anomalyLog);
        }

        // Tri chronologique
        // Pour que l'analyse soit réaliste, les logs doivent être dans l'ordre temporel
        logs.sort(Comparator.comparing(this::extractDateFromLog));

        return logs;
    }

    // Méthodes Utilitaires 

    private String generateRandomLogEntry(Random random, LocalDateTime time) {
        String ip = random.nextInt(256) + "." + random.nextInt(256) + "." + random.nextInt(256) + "." + random.nextInt(256);
        String timestamp = formatApacheDate(time);
        String method = METHODS[random.nextInt(METHODS.length)];
        String url = URLS[random.nextInt(URLS.length)];
        int status = STATUS_CODES[random.nextInt(STATUS_CODES.length)];
        int size = random.nextInt(5000) + 200;

        return String.format("%s - - [%s] \"%s %s HTTP/1.1\" %d %d",
                ip, timestamp, method, url, status, size);
    }

    private String formatApacheDate(LocalDateTime date) {
        // Format standard Apache CLF : 10/Oct/2000:13:55:36 +0000
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss", Locale.ENGLISH);
        return date.format(formatter) + " +0000";
    }

    //Extrait la date d'une ligne de log pour le tri.
    private LocalDateTime extractDateFromLog(String logLine) {
        try {
            int start = logLine.indexOf("[") + 1;
            int end = logLine.indexOf("]");
            String dateStr = logLine.substring(start, end).split(" ")[0]; // On ignore le timezone +0000
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss", Locale.ENGLISH);
            return LocalDateTime.parse(dateStr, formatter);
        } catch (Exception e) {
            return LocalDateTime.MIN; // En cas d'erreur, on met au début
        }
    }
}