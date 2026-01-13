package com.cyberscale.backend.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.cyberscale.backend.dto.DashboardResponse;
import com.cyberscale.backend.models.ExamSession;
import com.cyberscale.backend.models.QuizSession;
import com.cyberscale.backend.models.Recommendation;
import com.cyberscale.backend.models.User;
import com.cyberscale.backend.repositories.ExamSessionRepository;
import com.cyberscale.backend.repositories.QuizSessionRepository;
import com.cyberscale.backend.repositories.RecommendationRepository;
import com.cyberscale.backend.repositories.UserRepository;
import com.cyberscale.backend.services.QuizService;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    private final QuizService quizService;
    
    @Autowired private UserRepository userRepository;
    @Autowired private QuizSessionRepository quizSessionRepository;
    @Autowired private ExamSessionRepository examSessionRepository;
    @Autowired private RecommendationRepository recommendationRepository;

    public UserController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping("/{id}/dashboard")
    public ResponseEntity<DashboardResponse> getDashboardStats(@PathVariable Long id) {
        
        // 1. Calcul des Moyennes (Utilisation de VOTRE méthode existante)
        List<QuizSession> quizzes = quizSessionRepository.findByUserIdOrderByCreatedAtDesc(id);
        
        double totalScore = 0;
        int count = 0;

        for (QuizSession q : quizzes) {
            if (q.getScore() != null) { 
                totalScore += q.getScore(); 
                count++;
            }
        }

        // On calcule une moyenne globale (Théorie/Technique identiques pour l'instant)
        Double avgGlobal = count > 0 ? (totalScore / count) * 10 : 0.0;

        // 2. Liste des Certifications (Examens réussis)
        List<ExamSession> exams = examSessionRepository.findByUserId(id);
        List<String> certifs = exams.stream()
            .filter(e -> "VALIDATED".equals(e.getStatus()) || (e.getScore() != null && e.getScore() >= 80)) 
            .map(e -> "Certification " + (e.getExamRef() != null ? e.getExamRef() : "Sécurité"))
            .collect(Collectors.toList());

        // 3. Ressources Recommandées
        List<Recommendation> allRecos = recommendationRepository.findAll();
        List<DashboardResponse.RecommendationDTO> adaptiveRecos = allRecos.stream()
            .limit(3)
            .map(r -> new DashboardResponse.RecommendationDTO(r.getTitle(), r.getType(), r.getUrl()))
            .collect(Collectors.toList());

        return ResponseEntity.ok(new DashboardResponse(
            avgGlobal, // Théorie
            avgGlobal, // Technique
            count, 
            certifs, 
            adaptiveRecos
        ));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
    }

    @GetMapping("/{userId}/history")
    public ResponseEntity<List<com.cyberscale.backend.dto.HistoryDTO>> getUserHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(quizService.getUserHistory(userId));
    }
}