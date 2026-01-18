package com.cyberscale.backend.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Entité représentant une question d'un quiz.
 */
@Entity
@Table(name = "questions")
public class Question implements IQuestion { 
    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    
    private String text;

    @Enumerated(EnumType.STRING)
    private IQuestion.CategorieQuestion categorie;

    @Enumerated(EnumType.STRING)
    private IQuestion.DifficultyQuestion difficulty;

    @Column(name = "points_weight")
    private Integer pointsWeight = 1;

    @Column(name = "exam_ref")
    private String examRef; // Ex: "CISSP", "CEH", "SECURITY+"
    
    @OneToMany(mappedBy = "question")
    @JsonManagedReference
    private List<AnswerOption> options;

    public Question() {}

    public Question(Long id, String text, IQuestion.CategorieQuestion categorie, IQuestion.DifficultyQuestion difficulty) {
        this.id = id;
        this.text = text;
        this.categorie = categorie;
        this.difficulty = difficulty;
    }
    
    public Long getId() { return id; }

    @Override
    public String getText() { return text; }

    @Override
    public IQuestion.CategorieQuestion getCategorie() { return categorie; }

    @Override
    public IQuestion.DifficultyQuestion getDifficulty() { return difficulty; }

    public Integer getPointsWeight() { return pointsWeight; }

    public String getExamRef() { return examRef; }

    @Override
    public List<AnswerOption> getOptions() {  return options != null ? new ArrayList<>(options) : new ArrayList<>(); }

    @Override
    public Map<Long, Boolean> getAnswerKeyMap() {
        Map<Long, Boolean> correction = new HashMap<>();
        if (getOptions() != null) {
            for (AnswerOption option : getOptions()) {
                correction.put(option.getId(), option.getIsCorrect());
            }
        }
        return correction;
    }

    public void setId(Long id) { this.id = id; }
    
    public void setText(String text) { this.text = text; }
    
    public void setCategorie(IQuestion.CategorieQuestion categorie) { this.categorie = categorie; }
    
    public void setDifficulty(IQuestion.DifficultyQuestion difficulty) { this.difficulty = difficulty; }  
    
    public void setPointsWeight(Integer pointsWeight) { this.pointsWeight = pointsWeight; }  
    
    public void setExamRef(String examRef) { this.examRef = examRef; }
   
    public void setOptions(List<AnswerOption> options) { this.options = options; }
}