package com.cyberscale.backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cyberscale.backend.models.IQuestion;
import com.cyberscale.backend.models.Question; 

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    // Signature mise à jour avec IQuestion...
    List<Question> findByCategorieAndDifficulty(IQuestion.CategorieQuestion categorie, IQuestion.DifficultyQuestion difficulty);
}