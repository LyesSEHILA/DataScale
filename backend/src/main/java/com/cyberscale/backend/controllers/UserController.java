package com.cyberscale.backend.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cyberscale.backend.services.QuizService;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    private final QuizService quizService;

    public UserController(QuizService quizService) {
        this.quizService = quizService;
    }
    
    @GetMapping("/{userId}/history")
    public ResponseEntity<List<com.cyberscale.backend.dto.HistoryDTO>> getUserHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(quizService.getUserHistory(userId));
    }
}