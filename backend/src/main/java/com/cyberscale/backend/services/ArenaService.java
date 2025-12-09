package com.cyberscale.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.cyberscale.backend.models.Challenge;
import com.cyberscale.backend.models.User;
import com.cyberscale.backend.repositories.ChallengeRepository;
import com.cyberscale.backend.repositories.UserRepository;

@Service
public class ArenaService {

    @Autowired private ChallengeRepository challengeRepository;
    @Autowired private UserRepository userRepository;

    public boolean validateFlag(Long userId, String challengeId, String submittedFlag) {
        // 1. Récupérer le challenge
        Challenge challenge = challengeRepository.findById(challengeId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge introuvable"));

        // 2. Vérifier le flag (sensible à la casse ou non ? ici strict)
        if (!challenge.getFlagSecret().equals(submittedFlag.trim())) {
            return false; // Mauvais flag
        }

        // 3. Récupérer l'utilisateur
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        // 4. Attribuer les points (TODO: Vérifier si déjà validé pour ne pas spammer les points)
        // Pour cette version simple, on ajoute juste.
        user.addPoints(challenge.getPointsReward());
        userRepository.save(user);

        return true;
    }
}