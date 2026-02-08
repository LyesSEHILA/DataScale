package com.cyberscale.backend.services;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.cyberscale.backend.dto.ChallengeDTO;
import com.cyberscale.backend.models.Challenge;
import com.cyberscale.backend.models.User;
import com.cyberscale.backend.models.UserChallenge;
import com.cyberscale.backend.repositories.ChallengeRepository;
import com.cyberscale.backend.repositories.UserChallengeRepository;
import com.cyberscale.backend.repositories.UserRepository;

/**
 * Service gérant la logique métier de l'arène de challenges.
 * Il s'occupe de la liste des épreuves, de la validation des flags et
 * de l'orchestration des environnements Docker via ContainerService.
 */
@Service
public class ArenaService {

    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final ContainerService containerService;

    // 👇 NOUVEAU : Stockage temporaire des flags dynamiques (UserID -> Flag)
    private final Map<Long, String> activeUserFlags = new ConcurrentHashMap<>();

    public ArenaService(ChallengeRepository challengeRepository, 
                        UserRepository userRepository,
                        UserChallengeRepository userChallengeRepository,
                        ContainerService containerService) {
        this.challengeRepository = challengeRepository;
        this.userRepository = userRepository;
        this.userChallengeRepository = userChallengeRepository;
        this.containerService = containerService;
    }
    
    public List<ChallengeDTO> getAllChallenges(Long userId) {
        List<Challenge> challenges = challengeRepository.findAll();
        
        Set<String> solvedIds = java.util.Collections.emptySet();
        if (userId != null) {
            solvedIds = userChallengeRepository.findByUserId(userId).stream()
                .map(uc -> uc.getChallenge().getId())
                .collect(Collectors.toSet());
        }
        
        final Set<String> finalSolvedIds = solvedIds;

        return challenges.stream().map(c -> {
            String diff = "FACILE";
            if (c.getPointsReward() >= 100) diff = "HARDCORE";
            else if (c.getPointsReward() >= 50) diff = "MOYEN";

            boolean isDone = finalSolvedIds.contains(c.getId());

            return new ChallengeDTO(
                c.getId(),
                c.getName(),
                c.getDescription(),
                c.getPointsReward(),
                diff,
                isDone
            );
        }).collect(Collectors.toList());
    }

    /**
     * Tente de valider un flag soumis par un utilisateur.
     * Vérifie d'abord le flag dynamique, puis le flag statique en fallback.
     */
    public boolean validateFlag(Long userId, String challengeId, String submittedFlag) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User introuvable"));

        Challenge challenge = challengeRepository.findById(challengeId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge introuvable"));

        // Déjà validé ?
        if (userChallengeRepository.existsByUserIdAndChallengeId(userId, challengeId)) {
            return true;
        }

        String cleanedFlag = submittedFlag.trim();
        boolean isCorrect = false;

        // 1. Vérification du Flag Dynamique (Prioritaire)
        if (activeUserFlags.containsKey(userId)) {
            String expectedDynamicFlag = activeUserFlags.get(userId);
            if (expectedDynamicFlag.equals(cleanedFlag)) {
                isCorrect = true;
                // On retire le flag de la mémoire pour nettoyer
                activeUserFlags.remove(userId);
            }
        }

        // 2. Fallback : Vérification du Flag Statique (Base de données)
        // Utile si le conteneur a redémarré ou pour les tests
        if (!isCorrect && challenge.getFlagSecret().equals(cleanedFlag)) {
            isCorrect = true;
        }

        if (isCorrect) {
            user.addPoints(challenge.getPointsReward());
            userRepository.save(user);

            UserChallenge victory = new UserChallenge(user, challenge);
            userChallengeRepository.save(victory);
            return true;
        }

        return false;
    }
    
    public ChallengeDTO getChallengeById(String challengeId) {
        Challenge c = challengeRepository.findById(challengeId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge introuvable"));
            
        return new ChallengeDTO(c.getId(), c.getName(), c.getDescription(), c.getPointsReward(), "N/A", false);
    }

    /**
     * Démarre un environnement Docker avec un FLAG DYNAMIQUE UNIQUE.
     * @param userId L'ID de l'utilisateur qui lance le challenge.
     * @param challengeId L'ID du challenge.
     */
    public String startChallengeEnvironment(Long userId, String challengeId) {
        challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge inconnu"));

        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String dynamicFlag = uniqueId ; 

        activeUserFlags.put(userId, dynamicFlag);
        return containerService.createChallengeContainer(challengeId, dynamicFlag);
    }

    public void stopChallengeEnvironment(String containerId) {
        containerService.stopAndRemoveContainer(containerId);
    }
}