package com.cyberscale.backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cyberscale.backend.models.ExamSession;

/**
 * Interface d'acces aux donnees pour les sessions d'examen.
 */
@Repository
public interface ExamSessionRepository extends JpaRepository<ExamSession, Long> {
    List<ExamSession> findByUserId(Long userId);
}