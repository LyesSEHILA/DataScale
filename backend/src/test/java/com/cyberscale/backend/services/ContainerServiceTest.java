package com.cyberscale.backend.services;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.exception.NotModifiedException;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContainerServiceTest {

    @Mock private DockerClient dockerClient;
    @InjectMocks private ContainerService containerService;

    // Mocks pour les conteneurs (existants)
    @Mock private CreateContainerCmd createContainerCmd;
    @Mock private CreateContainerResponse createContainerResponse;
    @Mock private StartContainerCmd startContainerCmd;
    @Mock private StopContainerCmd stopContainerCmd;
    @Mock private RemoveContainerCmd removeContainerCmd;

    // Mocks pour l'exécution de commandes (Ticket W-02)
    @Mock private ExecCreateCmd execCreateCmd;
    @Mock private ExecCreateCmdResponse execCreateCmdResponse;
    @Mock private ExecStartCmd execStartCmd;
    @Mock private ExecStartResultCallback execStartResultCallback;

    // --- TESTS EXISTANTS ---

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
        assertThrows(RuntimeException.class, () -> containerService.createContainer("img"));
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
        assertThrows(RuntimeException.class, () -> containerService.startContainer("id"));
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
    void stopAndRemoveContainer_WhenAlreadyStopped() {
        when(dockerClient.stopContainerCmd(anyString())).thenReturn(stopContainerCmd);
        when(dockerClient.removeContainerCmd(anyString())).thenReturn(removeContainerCmd);
        doThrow(new NotModifiedException("Stopped")).when(stopContainerCmd).exec();

        containerService.stopAndRemoveContainer("id");
        verify(removeContainerCmd).exec();
    }

    @Test
    void stopAndRemoveContainer_WhenStopFailsGeneric() {
        when(dockerClient.stopContainerCmd(anyString())).thenReturn(stopContainerCmd);
        when(dockerClient.removeContainerCmd(anyString())).thenReturn(removeContainerCmd);
        doThrow(new RuntimeException("Crash")).when(stopContainerCmd).exec();

        containerService.stopAndRemoveContainer("id");
        verify(removeContainerCmd).exec();
    }

    @Test
    void stopAndRemoveContainer_WhenRemoveFails() {
        when(dockerClient.stopContainerCmd(anyString())).thenReturn(stopContainerCmd);
        when(dockerClient.removeContainerCmd(anyString())).thenReturn(removeContainerCmd);
        doThrow(new RuntimeException("Crash Remove")).when(removeContainerCmd).exec();

        assertDoesNotThrow(() -> containerService.stopAndRemoveContainer("id"));
    }

    // --- NOUVEAUX TESTS (Ticket W-02 : executeCommand) ---

    @Test
    void executeCommand_Success() throws InterruptedException {
        // 1. Mock de la création de la commande (execCreate)
        when(dockerClient.execCreateCmd(anyString())).thenReturn(execCreateCmd);
        when(execCreateCmd.withAttachStdout(true)).thenReturn(execCreateCmd);
        when(execCreateCmd.withAttachStderr(true)).thenReturn(execCreateCmd);
        when(execCreateCmd.withCmd(any(String[].class))).thenReturn(execCreateCmd);
        when(execCreateCmd.exec()).thenReturn(execCreateCmdResponse);
        when(execCreateCmdResponse.getId()).thenReturn("exec-id-123");

        // 2. Mock du démarrage de la commande (execStart)
        when(dockerClient.execStartCmd("exec-id-123")).thenReturn(execStartCmd);
        
        // C'est ici l'astuce : exec() renvoie un Callback, sur lequel on appelle awaitCompletion
        when(execStartCmd.exec(any(ExecStartResultCallback.class))).thenReturn(execStartResultCallback);
        when(execStartResultCallback.awaitCompletion(anyLong(), any(TimeUnit.class))).thenReturn(true);

        // WHEN
        String result = containerService.executeCommand("container-id", "ls -la");

        // THEN
        // On vérifie que tout a été appelé
        verify(dockerClient).execCreateCmd("container-id");
        verify(dockerClient).execStartCmd("exec-id-123");
        assertNotNull(result); 
    }

    @Test
    void executeCommand_Failure() {
        // Simulation d'une erreur dès la création de la commande
        when(dockerClient.execCreateCmd(anyString())).thenThrow(new RuntimeException("Docker Down"));

        String result = containerService.executeCommand("container-id", "ls");
        
        // ✅ CORRECTION : On vérifie le message anglais défini dans le service ("Error executing command")
        assertTrue(result.contains("Error executing command"), "Le message d'erreur doit correspondre à celui du service");
    }
}