package com.cyberscale.backend.models;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "Answers")
public class Answer {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private  Long id;
    private String text;

    public Answer() {}


    public Answer(long id, String text) {
        this.id = id;
        this.text = text;
    }


    // Getters

    public Long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    // Setters

    public void setId(Long id) {
        this.id = id;
    }


    public void setText(String text) {
        this.text = text;
    }
}
