package com.cyberscale.backend.services;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.exception.NotModifiedException;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContainerServiceTest {

    private static final String CONTAINER_ID = "container-id";

    @Mock private DockerClient dockerClient;
    @InjectMocks private ContainerService containerService;

    @Mock private CreateContainerCmd createContainerCmd;
    @Mock private CreateContainerResponse createContainerResponse;
    @Mock private StartContainerCmd startContainerCmd;
    @Mock private StopContainerCmd stopContainerCmd;
    @Mock private RemoveContainerCmd removeContainerCmd;
    @Mock private ExecCreateCmd execCreateCmd;
    @Mock private ExecCreateCmdResponse execCreateCmdResponse;
    @Mock private ExecStartCmd execStartCmd;
    @Mock private ExecStartResultCallback execStartResultCallback;

    @Test
    void createContainerSuccess() {
        when(dockerClient.createContainerCmd(anyString())).thenReturn(createContainerCmd);
        when(createContainerCmd.withTty(anyBoolean())).thenReturn(createContainerCmd);
        when(createContainerCmd.withStdinOpen(anyBoolean())).thenReturn(createContainerCmd);
        when(createContainerCmd.exec()).thenReturn(createContainerResponse);
        when(createContainerResponse.getId()).thenReturn(CONTAINER_ID);

        String res = containerService.createContainer("img");
        assertEquals(CONTAINER_ID, res);
    }

    @Test
    void startContainerFailure() {
        when(dockerClient.startContainerCmd(anyString())).thenThrow(new DockerException("Err", 500));
        assertThrows(RuntimeException.class, () -> containerService.startContainer(CONTAINER_ID));
    }

    @Test
    void createChallengeContainerSuccess() {
        when(dockerClient.createContainerCmd(anyString())).thenReturn(createContainerCmd);
        lenient().when(createContainerCmd.withTty(anyBoolean())).thenReturn(createContainerCmd);
        lenient().when(createContainerCmd.withStdinOpen(anyBoolean())).thenReturn(createContainerCmd);
        // Important : matcher le varargs
        lenient().when(createContainerCmd.withEnv(any(String[].class))).thenReturn(createContainerCmd);
        
        when(createContainerCmd.exec()).thenReturn(createContainerResponse);
        when(createContainerResponse.getId()).thenReturn(CONTAINER_ID);
        when(dockerClient.startContainerCmd(CONTAINER_ID)).thenReturn(startContainerCmd);

        String id = containerService.createChallengeContainer("chall", "flag");
        assertEquals(CONTAINER_ID, id);
    }

    @Test
    void stopAndRemoveContainerHandlesExceptions() {
        when(dockerClient.stopContainerCmd(anyString())).thenReturn(stopContainerCmd);
        when(dockerClient.removeContainerCmd(anyString())).thenReturn(removeContainerCmd);

        // Simulation d'erreurs qui doivent être catchées (pas de crash)
        doThrow(new RuntimeException("Stop failed")).when(stopContainerCmd).exec();
        doThrow(new RuntimeException("Remove failed")).when(removeContainerCmd).exec();

        assertDoesNotThrow(() -> containerService.stopAndRemoveContainer(CONTAINER_ID));
    }

    @Test
    void stopAndRemoveContainerHandlesNotModified() {
        when(dockerClient.stopContainerCmd(anyString())).thenReturn(stopContainerCmd);
        when(dockerClient.removeContainerCmd(anyString())).thenReturn(removeContainerCmd);
        doThrow(new NotModifiedException("Already stopped")).when(stopContainerCmd).exec();

        containerService.stopAndRemoveContainer(CONTAINER_ID);
        verify(removeContainerCmd).exec(); // Doit quand même tenter le remove
    }

    @Test
    void executeCommandSecurityCheck() {
        // Teste isCommandDangerous (méthode privée appelée par executeCommand)
        String res = containerService.executeCommand(CONTAINER_ID, "rm -rf /");
        assertTrue(res.contains("blocked"));
    }

    @Test
    void executeCommandSuccess() throws InterruptedException {
        when(dockerClient.execCreateCmd(anyString())).thenReturn(execCreateCmd);
        when(execCreateCmd.withAttachStdout(true)).thenReturn(execCreateCmd);
        when(execCreateCmd.withAttachStderr(true)).thenReturn(execCreateCmd);
        when(execCreateCmd.withCmd(any(String[].class))).thenReturn(execCreateCmd);
        when(execCreateCmd.exec()).thenReturn(execCreateCmdResponse);
        when(execCreateCmdResponse.getId()).thenReturn("exec-1");

        when(dockerClient.execStartCmd("exec-1")).thenReturn(execStartCmd);
        when(execStartCmd.exec(any())).thenReturn(execStartResultCallback);
        when(execStartResultCallback.awaitCompletion(anyLong(), any())).thenReturn(true);

        String res = containerService.executeCommand(CONTAINER_ID, "ls");
        assertNotNull(res);
    }

    @Test
    void executeCommandFailure() {
        when(dockerClient.execCreateCmd(anyString())).thenThrow(new RuntimeException("Docker HS"));
        String res = containerService.executeCommand(CONTAINER_ID, "ls");
        assertTrue(res.contains("Error executing command"));
    }
    
    @Test
    void startChallengeEnvironmentDeprecated() {
        // Couverture de l'ancienne méthode dépréciée
        when(dockerClient.createContainerCmd(anyString())).thenReturn(createContainerCmd);
        lenient().when(createContainerCmd.withTty(anyBoolean())).thenReturn(createContainerCmd);
        lenient().when(createContainerCmd.withStdinOpen(anyBoolean())).thenReturn(createContainerCmd);
        lenient().when(createContainerCmd.withEnv(any(String[].class))).thenReturn(createContainerCmd);
        when(createContainerCmd.exec()).thenReturn(createContainerResponse);
        when(createContainerResponse.getId()).thenReturn("legacy-id");
        when(dockerClient.startContainerCmd("legacy-id")).thenReturn(startContainerCmd);

        String id = containerService.startChallengeEnvironment("chall");
        assertEquals("legacy-id", id);
    }
}