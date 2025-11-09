package com.cyberscale.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cyberscale.backend.models.Question;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    
}