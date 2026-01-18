package com.cyberscale.backend.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cyberscale.backend.dto.OnboardingRequest;
import com.cyberscale.backend.dto.ResultsResponse;
import com.cyberscale.backend.dto.UserAnswerRequest;
import com.cyberscale.backend.models.Question;
import com.cyberscale.backend.models.QuizSession;
import com.cyberscale.backend.services.QuizService;

import jakarta.validation.Valid;

/**
 * Controleur REST gérant le flux du Quiz d'evaluation initiale.
 * Il expose les endpoints pour :
 * - Initialiser une session.
 * - Récupérer les questions adaptées.
 * - Soumettre les réponses.
 * - Obtenir les résultats et recommandations.
 */
@RestController 
@RequestMapping("/api/quiz") 
@CrossOrigin(origins = "*")
public class QuizController {

    @Autowired
    private QuizService quizService; 

    /**
     * Démarre une nouvelle session de quiz baser sur les donnees d'onboarding.
     * @param request DTO contenant l'âge et les auto-évaluations.
     * @return La session créée avec le code HTTP 201.
     */
    @PostMapping("/start") 
    public ResponseEntity<QuizSession> startQuiz(@Valid @RequestBody OnboardingRequest request) {
        QuizSession createdSession = quizService.createSession(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSession);
    }

    /**
     * Enregistre une réponse utilisateur.
     * @param request DTO contenant l'ID de session, de question et de l'option choisie.
     * @return code HTTP 200 si l'enregistrement a réussi.
     */
    @PostMapping("/answer")
    public ResponseEntity<Void> submitAnswer(@Valid @RequestBody UserAnswerRequest request) {
        quizService.saveUserAnswer(request);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Récupère la liste des questions génerées pour une session donnée.
     * L'algorithme adaptatif du service décide quelles questions renvoyer.
     * @param sessionId L'ID de la session active.
     * @return Liste des questions.
     */
    @GetMapping("/questions")
    public ResponseEntity<List<Question>> getQuestions(@RequestParam Long sessionId) {
        List<Question> questions = quizService.getQuestionsForSession(sessionId);
        return ResponseEntity.ok(questions);
    }

    /**
     * Clôture la session, calcule les scores finaux et renvoie les recommandations.
     * @param sessionId L'ID de la session à terminer.
     * @return Les scores et les ressources pédagogiques suggerées.
     */
    @GetMapping("/results")
    public ResponseEntity<ResultsResponse> getResults(@RequestParam Long sessionId) {
        ResultsResponse results = quizService.calculateAndGetResults(sessionId);
        return ResponseEntity.ok(results);
    }
}