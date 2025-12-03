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

@Component // Ce bean sera injecté dans le QuizService
public class AdaptiveQuestionGenerator implements QuestionGenerator {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserAnswerRepository userAnswerRepository; // Pour le compteur

    private final int SUCCESS_THRESHOLD = 5; // Limite de réussite pour filtrer une question
    private final int TOTAL_QUESTIONS = 10;

    @Override
    public List<Question> generate(QuizSession session) {
        
        // 1. Calcul des Ratios (Logique Adaptative)
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

        // 2. Récupération, Filtrage et Application du Compteur
        List<Question> allQuestions = questionRepository.findAll();

        // Application du Compteur : on filtre les questions qui ont été trop souvent réussies
        List<Question> validQuestions = allQuestions.stream()
            .filter(q -> {
                // Le filtre ici vérifie combien de fois la question a été réussie
                long successCount = userAnswerRepository.countByQuestionIdAndIsCorrectTrue(q.getId());
                return successCount < SUCCESS_THRESHOLD; 
            })
            .collect(Collectors.toList());

        // 3. Sélection Finale (basée sur la liste filtrée et les ratios)
        List<Question> theoryQuestions = validQuestions.stream()
            .filter(q -> q.getCategorie() == IQuestion.CategorieQuestion.THEORY)            
            .limit(countTheory)
            .collect(Collectors.toList());

        List<Question> techniqueQuestions = validQuestions.stream()
            .filter(q -> q.getCategorie() == IQuestion.CategorieQuestion.TECHNIQUE)
            .limit(countTechnique)
            .collect(Collectors.toList());

        // 4. Assemblage
        theoryQuestions.addAll(techniqueQuestions);
        Collections.shuffle(theoryQuestions);

        return theoryQuestions;
    }
}