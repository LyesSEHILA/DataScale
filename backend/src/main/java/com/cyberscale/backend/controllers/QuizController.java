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

    @PostMapping("/answer")
    public ResponseEntity<Void> submitAnswer(@Valid @RequestBody UserAnswerRequest request) {
        quizService.saveUserAnswer(request);
        return ResponseEntity.ok().build(); // Retourne 200 OK sans contenu
    }
    
    @GetMapping("/questions")
    public ResponseEntity<List<Question>> getQuestions(@RequestParam Long sessionId) {
        List<Question> questions = quizService.getQuestionsForSession(sessionId);
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/results")
    public ResponseEntity<ResultsResponse> getResults(@RequestParam Long sessionId) {
        ResultsResponse results = quizService.calculateAndGetResults(sessionId);
        return ResponseEntity.ok(results);
    }


}