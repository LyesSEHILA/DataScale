package com.cyberscale.backend.models;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Entité représentant une session de Quiz d'évaluation (Onboarding).
 * Elle stocke les données du candidat, ses auto-évaluations et les scores calculés.
 */
@Entity
@Table(name = "quiz_session")
public class QuizSession {
    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    private String prenom; 
    private Long age;
    private Long selfEvalTheory;
    private Long selfEvalTechnique;
    private Double finalScoreTheory;
    private Double finalScoreTechnique;
    private LocalDateTime createdAt;

    public QuizSession() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Calcule le score global moyen de la session.
     * @return La moyenne pondérée ou 0.0 si aucune note n'existe.
     */
    public Double getScore() {
        if (finalScoreTheory == null && finalScoreTechnique == null) return 0.0;
        if (finalScoreTheory == null) return finalScoreTechnique;
        if (finalScoreTechnique == null) return finalScoreTheory;
        return (finalScoreTheory + finalScoreTechnique) / 2.0;
    }

    public Long getId() { return id; }
    public String getPrenom() { return prenom; }
    public Long getAge() { return age; }
    public Long getSelfEvalTheory() { return selfEvalTheory; }
    public Long getSelfEvalTechnique() { return selfEvalTechnique; }
    public Double getFinalScoreTheory() { return finalScoreTheory; }
    public Double getFinalScoreTechnique() { return finalScoreTechnique; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public void setAge(Long age) { this.age = age; }
    public void setSelfEvalTheory(Long val) { this.selfEvalTheory = val; }
    public void setSelfEvalTechnique(Long val) { this.selfEvalTechnique = val; }
    public void setFinalScoreTheory(Double val) { this.finalScoreTheory = val; }
    public void setFinalScoreTechnique(Double val) { this.finalScoreTechnique = val; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}