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

@RestController
@RequestMapping("/api/exam")
@CrossOrigin(origins = "*")
public class ExamController {

    @Autowired
    private ExamService examService;

   @PostMapping("/start")
    public ResponseEntity<ExamSession> start(
            @RequestParam String candidateName, 
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String examRef) { 
        return ResponseEntity.ok(examService.startExam(candidateName, userId, examRef));
    }

    @PostMapping("/answer")
    public ResponseEntity<Void> answer(@RequestBody ExamAnswerRequest request) {
        examService.submitExamAnswer(request.sessionId(), request.questionId(), request.optionId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/finish/{sessionId}")
    public ResponseEntity<ExamSession> finish(@PathVariable Long sessionId) {
        return ResponseEntity.ok(examService.finishExam(sessionId));
    }

    @GetMapping("/{sessionId}/questions")
    public ResponseEntity<List<com.cyberscale.backend.models.Question>> getQuestions(@PathVariable Long sessionId) {
        return ResponseEntity.ok(examService.getExamQuestions(sessionId));
    }

    @GetMapping("/{sessionId}/status")
    public ResponseEntity<?> getExamStatus(@PathVariable Long sessionId) {
        return ResponseEntity.ok(examService.getExamStatus(sessionId));
    }
    
}
