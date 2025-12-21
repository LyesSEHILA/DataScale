package com.cyberscale.backend.services;

import com.cyberscale.backend.models.Challenge;
import com.cyberscale.backend.models.User;
import com.cyberscale.backend.repositories.ChallengeRepository;
import com.cyberscale.backend.repositories.UserChallengeRepository;
import com.cyberscale.backend.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArenaServiceTest {

    @Mock private ChallengeRepository challengeRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserChallengeRepository userChallengeRepository;
    @Mock private ContainerService containerService;

    @InjectMocks private ArenaService arenaService;

    @Test
    void validateFlag_Success() {
        User u = new User();
        // CORRECTION : Utilisation du constructeur car pas de setters
        Challenge c = new Challenge("C1", "Test Challenge", "Desc", "FLAG123", 100);

        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(challengeRepository.findById("C1")).thenReturn(Optional.of(c));
        when(userChallengeRepository.existsByUserIdAndChallengeId(1L, "C1")).thenReturn(false);

        boolean result = arenaService.validateFlag(1L, "C1", "FLAG123");

        assertTrue(result);
        verify(userRepository).save(u); // Points ajoutés
    }

    @Test
    void validateFlag_WrongFlag() {
        User u = new User();
        // CORRECTION : Utilisation du constructeur
        Challenge c = new Challenge("C1", "Test Challenge", "Desc", "FLAG123", 100);

        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(challengeRepository.findById("C1")).thenReturn(Optional.of(c));

        boolean result = arenaService.validateFlag(1L, "C1", "WRONG");

        assertFalse(result);
        verify(userRepository, never()).save(u);
    }

    @Test
    void validateFlag_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> arenaService.validateFlag(1L, "C1", "F"));
    }

    @Test
    void validateFlag_ChallengeNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(challengeRepository.findById("C1")).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> arenaService.validateFlag(1L, "C1", "F"));
    }

    @Test
    void validateFlag_AlreadySolved() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(challengeRepository.findById("C1")).thenReturn(Optional.of(new Challenge("C1", "Name", "Desc", "F", 10)));
        // Déjà résolu
        when(userChallengeRepository.existsByUserIdAndChallengeId(1L, "C1")).thenReturn(true);

        boolean result = arenaService.validateFlag(1L, "C1", "FLAG123");

        assertTrue(result); // Renvoie true
        verify(userRepository, never()).save(any()); // MAIS ne donne pas de points
    }
    
    @Test
    void startChallengeEnvironment_Success() {
        when(challengeRepository.findById("C1")).thenReturn(Optional.of(new Challenge()));
        when(containerService.createContainer(anyString())).thenReturn("container-id");
        
        String id = arenaService.startChallengeEnvironment("C1");
        
        assertEquals("container-id", id);
        verify(containerService).startContainer("container-id");
    }

    @Test
    void startChallengeEnvironment_NotFound() {
        when(challengeRepository.findById("C1")).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> arenaService.startChallengeEnvironment("C1"));
    }
}