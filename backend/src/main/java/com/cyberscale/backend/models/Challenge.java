package com.cyberscale.backend.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "challenges")
public class Challenge {

    @Id
    private String id;

    private String name;
    private String description;
    private String flagSecret;
    private Integer pointsReward;

    public Challenge() {}

    public Challenge(String id, String name, String description, String flagSecret, Integer pointsReward) {
        this.id = id;
        this.name = name;
        this.description = description; 
        this.flagSecret = flagSecret;
        this.pointsReward = pointsReward;
    }

    // Getters
    public String getId() { return id; }
    public String getFlagSecret() { return flagSecret; }
    public Integer getPointsReward() { return pointsReward; }
    
    public String getName() { return name; }
    public String getDescription() { return description; }
}