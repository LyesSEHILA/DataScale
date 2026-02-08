package com.cyberscale.backend.services;

import com.cyberscale.backend.dto.ChallengeDTO;
import com.cyberscale.backend.models.Challenge;
import com.cyberscale.backend.models.User;
import com.cyberscale.backend.models.UserChallenge;
import com.cyberscale.backend.repositories.ChallengeRepository;
import com.cyberscale.backend.repositories.UserChallengeRepository;
import com.cyberscale.backend.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArenaServiceTest {

    @Mock private ContainerService containerService;
    @Mock private UserRepository userRepository;
    @Mock private ChallengeRepository challengeRepository;
    @Mock private UserChallengeRepository userChallengeRepository;

    @InjectMocks private ArenaService arenaService;

    @Test
    void getAllChallengesShouldMapDifficultiesCorrectly() {
        // GIVEN: 3 challenges avec des points différents pour tester les IFs
        Challenge c1 = new Challenge("1", "Easy", "Desc", "F", 10);
        Challenge c2 = new Challenge("2", "Medium", "Desc", "F", 60);
        Challenge c3 = new Challenge("3", "Hard", "Desc", "F", 150);

        when(challengeRepository.findAll()).thenReturn(List.of(c1, c2, c3));
        when(userChallengeRepository.findByUserId(anyLong())).thenReturn(List.of());

        // WHEN
        List<ChallengeDTO> result = arenaService.getAllChallenges(1L);

        // THEN
        assertEquals("FACILE", result.get(0).difficulty());
        assertEquals("MOYEN", result.get(1).difficulty());
        assertEquals("HARDCORE", result.get(2).difficulty());
    }

    @Test
    void getChallengeByIdNotFound() {
        when(challengeRepository.findById("unknown")).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> arenaService.getChallengeById("unknown"));
    }
    
    @Test
    void getChallengeByIdSuccess() {
        Challenge c = new Challenge("1", "C", "D", "F", 10);
        when(challengeRepository.findById("1")).thenReturn(Optional.of(c));
        assertNotNull(arenaService.getChallengeById("1"));
    }

    @Test
    void startChallengeEnvironmentNotFound() {
        when(challengeRepository.findById("unknown")).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> arenaService.startChallengeEnvironment(1L, "unknown"));
    }

    @Test
    void validateFlagStaticFallbackSuccess() {
        // Teste le cas où le flag dynamique est absent mais le flag statique est bon
        User user = new User(); user.setId(1L);
        Challenge challenge = new Challenge("c1", "Linux", "Easy", "STATIC_FLAG", 100);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(challengeRepository.findById("c1")).thenReturn(Optional.of(challenge));
        when(userChallengeRepository.existsByUserIdAndChallengeId(1L, "c1")).thenReturn(false);

        // Pas de flag dynamique dans la map (cas par défaut)
        boolean result = arenaService.validateFlag(1L, "c1", "STATIC_FLAG");

        assertTrue(result);
    }
    
    @Test
    void validateFlagAlreadyDone() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(challengeRepository.findById("c1")).thenReturn(Optional.of(new Challenge()));
        when(userChallengeRepository.existsByUserIdAndChallengeId(1L, "c1")).thenReturn(true);
        
        assertTrue(arenaService.validateFlag(1L, "c1", "ANY"));
    }
}