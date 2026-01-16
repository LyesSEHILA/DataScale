package com.cyberscale.backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cyberscale.backend.models.IQuestion;
import com.cyberscale.backend.models.Question; 

/**
 * Interface d'acces aux donnees pour la banque de questions (Table "questions").
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByCategorieAndDifficulty(IQuestion.CategorieQuestion categorie, IQuestion.DifficultyQuestion difficulty);
    List<Question> findByExamRef(String examRef);
}