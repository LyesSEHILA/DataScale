package com.cyberscale.backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cyberscale.backend.models.Question;
import com.cyberscale.backend.models.IQuestion.categorieQuestion;
import com.cyberscale.backend.models.IQuestion.difficultyQuestion;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    // Trouve toutes les questions d'une catégorie et difficulté données
    List<Question> findByCategorieAndDifficulty(categorieQuestion categorie, difficultyQuestion difficulty);
}