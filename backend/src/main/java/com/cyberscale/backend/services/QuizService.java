package com.cyberscale.backend.services;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.cyberscale.backend.dto.OnboardingRequest;
import com.cyberscale.backend.dto.ResultsResponse;
import com.cyberscale.backend.dto.UserAnswerRequest;
import com.cyberscale.backend.models.AnswerOption;
import com.cyberscale.backend.models.ExamSession;
import com.cyberscale.backend.models.IQuestion;
import com.cyberscale.backend.models.Question;
import com.cyberscale.backend.models.QuizSession;
import com.cyberscale.backend.models.Recommendation;
import com.cyberscale.backend.models.UserAnswer;
import com.cyberscale.backend.repositories.AnswerOptionRepository;
import com.cyberscale.backend.repositories.ExamSessionRepository;
import com.cyberscale.backend.repositories.QuestionRepository;
import com.cyberscale.backend.repositories.QuizSessionRepository;
import com.cyberscale.backend.repositories.RecommendationRepository;
import com.cyberscale.backend.repositories.UserAnswerRepository;
import com.cyberscale.backend.repositories.UserRepository;
import jakarta.transaction.Transactional;
import java.util.Map;

/**
 * Service gérant la logique des Quiz d'évaluation initiale et de l'historique utilisateur.
 * Il orchestre la génération de questions, le calcul des scores et les recommandations.
 */
@Service
public class QuizService {

    @Autowired private QuizSessionRepository quizSessionRepository;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private AnswerOptionRepository answerOptionRepository;
    @Autowired private UserAnswerRepository userAnswerRepository;
    @Autowired private RecommendationRepository recommendationRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ExamSessionRepository examSessionRepository;
    
    @Autowired 
    private QuestionGenerator questionGenerator;


    /**
     * Génère ou récupère les questions pour une session de quiz donnée.
     * @param sessionId L'ID de la session.
     * @return Une liste de questions adaptées au profil.
     */
    public List<Question> getQuestionsForSession(Long sessionId) {
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session introuvable"));

        return questionGenerator.generate(session);
    }

    /**
     * Enregistre la réponse d'un utilisateur à une question.
     * @param request DTO contenant l'ID de session, de question et de réponse choisie.
     */
    public void saveUserAnswer(UserAnswerRequest request) {
        QuizSession session = quizSessionRepository.findById(request.sessionId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session introuvable"));
        Question question = questionRepository.findById(request.questionId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question introuvable"));
        AnswerOption selectedOption = answerOptionRepository.findById(request.answerOptionId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Réponse introuvable"));

        UserAnswer userAnswer = new UserAnswer(session, question, selectedOption);
        userAnswerRepository.save(userAnswer);
    }

    /**
     * Calcule les scores finaux et génère des recommandations.
     * @param sessionId L'ID de la session à clôturer.
     * @return Les scores calculés et la liste des recommandations pédagogiques.
     */
    public ResultsResponse calculateAndGetResults(Long sessionId) {
        QuizSession session = quizSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session introuvable"));

        List<UserAnswer> answers = userAnswerRepository.findBySessionId(sessionId);

        if (answers.isEmpty()) {
            return new ResultsResponse(0.0, 0.0, Collections.emptyList());
        }

        int theoryTotal = 0; int theoryCorrect = 0;
        int techTotal = 0; int techCorrect = 0;

        for (UserAnswer answer : answers) {
            Question q = answer.getQuestion();
            if (answer.getSelectedOption() != null) {
                boolean isCorrect = answer.getSelectedOption().getIsCorrect();
                
                if (q.getCategorie() == IQuestion.CategorieQuestion.THEORY) {
                    theoryTotal++;
                    if (isCorrect) theoryCorrect++;
                } else {
                    techTotal++;
                    if (isCorrect) techCorrect++;
                }
            }
        }

        Double finalScoreTheory = (theoryTotal == 0) ? 0.0 : ((double) theoryCorrect / theoryTotal) * 10.0;
        Double finalScoreTechnique = (techTotal == 0) ? 0.0 : ((double) techCorrect / techTotal) * 10.0;

        session.setFinalScoreTheory(finalScoreTheory);
        session.setFinalScoreTechnique(finalScoreTechnique);
        quizSessionRepository.save(session);

        String targetProfile;
        if (finalScoreTechnique < 5) targetProfile = "LOW_TECH";
        else if (finalScoreTheory < 5) targetProfile = "LOW_THEORY";
        else targetProfile = "HIGH_ALL";

        List<Recommendation> recommendations = recommendationRepository.findByTargetProfile(targetProfile);
        return new ResultsResponse(finalScoreTheory, finalScoreTechnique, recommendations);
    }

    /**
     * Initialise une nouvelle session de quiz.
     * @param request Données d'auto-évaluation et ID utilisateur.
     * @return La session créée.
     */
    public QuizSession createSession(OnboardingRequest request) {
        QuizSession newSession = new QuizSession();
        newSession.setAge(request.age());
        newSession.setSelfEvalTheory(request.selfEvalTheory());
        newSession.setSelfEvalTechnique(request.selfEvalTechnique());

        if (request.userId() != null) {
            userRepository.findById(request.userId()).ifPresent(user -> {
                newSession.setUser(user);
            });
        }

        return quizSessionRepository.save(newSession);
    }
    
    /**
     * Récupère l'historique (Quiz + Examens) d'un utilisateur.
     * @param userId L'ID de l'utilisateur.
     * @return Une liste triée par date décroissante d'événements.
     */
    public List<com.cyberscale.backend.dto.HistoryDTO> getUserHistory(Long userId) {
        List<com.cyberscale.backend.dto.HistoryDTO> history = new java.util.ArrayList<>();

        List<QuizSession> quizzes = quizSessionRepository.findByUserIdOrderByCreatedAtDesc(userId);
        for (QuizSession q : quizzes) {
            Double theory = q.getFinalScoreTheory() != null ? q.getFinalScoreTheory() : 0.0;
            Double tech = q.getFinalScoreTechnique() != null ? q.getFinalScoreTechnique() : 0.0;
            
            history.add(new com.cyberscale.backend.dto.HistoryDTO(
                q.getId(),
                "QUIZ",
                "Évaluation Initiale",
                (int) ((theory + tech) / 2),
                10,
                q.getCreatedAt(),
                "Terminé"
            ));
        }

        List<ExamSession> exams = examSessionRepository.findByUserId(userId);
        for (ExamSession e : exams) {
            Integer finalScore = e.getFinalScore() != null ? e.getFinalScore() : 0;
            Integer maxScore = e.getMaxPossibleScore() != null ? e.getMaxPossibleScore() : 0;

            String status = "En cours";
            if (maxScore > 0) {
                double percent = (double) finalScore / maxScore;
                status = percent >= 0.7 ? "Validé" : "Échoué";
            }

            String title = "Certification Blanche";
            if ("CEH".equals(e.getExamRef())) title = "CEH v12 Simulator";
            else if ("SEC_PLUS".equals(e.getExamRef())) title = "CompTIA Security+";
            else if ("CISSP".equals(e.getExamRef())) title = "CISSP Manager";

            history.add(new com.cyberscale.backend.dto.HistoryDTO(
                e.getId(),
                "EXAMEN",
                title,      
                finalScore, 
                maxScore,   
                e.getStartTime(), 
                status     
            ));
        }

        history.sort((a, b) -> b.date().compareTo(a.date()));

        return history;
    }

}