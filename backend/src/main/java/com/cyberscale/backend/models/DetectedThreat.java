package com.cyberscale.backend.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "detected_threats")
public class DetectedThreat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ipAddress;

    private String countryCode;
    private String usageType;
    private Integer abuseConfidenceScore;
    private LocalDateTime detectedAt;

    public DetectedThreat() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

    public String getUsageType() { return usageType; }
    public void setUsageType(String usageType) { this.usageType = usageType; }

    public Integer getAbuseConfidenceScore() { return abuseConfidenceScore; }
    public void setAbuseConfidenceScore(Integer abuseConfidenceScore) { this.abuseConfidenceScore = abuseConfidenceScore; }

    public LocalDateTime getDetectedAt() { return detectedAt; }
    public void setDetectedAt(LocalDateTime detectedAt) { this.detectedAt = detectedAt; }
}