package com.cyberscale.backend.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entité représentant une ressource pédagogique recommandée.
 * Affichée à l'utilisateur à la fin du Quiz d'évaluation
 */
@Entity
@Table(name = "recommendation")
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String type;
    private String url;
    private String targetProfile;

    public Recommendation() {}

    public Long getId() { return id; }
    
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    
    public void setTitle(String title) { this.title = title; }
    
    public String getType() { return type; }
    
    public void setType(String type) { this.type = type; }
    
    public String getUrl() { return url; }
    
    public void setUrl(String url) { this.url = url; }
    
    public String getTargetProfile() { return targetProfile; }
    
    public void setTargetProfile(String targetProfile) { this.targetProfile = targetProfile; }
}