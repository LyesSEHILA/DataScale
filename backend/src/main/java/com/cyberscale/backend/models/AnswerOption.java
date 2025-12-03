package com.cyberscale.backend.models;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table; 



@Entity
@Table(name = "answers_option")
public class AnswerOption {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    private String text;

    @ManyToOne
    @JoinColumn(name = "question_id")
    @JsonBackReference
    private Question question; 

    private boolean isCorrect;

    public AnswerOption() {}

    public AnswerOption(Long id, String text, boolean isCorrect) {
        this.id = id;
        this.text = text;
        this.isCorrect = isCorrect;
    }

    // Getters

    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public String getText() {
        return text;
    }

    // Setters


    public void setText(String text) {
        this.text = text;
    }


    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public Boolean isCorrect() {
        return isCorrect;
    }
    
}