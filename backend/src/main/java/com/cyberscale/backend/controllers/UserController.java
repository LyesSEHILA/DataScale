package com.cyberscale.backend.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cyberscale.backend.models.User;
import com.cyberscale.backend.repositories.UserRepository;
import com.cyberscale.backend.services.QuizService;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    private final QuizService quizService;
    
    @Autowired
    private UserRepository userRepository; // 1. Injection du repository

    public UserController(QuizService quizService) {
        this.quizService = quizService;
    }
    
    // 2. L'ENDPOINT MANQUANT (Récupère l'user et ses points)
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