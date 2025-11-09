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
    private final int id;
    private final String text;
    private boolean isCorrect;


    public AnswerOption(int id, String text, boolean isCorrect) {
        this.id = id;
        this.text = text;
        this.isCorrect = false;
    }
    
    public int getId() {
        return id;
    }
    
    public String getText() {
        return text;
    }
   
    public boolean isCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(String reponse) {
        this.isCorrect = this.text.equals(reponse);
    }
}