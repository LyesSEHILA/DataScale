package com.cyberscale.backend.repositories;

import com.cyberscale.backend.models.QuizSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuizSessionRepository extends JpaRepository<QuizSession, Long> {
    
    List<QuizSession> findByUserIdOrderByCreatedAtDesc(Long userId);
}