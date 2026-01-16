package com.cyberscale.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cyberscale.backend.models.AnswerOption;

/**
 * Interface d'acces aux donnees pour les options de reponse (Table "answers_option").
 */
@Repository
public interface AnswerOptionRepository extends JpaRepository<AnswerOption, Long> {
}