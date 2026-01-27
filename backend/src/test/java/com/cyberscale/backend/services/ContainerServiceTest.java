package com.cyberscale.backend.services;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.exception.NotModifiedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContainerServiceTest {

    @Mock private DockerClient dockerClient;
    @InjectMocks private ContainerService containerService;

    // Mocks Fluent API
    @Mock private CreateContainerCmd createContainerCmd;
    @Mock private CreateContainerResponse createContainerResponse;
    @Mock private StartContainerCmd startContainerCmd;
    @Mock private StopContainerCmd stopContainerCmd;
    @Mock private RemoveContainerCmd removeContainerCmd;

    @Test
    void createContainer_Success() {
        when(dockerClient.createContainerCmd(anyString())).thenReturn(createContainerCmd);
        when(createContainerCmd.withTty(anyBoolean())).thenReturn(createContainerCmd);
        when(createContainerCmd.withStdinOpen(anyBoolean())).thenReturn(createContainerCmd);
        when(createContainerCmd.exec()).thenReturn(createContainerResponse);
        when(createContainerResponse.getId()).thenReturn("id-123");

        String res = containerService.createContainer("img");
        assertEquals("id-123", res);
    }

    @Test
    void createContainer_Failure_ShouldThrowRuntimeException() {
        when(dockerClient.createContainerCmd(anyString())).thenThrow(new DockerException("Docker Error", 500));
        
        RuntimeException ex = assertThrows(RuntimeException.class, () -> containerService.createContainer("img"));
        assertTrue(ex.getMessage().contains("Erreur lors de la création"));
    }

    @Test
    void startContainer_Success() {
        when(dockerClient.startContainerCmd(anyString())).thenReturn(startContainerCmd);
        containerService.startContainer("id");
        verify(startContainerCmd).exec();
    }

    @Test
    void startContainer_Failure_ShouldThrowRuntimeException() {
        when(dockerClient.startContainerCmd(anyString())).thenThrow(new DockerException("Start Error", 500));
        
        RuntimeException ex = assertThrows(RuntimeException.class, () -> containerService.startContainer("id"));
        assertTrue(ex.getMessage().contains("Erreur lors du démarrage"));
    }

    @Test
    void stopAndRemoveContainer_Success() {
        // Setup des mocks pour le cas nominal
        when(dockerClient.stopContainerCmd(anyString())).thenReturn(stopContainerCmd);
        when(dockerClient.removeContainerCmd(anyString())).thenReturn(removeContainerCmd);

        containerService.stopAndRemoveContainer("id");

        verify(stopContainerCmd).exec();
        verify(removeContainerCmd).exec();
    }

    // 👇 NOUVEAU TEST CRUCIAL (Pour le coverage du catch NotModifiedException)
    @Test
    void stopAndRemoveContainer_WhenAlreadyStopped_ShouldContinueToRemove() {
        // GIVEN
        when(dockerClient.stopContainerCmd(anyString())).thenReturn(stopContainerCmd);
        when(dockerClient.removeContainerCmd(anyString())).thenReturn(removeContainerCmd);
        
        // Simule que le conteneur est déjà arrêté (Error 304)
        doThrow(new NotModifiedException("Container already stopped")).when(stopContainerCmd).exec();

        // WHEN
        containerService.stopAndRemoveContainer("id");

        // THEN : On vérifie que ça ne plante pas ET que le remove est quand même appelé
        verify(stopContainerCmd).exec();
        verify(removeContainerCmd).exec(); // C'est ça qu'on veut vérifier !
    }

    // 👇 NOUVEAU TEST (Pour le coverage du catch global Exception sur le stop)
    @Test
    void stopAndRemoveContainer_WhenStopFailsGeneric_ShouldContinueToRemove() {
        // GIVEN
        when(dockerClient.stopContainerCmd(anyString())).thenReturn(stopContainerCmd);
        when(dockerClient.removeContainerCmd(anyString())).thenReturn(removeContainerCmd);

        // Simule une erreur inconnue sur le stop
        doThrow(new RuntimeException("Crash docker")).when(stopContainerCmd).exec();

        // WHEN
        containerService.stopAndRemoveContainer("id");

        // THEN : On force quand même la suppression
        verify(removeContainerCmd).exec();
    }
    
    // 👇 NOUVEAU TEST (Pour le coverage du catch global Exception sur le remove)
    @Test
    void stopAndRemoveContainer_WhenRemoveFails_ShouldNotThrow() {
        // GIVEN
        when(dockerClient.stopContainerCmd(anyString())).thenReturn(stopContainerCmd);
        when(dockerClient.removeContainerCmd(anyString())).thenReturn(removeContainerCmd);

        // Le stop marche, mais le remove plante
        doThrow(new RuntimeException("Crash remove")).when(removeContainerCmd).exec();

        // WHEN & THEN : Pas d'exception levée (le service avale l'erreur)
        assertDoesNotThrow(() -> containerService.stopAndRemoveContainer("id"));
    }
}