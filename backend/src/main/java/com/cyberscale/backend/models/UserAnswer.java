package com.cyberscale.backend.models;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_answer")
public class UserAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_id")
    private QuizSession session;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;

    @ManyToOne
    @JoinColumn(name = "selected_option_id")
    private AnswerOption selectedOption;

    private LocalDateTime answeredAt;

    public UserAnswer() {
        this.answeredAt = LocalDateTime.now();
    }

    // Constructeur pratique pour le Service
    public UserAnswer(QuizSession session, Question question, AnswerOption selectedOption) {
        this.session = session;
        this.question = question;
        this.selectedOption = selectedOption;
        this.answeredAt = LocalDateTime.now();
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public QuizSession getSession() { return session; }
    public void setSession(QuizSession session) { this.session = session; }
    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }
    public AnswerOption getSelectedOption() { return selectedOption; }
    public void setSelectedOption(AnswerOption selectedOption) { this.selectedOption = selectedOption; }
    public LocalDateTime getAnsweredAt() { return answeredAt; }
    public void setAnsweredAt(LocalDateTime answeredAt) { this.answeredAt = answeredAt; }
}