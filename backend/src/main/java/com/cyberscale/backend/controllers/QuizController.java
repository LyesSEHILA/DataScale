package com.cyberscale.backend.controllers;

import com.cyberscale.backend.dto.OnboardingRequest;
import com.cyberscale.backend.models.Question;
import com.cyberscale.backend.models.QuizSession;
import com.cyberscale.backend.services.QuizService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController // Dit à Spring que ceci est un Controller API
@RequestMapping("/api/quiz") // Toutes les URL de ce fichier commenceront par /api/quiz
@CrossOrigin(origins = "*") // DoD #7: Autorise les appels du frontend
public class QuizController {

    @Autowired
    private QuizService quizService; // Injecte le "cerveau"

    /**
     * Endpoint pour démarrer une nouvelle session de quiz.
     * C'est le DoD #5.
     */
    @PostMapping("/start") // Se déclenche sur POST /api/quiz/start
    public ResponseEntity<QuizSession> startQuiz(@Valid @RequestBody OnboardingRequest request) {
        
        // @Valid: active la validation (DoD #1)
        // @RequestBody: dit à Spring de lire le JSON de la requête
        
        QuizSession createdSession = quizService.createSession(request);
        
        // DoD #6: Retourne un statut 201 CREATED et la session créée dans le corps
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSession);
    }

    @GetMapping("/questions")
    public ResponseEntity<List<Question>> getQuestions(@RequestParam Long sessionId) {
        List<Question> questions = quizService.getQuestionsForSession(sessionId);
        return ResponseEntity.ok(questions);
    }
}