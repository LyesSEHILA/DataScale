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
 * Entité représentant une session d'examen blanc.
 * Elle contient les informations du candidat, le statut et les scores finaux.
 */
@Entity
@Table(name = "exam_session")
public class ExamSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String examRef; 
    private Integer successProbability;
    private String candidateName;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer finalScore;
    private Integer maxPossibleScore;

    public ExamSession() {
        this.startTime = LocalDateTime.now();
        this.endTime = this.startTime.plusMinutes(60); 
        this.status = "PENDING";
    }

    public Integer getScore() { return this.finalScore; }

    public String getStatus() { return this.status; }

    public void setStatus(String status) { this.status = status; }

    public Long getId() { return id; }
    
    public void setId(Long id) { this.id = id; }
    
    public String getCandidateName() { return candidateName; }
    
    public void setCandidateName(String name) { this.candidateName = name; }

    public LocalDateTime getStartTime() { return startTime; }
    
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Integer getFinalScore() { return finalScore; }
    
    public void setFinalScore(Integer finalScore) { this.finalScore = finalScore; }

    public Integer getMaxPossibleScore() { return maxPossibleScore; }
    
    public void setMaxPossibleScore(Integer max) { this.maxPossibleScore = max; }
    
    public boolean isValid() { return LocalDateTime.now().isBefore(this.endTime); }

    public User getUser() { return user; }
    
    public void setUser(User user) { this.user = user; }
    
    public String getExamRef() { return examRef; }
    
    public void setExamRef(String examRef) { this.examRef = examRef; }

    public Integer getSuccessProbability() { return successProbability; }
    
    public void setSuccessProbability(Integer successProbability) { this.successProbability = successProbability; }
}