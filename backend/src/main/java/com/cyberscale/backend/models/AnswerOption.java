package com.cyberscale.backend.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "answers_option")
public class AnswerOption {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    private String text;
    private Boolean isCorrect;

    public AnswerOption() {}

    public AnswerOption(Long id, String text, boolean isCorrect) {
        this.id = id;
        this.text = text;
        this.isCorrect = false;
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


    public Boolean getIsCorrect() {
        return isCorrect;
    }


    public void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
    }
    
}