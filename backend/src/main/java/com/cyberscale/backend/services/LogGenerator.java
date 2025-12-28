package com.cyberscale.backend.services;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class LogGenerator {

    static final int TOTAL_LOGS = 500;
    static final int ANOMALY_LOGS = 50;
    static final String ATTACKER_IP = "192.168.1.66";
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

    public List<String> generateLogs() {
        List<String> logs = new ArrayList<>();

        // Bruit de fond
        int noiseLogs = TOTAL_LOGS - ANOMALY_LOGS;
        LocalDateTime baseTime = LocalDateTime.now().minusMinutes(HISTORY_WINDOW_MINUTES);
        
        for (int i = 0; i < noiseLogs; i++) {
            long randomSeconds = random.nextInt(HISTORY_WINDOW_MINUTES * SECONDS_IN_MINUTE);
            LocalDateTime logTime = baseTime.plusSeconds(randomSeconds);
            logs.add(generateRandomLogEntry(logTime));
        }

        // Anomalie
        LocalDateTime attackStartTime = LocalDateTime.now().minusMinutes(ATTACK_DELAY_MINUTES);

        for (int i = 0; i < ANOMALY_LOGS; i++) {
            LocalDateTime logTime = attackStartTime.plusSeconds(i + (long)random.nextInt(2));
            String timestamp = formatApacheDate(logTime);
            
            String anomalyLog = String.format("%s - - [%s] \"GET %s HTTP/1.1\" %d %d",
                    ATTACKER_IP, timestamp, TARGET_URL, ANOMALY_STATUS, ANOMALY_PACKET_SIZE);
            
            logs.add(anomalyLog);
        }

        // Tri
        logs.sort(Comparator.comparing(this::extractDateFromLog));

        return logs;
    }

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

    private String formatApacheDate(LocalDateTime date) {
        return date.format(APACHE_FORMATTER) + " +0000";
    }

    private LocalDateTime extractDateFromLog(String logLine) {
        int start = logLine.indexOf('[') + 1;
        int end = logLine.indexOf(']');
        String dateStr = logLine.substring(start, end).split(" ")[0];
        return LocalDateTime.parse(dateStr, APACHE_FORMATTER);
    }
}