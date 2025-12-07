package com.cyberscale.backend.controllers;

import com.cyberscale.backend.models.ExamSession;
import com.cyberscale.backend.services.ExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.cyberscale.backend.dto.ExamAnswerRequest;

@RestController
@RequestMapping("/api/exam")
@CrossOrigin(origins = "*")
public class ExamController {

    @Autowired
    private ExamService examService;

    @PostMapping("/start")
    public ResponseEntity<ExamSession> start(@RequestParam String candidateName) {
        return ResponseEntity.ok(examService.startExam(candidateName));
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
}
