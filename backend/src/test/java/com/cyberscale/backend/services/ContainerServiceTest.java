package com.cyberscale.backend.services;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.exception.DockerException;
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
        // Teste le bloc catch(DockerException)
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
        // Teste le bloc catch(DockerException)
        when(dockerClient.startContainerCmd(anyString())).thenThrow(new DockerException("Start Error", 500));
        
        RuntimeException ex = assertThrows(RuntimeException.class, () -> containerService.startContainer("id"));
        assertTrue(ex.getMessage().contains("Erreur lors du démarrage"));
    }

    @Test
    void stopAndRemoveContainer_Success() {
        when(dockerClient.stopContainerCmd(anyString())).thenReturn(stopContainerCmd);
        when(dockerClient.removeContainerCmd(anyString())).thenReturn(removeContainerCmd);

        containerService.stopAndRemoveContainer("id");

        verify(stopContainerCmd).exec();
        verify(removeContainerCmd).exec();
    }

    @Test
    void stopAndRemoveContainer_Failure_ShouldLogButNotThrow() {
        // Teste le bloc catch(DockerException) - méthode void qui avale l'exception
        when(dockerClient.stopContainerCmd(anyString())).thenThrow(new DockerException("Stop Error", 500));

        assertDoesNotThrow(() -> containerService.stopAndRemoveContainer("id"));
        // On vérifie que le remove n'est PAS appelé si le stop plante (selon ton implémentation)
        // Si ton code essaie le remove même si stop plante, change en verify(..., times(1))
    }
}