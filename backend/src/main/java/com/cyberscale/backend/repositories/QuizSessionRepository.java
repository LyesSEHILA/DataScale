package com.cyberscale.backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cyberscale.backend.models.QuizSession;

@Repository
public interface QuizSessionRepository extends JpaRepository<QuizSession, Long> {
    
    List<QuizSession> findByUserIdOrderByCreatedAtDesc(Long userId);
}