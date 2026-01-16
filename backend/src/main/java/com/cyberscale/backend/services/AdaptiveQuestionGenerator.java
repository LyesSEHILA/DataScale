package com.cyberscale.backend.services;

import com.cyberscale.backend.models.Question;
import com.cyberscale.backend.models.IQuestion;
import com.cyberscale.backend.models.QuizSession;
import com.cyberscale.backend.repositories.QuestionRepository;
import com.cyberscale.backend.repositories.UserAnswerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Générateur de questions adaptatif.
 * Il sélectionne des questions en fonction du profil d'auto-évaluation de l'utilisateur
 * et filtre celles qui sont statistiquement trop faciles.
 */
@Component
public class AdaptiveQuestionGenerator implements QuestionGenerator {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserAnswerRepository userAnswerRepository;

    private final int SUCCESS_THRESHOLD = 5;
    private final int TOTAL_QUESTIONS = 10;

    @Override
    public List<Question> generate(QuizSession session) {
        
        Long theoryScore = session.getSelfEvalTheory();
        Long techniqueScore = session.getSelfEvalTechnique();
        int countTheory, countTechnique;

        if (theoryScore == 0 && techniqueScore == 0) {
            countTheory = TOTAL_QUESTIONS / 2;
            countTechnique = TOTAL_QUESTIONS - countTheory;
        } else {
            Long totalUserScore = theoryScore + techniqueScore;
            double ratioTheory = (double) theoryScore / totalUserScore;
            countTheory = (int) Math.round(ratioTheory * TOTAL_QUESTIONS);
            countTechnique = TOTAL_QUESTIONS - countTheory;
        }

        List<Question> allQuestions = questionRepository.findAll();

        List<Question> validQuestions = allQuestions.stream()
            .filter(q -> {
                long successCount = userAnswerRepository.countByQuestionIdAndSelectedOptionIsCorrectTrue(q.getId());
                return successCount < SUCCESS_THRESHOLD; 
            })
            .collect(Collectors.toList());

        List<Question> theoryQuestions = validQuestions.stream()
            .filter(q -> q.getCategorie() == IQuestion.CategorieQuestion.THEORY)            
            .limit(countTheory)
            .collect(Collectors.toList());

        List<Question> techniqueQuestions = validQuestions.stream()
            .filter(q -> q.getCategorie() == IQuestion.CategorieQuestion.TECHNIQUE)
            .limit(countTechnique)
            .collect(Collectors.toList());

        theoryQuestions.addAll(techniqueQuestions);
        Collections.shuffle(theoryQuestions);

        return theoryQuestions;
    }
}