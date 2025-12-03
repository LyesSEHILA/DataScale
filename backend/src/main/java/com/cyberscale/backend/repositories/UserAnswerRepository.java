package com.cyberscale.backend.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.cyberscale.backend.models.UserAnswer;

@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {
    List<UserAnswer> findBySessionId(Long sessionId);

    long countByQuestionIdAndSelectedOptionIsCorrectTrue(Long questionId);
}