package com.cyberscale.backend.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "questions")
public class Question {
    public static enum categorieQuestion{THEORY, TECHNIQUE}
    public static enum difficultyQuestion{EASY, MEDIUM, HARD}
    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private final int id;
    private final String text;
    private final categorieQuestion categorie;
    private final difficultyQuestion difficulty;

    public Question(int id, String text, categorieQuestion categorie, difficultyQuestion difficulty) {
        this.id = id;
        this.text = text;
        this.categorie = categorie;
        this.difficulty = difficulty;
    }
    
    public int getId() {
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
}
