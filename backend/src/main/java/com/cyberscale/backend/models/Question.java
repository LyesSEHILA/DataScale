package com.cyberscale.backend.models;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.HashMap;


@Entity
@Table(name = "questions")
public class Question  implements IQuestion{
    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    private String text;
    @Enumerated(EnumType.STRING)
    private categorieQuestion categorie;
    @Enumerated(EnumType.STRING)
    private difficultyQuestion difficulty;
    @OneToMany(mappedBy = "question")
    @JsonManagedReference // Important pour éviter les boucles infinies en JSON
    private List<AnswerOption> options;

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
    public int getCategorie() {
        return categorie.ordinal();
    }
    public int getDifficulty() {
        return this.difficulty.getValue();
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

    public List<AnswerOption> getOptions() { return new ArrayList<AnswerOption>(options); }
    public void setOptions(List<AnswerOption> options) { this.options = new ArrayList<AnswerOption>(options); }

    
    public Map<Long, Boolean> getAnswerKeyMap() {
        Map<Long, Boolean> correction = new HashMap<>();
    
        for (AnswerOption option : getOptions()) {
            correction.put(option.getId(), option.isCorrect());
        }

        return correction;
    
    }    
}
