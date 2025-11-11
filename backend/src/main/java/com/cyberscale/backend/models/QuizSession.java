package com.cyberscale.backend.models;

import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "quiz_session")
public class QuizSession {
    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    
    // 'prenom' n'était pas dans notre DTO. On devrait l'enlever ou l'ajouter au DTO.
    // Pour l'instant, je le laisse, mais c'est à revoir.
    private String prenom; 
    private Long age;
    private Long selfEvalTheory;
    private Long selfEvalTechnique;
    private Double finalScoreTheory;
    private Double finalScoreTechnique;
    private LocalDateTime createdAt;

    // Constructeur vide (obligatoire pour JPA)
    public QuizSession() {
        this.createdAt = LocalDateTime.now();
    }

    // --- Getters ---
    public Long getId() { return id; }
    public String getPrenom() { return prenom; }
    public Long getAge() { return age; }
    public Long getSelfEvalTheory() { return selfEvalTheory; }
    public Long getSelfEvalTechnique() { return selfEvalTechnique; }
    public Double getFinalScoreTheory() { return finalScoreTheory; }
    public Double getFinalScoreTechnique() { return finalScoreTechnique; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // --- Setters ---
    public void setId(Long id) { this.id = id; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public void setAge(Long age) { this.age = age; }
    public void setSelfEvalTheory(Long val) { this.selfEvalTheory = val; }
    public void setSelfEvalTechnique(Long val) { this.selfEvalTechnique = val; }
    public void setFinalScoreTheory(Double val) { this.finalScoreTheory = val; }
    public void setFinalScoreTechnique(Double val) { this.finalScoreTechnique = val; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}