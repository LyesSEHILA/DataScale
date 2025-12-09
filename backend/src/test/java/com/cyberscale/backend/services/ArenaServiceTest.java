package com.cyberscale.backend.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cyberscale.backend.dto.ChallengeDTO;
import com.cyberscale.backend.models.Challenge;
import com.cyberscale.backend.models.User;
import com.cyberscale.backend.models.UserChallenge;
import com.cyberscale.backend.repositories.ChallengeRepository;
import com.cyberscale.backend.repositories.UserChallengeRepository;
import com.cyberscale.backend.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class ArenaServiceTest {

    @Mock private ChallengeRepository challengeRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserChallengeRepository userChallengeRepository;

    @InjectMocks private ArenaService arenaService;

    private User mockUser;
    private Challenge mockChallenge;

    @BeforeEach
    void setUp() {
        mockUser = new User("Hacker", "hacker@test.com", "pass");
        mockUser.setId(1L);
        mockUser.setPoints(0);
        mockChallenge = new Challenge("CTF_1", "Test Challenge", "Test Desc", "SECRET_FLAG", 100);
    }

    @Test
    void validateFlag_Success_ShouldAddPoints() {
        when(challengeRepository.findById("CTF_1")).thenReturn(Optional.of(mockChallenge));
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userChallengeRepository.existsByUserIdAndChallengeId(1L, "CTF_1")).thenReturn(false);

        boolean result = arenaService.validateFlag(1L, "CTF_1", "SECRET_FLAG");

        assertTrue(result, "Le flag devrait être validé");
        assertEquals(100, mockUser.getPoints(), "L'utilisateur doit recevoir 100 points");
        verify(userRepository).save(mockUser);
        verify(userChallengeRepository).save(any(UserChallenge.class));
    }

    @Test
    void validateFlag_Failure_ShouldNotAddPoints() {
        // CORRECTION : On doit mocker l'user car le service le cherche AVANT de vérifier le flag
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        
        when(challengeRepository.findById("CTF_1")).thenReturn(Optional.of(mockChallenge));
        when(userChallengeRepository.existsByUserIdAndChallengeId(1L, "CTF_1")).thenReturn(false);

        boolean result = arenaService.validateFlag(1L, "CTF_1", "WRONG_FLAG");

        assertFalse(result, "Le flag devrait être rejeté");
        assertEquals(0, mockUser.getPoints(), "Aucun point ne doit être ajouté");
        verify(userRepository, never()).save(any());
        verify(userChallengeRepository, never()).save(any());
    }

    @Test
    void getAllChallenges_ShouldMarkSolvedOnes() {
        Challenge c1 = new Challenge("C1", "Facile", "Desc", "FLAG", 10);
        Challenge c2 = new Challenge("C2", "Dur", "Desc", "FLAG", 100);
        
        User user = new User(); user.setId(1L);
        UserChallenge victory = new UserChallenge(user, c1); 

        when(challengeRepository.findAll()).thenReturn(List.of(c1, c2));
        when(userChallengeRepository.findByUserId(1L)).thenReturn(List.of(victory));

        List<ChallengeDTO> result = arenaService.getAllChallenges(1L);

        assertEquals(2, result.size());
        assertEquals("C1", result.get(0).id());
        assertTrue(result.get(0).isValidated());
        assertEquals("C2", result.get(1).id());
        assertFalse(result.get(1).isValidated());
    }
}