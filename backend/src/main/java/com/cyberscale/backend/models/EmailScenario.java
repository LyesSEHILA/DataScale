package com.cyberscale.backend.models;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.Map;

@Entity
@Table(name = "email_scenarios")
public class EmailScenario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sender;
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String contentHtml;

    @Enumerated(EnumType.STRING)
    private IQuestion.DifficultyQuestion difficulty;

    /**
     * Stocke la liste des zones piégées au format JSON.
     * Exemple de structure :
     * [
     * { "id": "trap1", "type": "link", "keyword": "cliquez ici", "explanation": "URL suspecte" },
     * { "id": "trap2", "type": "attachment", "filename": "facture.exe", "explanation": "Exécutable dangereux" }
     * ]
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "traps", columnDefinition = "jsonb")
    private List<Map<String, Object>> traps;

    public EmailScenario() {
    }

    public EmailScenario(String sender, String subject, String contentHtml, IQuestion.DifficultyQuestion difficulty, List<Map<String, Object>> traps) {
        this.sender = sender;
        this.subject = subject;
        this.contentHtml = contentHtml;
        this.difficulty = difficulty;
        this.traps = traps;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContentHtml() {
        return contentHtml;
    }

    public void setContentHtml(String contentHtml) {
        this.contentHtml = contentHtml;
    }

    public IQuestion.DifficultyQuestion getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(IQuestion.DifficultyQuestion difficulty) {
        this.difficulty = difficulty;
    }

    public List<Map<String, Object>> getTraps() {
        return traps;
    }

    public void setTraps(List<Map<String, Object>> traps) {
        this.traps = traps;
    }
}