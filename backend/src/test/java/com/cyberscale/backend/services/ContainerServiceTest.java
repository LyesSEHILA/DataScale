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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContainerServiceTest {

    @Mock private DockerClient dockerClient;
    @InjectMocks private ContainerService containerService;

    // Mocks pour les conteneurs
    @Mock private CreateContainerCmd createContainerCmd;
    @Mock private CreateContainerResponse createContainerResponse;
    @Mock private StartContainerCmd startContainerCmd;
    @Mock private StopContainerCmd stopContainerCmd;
    @Mock private RemoveContainerCmd removeContainerCmd;

    // Mocks pour l'exécution de commandes
    @Mock private ExecCreateCmd execCreateCmd;
    @Mock private ExecCreateCmdResponse execCreateCmdResponse;
    @Mock private ExecStartCmd execStartCmd;
    @Mock private ExecStartResultCallback execStartResultCallback;

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

    // --- Test Manquant Ajouté ---
    @Test
    void startChallengeEnvironment_Success() {
        // Cette méthode appelle createContainer puis startContainer
        // On doit mocker la chaîne complète
        
        // 1. Mock de createContainer
        when(dockerClient.createContainerCmd("cyberscale/base-challenge")).thenReturn(createContainerCmd);
        when(createContainerCmd.withTty(anyBoolean())).thenReturn(createContainerCmd);
        when(createContainerCmd.withStdinOpen(anyBoolean())).thenReturn(createContainerCmd);
        when(createContainerCmd.exec()).thenReturn(createContainerResponse);
        when(createContainerResponse.getId()).thenReturn("new-env-id");

        // 2. Mock de startContainer
        when(dockerClient.startContainerCmd("new-env-id")).thenReturn(startContainerCmd);

        // WHEN
        String result = containerService.startChallengeEnvironment("Challenge1");

        // THEN
        assertEquals("new-env-id", result);
        verify(dockerClient).createContainerCmd("cyberscale/base-challenge");
        verify(dockerClient).startContainerCmd("new-env-id");
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

    @Test
    void executeCommand_Success() throws InterruptedException {
        when(dockerClient.execCreateCmd(anyString())).thenReturn(execCreateCmd);
        when(execCreateCmd.withAttachStdout(true)).thenReturn(execCreateCmd);
        when(execCreateCmd.withAttachStderr(true)).thenReturn(execCreateCmd);
        when(execCreateCmd.withCmd(anyString(), anyString(), anyString())).thenReturn(execCreateCmd);
        when(execCreateCmd.exec()).thenReturn(execCreateCmdResponse);
        when(execCreateCmdResponse.getId()).thenReturn("exec-id-123");
        when(dockerClient.execStartCmd("exec-id-123")).thenReturn(execStartCmd);
        
        // On simule le comportement du callback pour éviter un NullPointerException
        when(execStartCmd.exec(any(ExecStartResultCallback.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        String result = containerService.executeCommand("container-id", "ls -la");

        verify(dockerClient).execCreateCmd("container-id");
        verify(dockerClient).execStartCmd("exec-id-123");
        assertNotNull(result); 
    }

    @Test
    void executeCommand_Failure() {
        when(dockerClient.execCreateCmd(anyString())).thenThrow(new RuntimeException("Docker Down"));

        String result = containerService.executeCommand("container-id", "ls");
        
        assertTrue(result.contains("ERREUR D'EXÉCUTION : "), "Le message doit signaler une erreur d'exécution");
    }

    @Test
    void executeCommand_ShouldBlockDangerousCommand() {
        // ARRANGE
        String dangerousCmd = "rm -rf /"; // Commande interdite
        String containerId = "test-container";

        // ACT
        String result = containerService.executeCommand(containerId, dangerousCmd);

        // ASSERT
        // 1. On vérifie qu'on reçoit le message d'alerte
        assertTrue(result.contains("ERREUR : Commande interdite par la politique de sécurité."), "Le service doit bloquer la commande dangereuse");
        
        // 2. CRUCIAL : On vérifie que Docker n'a JAMAIS été appelé
        verify(dockerClient, never()).execCreateCmd(anyString());
    }
}