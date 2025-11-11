package com.cyberscale.backend.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;   
import jakarta.persistence.Enumerated;


@Entity
@Table(name = "questions")
public class Question {
    public static enum categorieQuestion{THEORY, TECHNIQUE}
    public static enum difficultyQuestion{EASY, MEDIUM, HARD}
    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    private String text;
    @Enumerated(EnumType.STRING)
    private categorieQuestion categorie;
    @Enumerated(EnumType.STRING)
    private difficultyQuestion difficulty;


    public Question() {}

    public Question(Long id, String text, categorieQuestion categorie, difficultyQuestion difficulty) {
        this.id = id;
        this.text = text;
        this.categorie = categorie;
        this.difficulty = difficulty;
    }

    // Getters
    
    public Long getId() {
        return id;
    }
    public String getText() {
        return text;
    }
    public categorieQuestion getCategorie() {
        return categorie;
    }
    public difficultyQuestion getDifficulty() {
        return difficulty;
    }

    // Setters

    public void setId(Long id) {
        this.id = id;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setCategorie(categorieQuestion categorie) {
        this.categorie = categorie;
    }

    public void setDifficulty(difficultyQuestion difficulty) {
        this.difficulty = difficulty;
    }    
}
