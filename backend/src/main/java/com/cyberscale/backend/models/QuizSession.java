package com.cyberscale.backend.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private String prenom;
    private Long age;
    private Long selfEvalTheory;
    private Long selfEvalTechnique;
    private Double finalScoreTheory;
    private Double finalScoreTechnique;
    
    /**private List<Question> questions;
    private List<AnswerOption> reponses;
    private Map<Question,Answer> QuestionsReponses;*/

    private LocalDateTime createdAt;


    public QuizSession() {this.createdAt = LocalDateTime.now();}

    public QuizSession(Long id, String prenom, Long age, List<Question> questions, List<AnswerOption> reponses, Map<Question, Answer> QuestionsReponses) {
        this.id = id;
        this.prenom = prenom;
        this.age = age;
        this.selfEvalTheory = 0L;
        this.selfEvalTechnique = 0L;
        this.finalScoreTheory = 0.0;
        this.finalScoreTechnique = 0.0;

        /**this.questions = new ArrayList<>(questions);
        this.reponses = null;
        this.QuestionsReponses = new HashMap<>(QuestionsReponses);*/

        this.createdAt = LocalDateTime.now();

    }


    // Getters
    
    public Long getId() {
        return id;
    }
    
    public String getPrenom() {
        return prenom;
    }
    
    public Long getAge() {
        return age;
    }
    
    public Long getSelfEvalTheory() {
        return selfEvalTheory;
    }
    
    public Long getSelfEvalTechnique() {
        return selfEvalTechnique;
    }
    
    public Double getFinalScoreTheory() {
        return finalScoreTheory;
    }
    
    public Double getFinalScoreTechnique() {
        return finalScoreTechnique;
    }


    public void setSelfEvalTheory(Long val) {
        this.selfEvalTheory = val;
    }

    public void setSelfEvalTechnique(Long val) {
        this.selfEvalTechnique = val;
    }

    public void setFinalScoreTheory(Double val) {
        this.finalScoreTheory = val;
    }

    public void setFinalScoreTechnique(Double val) {
        this.finalScoreTechnique = val;
    }

    /**public List<Question> getQuestions() {
        return questions;
    }

    public List<AnswerOption> getReponses() {
        return reponses;
    }

    public Map<Question, Answer> getQuestionsReponses() {
        return QuestionsReponses;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }*/


    // Setters

    public void setId(Long id) {
        this.id = id;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public void setAge(Long age) {
        this.age = age;
    }

    /**public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public void setReponses(List<AnswerOption> reponses) {
        this.reponses = reponses;
    }

    public void setQuestionsReponses(Map<Question, Answer> questionsReponses) {
        QuestionsReponses = questionsReponses;
    }*/

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}