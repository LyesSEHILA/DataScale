package com.cyberscale.backend.services;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.cyberscale.backend.models.Challenge;
import com.cyberscale.backend.models.User;
import com.cyberscale.backend.repositories.ChallengeRepository;
import com.cyberscale.backend.repositories.UserChallengeRepository;
import com.cyberscale.backend.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class ArenaServiceTest {

    @Mock private ChallengeRepository challengeRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserChallengeRepository userChallengeRepository;
    @Mock private ContainerService containerService;

    @InjectMocks private ArenaService arenaService;

    // --- TESTS EXISTANTS (MIS À JOUR) ---

    @Test
    void validateFlag_Success_Static() {
        // Teste le cas "Fallback" (Flag statique en base)
        User u = new User();
        u.setId(1L);
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
        u.setId(1L);
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

    // --- NOUVEAUX TESTS (FLAGS DYNAMIQUES) ---

    @Test
    void startChallengeEnvironment_ShouldGenerateRawStringFlag() {
        // GIVEN
        Long userId = 1L;
        String challengeId = "C1";
        Challenge challenge = new Challenge(challengeId, "Linux", "Desc", "STATIC", 100);

        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));
        // Note : startChallengeEnvironment prend maintenant userId en 1er paramètre
        when(containerService.createChallengeContainer(eq(challengeId), anyString())).thenReturn("container-123");

        // WHEN
        String containerId = arenaService.startChallengeEnvironment(userId, challengeId);

        // THEN
        assertEquals("container-123", containerId);

        // Capture le flag généré pour vérifier son format
        ArgumentCaptor<String> flagCaptor = ArgumentCaptor.forClass(String.class);
        verify(containerService).createChallengeContainer(eq(challengeId), flagCaptor.capture());

        String generatedFlag = flagCaptor.getValue();
        assertNotNull(generatedFlag);
        assertEquals(8, generatedFlag.length(), "Le flag doit faire 8 caractères (UUID partiel)");
        assertFalse(generatedFlag.startsWith("CTF{"), "Le flag NE DOIT PAS commencer par CTF{ (Format Brut)");
        
        System.out.println("Flag généré testé : " + generatedFlag);
    }

    @Test
    void validateFlag_ShouldAcceptDynamicFlag() {
        // 1. Setup
        Long userId = 1L;
        String challengeId = "C1";
        User user = new User();
        user.setId(userId);
        Challenge challenge = new Challenge(challengeId, "Linux", "Desc", "STATIC_FLAG", 100);

        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(containerService.createChallengeContainer(anyString(), anyString())).thenReturn("id");

        // 2. Lancement du challenge (Génération du flag en mémoire)
        arenaService.startChallengeEnvironment(userId, challengeId);
        
        // On récupère le flag qui a été généré secrètement
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(containerService).createChallengeContainer(anyString(), captor.capture());
        String secretFlag = captor.getValue();

        // 3. Validation avec ce flag
        boolean result = arenaService.validateFlag(userId, challengeId, secretFlag);
        
        assertTrue(result, "Le flag dynamique brut doit être accepté");
        verify(userRepository).save(user); // Points attribués
    }

    @Test
    void startChallengeEnvironment_NotFound() {
        when(challengeRepository.findById("C1")).thenReturn(Optional.empty());
        // Mise à jour de la signature : ajout de 1L (userId)
        assertThrows(ResponseStatusException.class, () -> arenaService.startChallengeEnvironment(1L, "C1"));
    }
}