package com.cyberscale.backend.services;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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

@Service
public class ArenaService {

    @Autowired private ChallengeRepository challengeRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private UserChallengeRepository userChallengeRepository;
    @Autowired private ContainerService containerService;
    
    // Liste des Challenge
    public List<ChallengeDTO> getAllChallenges(Long userId) {
        List<Challenge> challenges = challengeRepository.findAll();
        
        // On récupère les IDs des challenges déjà réussis par cet utilisateur
        Set<String> solvedIds = java.util.Collections.emptySet();
        if (userId != null) {
            solvedIds = userChallengeRepository.findByUserId(userId).stream()
                .map(uc -> uc.getChallenge().getId())
                .collect(Collectors.toSet());
        }
        
        final Set<String> finalSolvedIds = solvedIds; // Pour le lambda

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

    // --- VALIDATION DU FLAG ---
    public boolean validateFlag(Long userId, String challengeId, String submittedFlag) {
        // 1. Vérif Utilisateur
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User introuvable"));

        // 2. Vérif Challenge
        Challenge challenge = challengeRepository.findById(challengeId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge introuvable"));

        // 3. Si déjà validé, on ne fait rien (ou on renvoie true sans donner de points)
        if (userChallengeRepository.existsByUserIdAndChallengeId(userId, challengeId)) {
            return true; // Déjà gagné !
        }

        // 4. Vérif Flag
        if (!challenge.getFlagSecret().equals(submittedFlag.trim())) {
            return false;
        }

        // 5. VICTOIRE : Sauvegarde + Points
        user.addPoints(challenge.getPointsReward());
        userRepository.save(user);

        UserChallenge victory = new UserChallenge(user, challenge);
        userChallengeRepository.save(victory);

        return true;
    }
    
    // Récupérer un challenge
    public ChallengeDTO getChallengeById(String challengeId) {
        Challenge c = challengeRepository.findById(challengeId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge introuvable"));
            
        return new ChallengeDTO(c.getId(), c.getName(), c.getDescription(), c.getPointsReward(), "N/A", false);
    }

    public String startChallengeEnvironment(String challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge inconnu"));

        String imageName = "cyberscale/base-challenge";
        
        String containerId = containerService.createContainer(imageName);
        containerService.startContainer(containerId);

        return containerId;
    }

    // Arrête un environnement
    public void stopChallengeEnvironment(String containerId) {
        containerService.stopAndRemoveContainer(containerId);
    }
}