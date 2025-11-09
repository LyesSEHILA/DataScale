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
    private final int id;
    private final String prenom;
    private final int age;
    private int selfEvalTheory;
    private int selfEvalTechnique;
    private double finalScoreTheory;
    private double finalScoreTechnique;
    
    private List<Question> questions;
    private List<AnswerOption> reponses;
    private Map<Question,Answer> QuestionsReponses;

    private LocalDateTime createdAt;

    public QuizSession(int id, String prenom, int age, List<Question> questions, List<AnswerOption> reponses, Map<Question, Answer> QuestionsReponses) {
        this.id = id;
        this.prenom = prenom;
        this.age = age;
        this.selfEvalTheory = 0;
        this.selfEvalTechnique = 0;
        this.finalScoreTheory = 0.0;
        this.finalScoreTechnique = 0.0;

        this.questions = new ArrayList<>(questions);
        this.reponses = null;
        this.QuestionsReponses = new HashMap<>(QuestionsReponses);

        this.createdAt = LocalDateTime.now();

    }
    
    public int getId() {
        return id;
    }
    
    public String getPrenom() {
        return prenom;
    }
    
    public int getAge() {
        return age;
    }
    
    public int getSelfEvalTheory() {
        return selfEvalTheory;
    }
    
    public int getSelfEvalTechnique() {
        return selfEvalTechnique;
    }
    
    public double getFinalScoreTheory() {
        return finalScoreTheory;
    }
    
    public double getFinalScoreTechnique() {
        return finalScoreTechnique;
    }


    public void setSelfEvalTheory(int val) {
        this.selfEvalTheory = val;
    }

    public void setSelfEvalTechnique(int val) {
        this.selfEvalTechnique = val;
    }

    public void setFinalScoreTheory(double val) {
        this.finalScoreTheory = val;
    }

    public void setFinalScoreTechnique(double val) {
        this.finalScoreTechnique = val;
    }

    public List<Question> getQuestions() {
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
    }

}