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

/**
 * Controleur REST gérant les données relatives à l'utilisateur.
 * Il expose les endpoints pour :
 * - Récupérer les statistiques du tableau de bord.
 * - Obtenir le profil utilisateur.
 * - Consulter l'historique complet.
 */
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

    /**
     * Agrege les statistiques pour le tableau de bord de l'utilisateur.
     * Calcule la moyenne des quiz, liste les certifications obtenues et propose des ressources.
     * @param id L'ID de l'utilisateur.
     * @return Un DTO DashboardResponse contenant toutes les informations.
     */
    @GetMapping("/{id}/dashboard")
    public ResponseEntity<DashboardResponse> getDashboardStats(@PathVariable Long id) {
        
        List<QuizSession> quizzes = quizSessionRepository.findByUserIdOrderByCreatedAtDesc(id);
        
        double totalScore = 0;
        int count = 0;

        for (QuizSession q : quizzes) {
            if (q.getScore() != null) { 
                totalScore += q.getScore(); 
                count++;
            }
        }

        Double avgGlobal = count > 0 ? (totalScore / count) * 10 : 0.0;

        List<ExamSession> exams = examSessionRepository.findByUserId(id);
        List<String> certifs = exams.stream()
            .filter(e -> "VALIDATED".equals(e.getStatus()) || (e.getScore() != null && e.getScore() >= 80)) 
            .map(e -> "Certification " + (e.getExamRef() != null ? e.getExamRef() : "Sécurité"))
            .collect(Collectors.toList());

        List<Recommendation> allRecos = recommendationRepository.findAll();
        List<DashboardResponse.RecommendationDTO> adaptiveRecos = allRecos.stream()
            .limit(3)
            .map(r -> new DashboardResponse.RecommendationDTO(r.getTitle(), r.getType(), r.getUrl()))
            .collect(Collectors.toList());

        return ResponseEntity.ok(new DashboardResponse(
            avgGlobal, 
            avgGlobal, 
            count, 
            certifs, 
            adaptiveRecos
        ));
    }

    /**
     * Récupère les informations de base d'un utilisateur.
     * @param id L'ID de l'utilisateur.
     * @return L'entité User ou une erreur 404.
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
    }

    /**
     * Récupère l'historique (Quiz + Examens) via le service.
     * @param userId L'ID de l'utilisateur.
     * @return Liste d'évènements triés par date.
     */
    @GetMapping("/{userId}/history")
    public ResponseEntity<List<com.cyberscale.backend.dto.HistoryDTO>> getUserHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(quizService.getUserHistory(userId));
    }
}