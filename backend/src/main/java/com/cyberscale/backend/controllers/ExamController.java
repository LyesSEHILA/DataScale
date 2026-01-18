package com.cyberscale.backend.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cyberscale.backend.dto.ExamAnswerRequest;
import com.cyberscale.backend.models.ExamSession;
import com.cyberscale.backend.services.ExamService;
import java.util.Map;

/**
 * Controleur REST gérant le module de Certification Blanche.
 * Il expose les endpoints pour :
 * - Démarrer un examen chronometré.
 * - Soumettre des réponses.
 * - Récupérer le temps restant et les résultats finaux.
 */
@RestController
@RequestMapping("/api/exam")
@CrossOrigin(origins = "*")
public class ExamController {

    @Autowired
    private ExamService examService;

    /**
     * Initialise une nouvelle session d'examen.
     * @param candidateName Nom du candidat.
     * @param userId ID de l'utilisateur.
     * @param examRef Réfèrence de l'examen choisi.
     * @return La session créée avec le chronomètre qui demarré.
     */
   @PostMapping("/start")
    public ResponseEntity<ExamSession> start(
            @RequestParam String candidateName, 
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String examRef) { 
        return ResponseEntity.ok(examService.startExam(candidateName, userId, examRef));
    }

    /**
     * Enregistre une réponse a une question d'examen.
     * Le service verifie que le temps n'est pas ecoulé.
     * @param request DTO contenant les IDs (Session, Question, Option).
     * @return code HTTP 200 si accepté.
     */
    @PostMapping("/answer")
    public ResponseEntity<Void> answer(@RequestBody ExamAnswerRequest request) {
        examService.submitExamAnswer(request.sessionId(), request.questionId(), request.optionId());
        return ResponseEntity.ok().build();
    }

    /**
     * Termine l'examen manuellement ou automatiquement.
     * Calcule le score final et la probabilité de réussite.
     * @param sessionId L'ID de la session.
     * @return La session mise à jour avec les résultats.
     */
    @PostMapping("/finish/{sessionId}")
    public ResponseEntity<ExamSession> finish(@PathVariable Long sessionId) {
        return ResponseEntity.ok(examService.finishExam(sessionId));
    }

    /**
     * Récupère la liste des questions pour l'examen.
     * @param sessionId L'ID de la session.
     * @return Liste de questions.
     */
    @GetMapping("/{sessionId}/questions")
    public ResponseEntity<List<com.cyberscale.backend.models.Question>> getQuestions(@PathVariable Long sessionId) {
        return ResponseEntity.ok(examService.getExamQuestions(sessionId));
    }

    /**
     * Récupère les métadonnées en temps réel.
     * @param sessionId L'ID de la session.
     * @return Map contenant les informations sur l'examen.
     */
    @GetMapping("/{sessionId}/status")
    public ResponseEntity<?> getExamStatus(@PathVariable Long sessionId) {
        return ResponseEntity.ok(examService.getExamStatus(sessionId));
    }
}
