package com.cyberscale.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cyberscale.backend.models.QuizSession;

public interface  QuizSessionRepository extends JpaRepository<QuizSession, Long>{
    
}
