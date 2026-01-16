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

    public ArenaService(ChallengeRepository challengeRepository, 
                        UserRepository userRepository,
                        UserChallengeRepository userChallengeRepository,
                        ContainerService containerService) {
        this.challengeRepository = challengeRepository;
        this.userRepository = userRepository;
        this.userChallengeRepository = userChallengeRepository;
        this.containerService = containerService;
    }
    
    /**
     * Récupère la liste de tous les challenges disponibles.
     * @param userId L'ID de l'utilisateur connecté.
     * @return Une liste de DTOs prêts pour l'affichage frontend.
     */
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
     * Tente de valider un flag soumis par un utilisateur pour un challenge donné.
     * @param userId L'ID de l'utilisateur.
     * @param challengeId L'ID du challenge.
     * @param submittedFlag Le flag entré par l'utilisateur.
     * @return true si le flag est valide ou déjà validé, false sinon.
     * @throws ResponseStatusException Si l'utilisateur ou le challenge n'existe pas.
     */
    public boolean validateFlag(Long userId, String challengeId, String submittedFlag) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User introuvable"));

        Challenge challenge = challengeRepository.findById(challengeId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge introuvable"));

        if (userChallengeRepository.existsByUserIdAndChallengeId(userId, challengeId)) {
            return true;
        }

        if (!challenge.getFlagSecret().equals(submittedFlag.trim())) {
            return false;
        }

        user.addPoints(challenge.getPointsReward());
        userRepository.save(user);

        UserChallenge victory = new UserChallenge(user, challenge);
        userChallengeRepository.save(victory);

        return true;
    }
    
    /**
     * Récupère les détails d'un challenge spécifique.
     * @param challengeId L'ID du challenge.
     * @return Le DTO du challenge.
     * @throws ResponseStatusException Si le challenge est introuvable.
     */
    public ChallengeDTO getChallengeById(String challengeId) {
        Challenge c = challengeRepository.findById(challengeId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge introuvable"));
            
        return new ChallengeDTO(c.getId(), c.getName(), c.getDescription(), c.getPointsReward(), "N/A", false);
    }

    /**
     * Démarre un environnement Docker pour le challenge.
     * @param challengeId L'ID du challenge.
     * @return L'ID du conteneur Docker créé.
     * @throws ResponseStatusException Si le challenge n'existe pas.
     */
    public String startChallengeEnvironment(String challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge inconnu"));

        String imageName = "cyberscale/base-challenge";
        
        String containerId = containerService.createContainer(imageName);
        containerService.startContainer(containerId);

        return containerId;
    }

    /**
     * Arrête et supprime un environnement de challenge.
     * @param containerId L'ID du conteneur à nettoyer.
     */
    public void stopChallengeEnvironment(String containerId) {
        containerService.stopAndRemoveContainer(containerId);
    }
}