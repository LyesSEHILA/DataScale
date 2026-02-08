package com.cyberscale.backend.services;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmd;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.ExecStartCmd;
import com.github.dockerjava.api.command.RemoveContainerCmd;
import com.github.dockerjava.api.command.StartContainerCmd;
import com.github.dockerjava.api.command.StopContainerCmd;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.exception.NotModifiedException;
import com.github.dockerjava.core.command.ExecStartResultCallback;

@ExtendWith(MockitoExtension.class)
class ContainerServiceTest {

    private static final String CONTAINER_99 = "container-99";

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
        when(createContainerResponse.getId()).thenReturn("id-123");

        String res = containerService.createContainer("img");
        assertEquals("id-123", res);
    }

    @Test
    void createContainerFailureShouldThrowRuntimeException() {
        when(dockerClient.createContainerCmd(anyString())).thenThrow(new DockerException("Docker Error", 500));
        assertThrows(RuntimeException.class, () -> containerService.createContainer("img"));
    }

    @Test
    void startContainerSuccess() {
        when(dockerClient.startContainerCmd(anyString())).thenReturn(startContainerCmd);
        containerService.startContainer("id");
        verify(startContainerCmd).exec();
    }

    @Test
    void startContainerFailureShouldThrowRuntimeException() {
        when(dockerClient.startContainerCmd(anyString())).thenThrow(new DockerException("Start Error", 500));
        assertThrows(RuntimeException.class, () -> containerService.startContainer("id"));
    }

    @Test
    void createChallengeContainerShouldInjectEnvVar() {
        String challengeId = "linux-challenge";
        String rawFlag = "a1b2c3d4";

        when(dockerClient.createContainerCmd(anyString())).thenReturn(createContainerCmd);
        
        lenient().when(createContainerCmd.withTty(anyBoolean())).thenReturn(createContainerCmd);
        lenient().when(createContainerCmd.withStdinOpen(anyBoolean())).thenReturn(createContainerCmd);
        lenient().when(createContainerCmd.withName(anyString())).thenReturn(createContainerCmd);
        
        lenient().when(createContainerCmd.withEnv(any(String[].class))).thenReturn(createContainerCmd);
        
        when(createContainerCmd.exec()).thenReturn(createContainerResponse);
        when(createContainerResponse.getId()).thenReturn(CONTAINER_99);
        when(dockerClient.startContainerCmd(CONTAINER_99)).thenReturn(startContainerCmd);

        String resultId = containerService.createChallengeContainer(challengeId, rawFlag);

        assertEquals(CONTAINER_99, resultId);

        ArgumentCaptor<String> envCaptor = ArgumentCaptor.forClass(String.class);
        verify(createContainerCmd).withEnv(envCaptor.capture());

        String capturedEnv = envCaptor.getValue();
        assertTrue(capturedEnv.contains("CHALLENGE_FLAG=" + rawFlag));
    }

    @Test
    void stopAndRemoveContainerSuccess() {
        when(dockerClient.stopContainerCmd(anyString())).thenReturn(stopContainerCmd);
        when(dockerClient.removeContainerCmd(anyString())).thenReturn(removeContainerCmd);
        containerService.stopAndRemoveContainer("id");
        verify(stopContainerCmd).exec();
        verify(removeContainerCmd).exec();
    }

    @Test
    void stopAndRemoveContainerWhenAlreadyStopped() {
        when(dockerClient.stopContainerCmd(anyString())).thenReturn(stopContainerCmd);
        when(dockerClient.removeContainerCmd(anyString())).thenReturn(removeContainerCmd);
        doThrow(new NotModifiedException("Stopped")).when(stopContainerCmd).exec();

        containerService.stopAndRemoveContainer("id");
        verify(removeContainerCmd).exec();
    }

    @Test
    void stopAndRemoveContainerWhenStopFailsGeneric() {
        when(dockerClient.stopContainerCmd(anyString())).thenReturn(stopContainerCmd);
        when(dockerClient.removeContainerCmd(anyString())).thenReturn(removeContainerCmd);
        doThrow(new RuntimeException("Crash")).when(stopContainerCmd).exec();

        containerService.stopAndRemoveContainer("id");
        verify(removeContainerCmd).exec();
    }

    @Test
    void stopAndRemoveContainerWhenRemoveFails() {
        when(dockerClient.stopContainerCmd(anyString())).thenReturn(stopContainerCmd);
        when(dockerClient.removeContainerCmd(anyString())).thenReturn(removeContainerCmd);
        doThrow(new RuntimeException("Crash Remove")).when(removeContainerCmd).exec();

        assertDoesNotThrow(() -> containerService.stopAndRemoveContainer("id"));
    }

    @Test
    void executeCommandSuccess() throws InterruptedException {
        when(dockerClient.execCreateCmd(anyString())).thenReturn(execCreateCmd);
        when(execCreateCmd.withAttachStdout(true)).thenReturn(execCreateCmd);
        when(execCreateCmd.withAttachStderr(true)).thenReturn(execCreateCmd);
        when(execCreateCmd.withCmd(any(String[].class))).thenReturn(execCreateCmd);
        when(execCreateCmd.exec()).thenReturn(execCreateCmdResponse);
        when(execCreateCmdResponse.getId()).thenReturn("exec-id-123");

        when(dockerClient.execStartCmd("exec-id-123")).thenReturn(execStartCmd);
        when(execStartCmd.exec(any(ExecStartResultCallback.class))).thenReturn(execStartResultCallback);
        when(execStartResultCallback.awaitCompletion(anyLong(), any(TimeUnit.class))).thenReturn(true);

        String result = containerService.executeCommand("container-id", "ls -la");

        verify(dockerClient).execCreateCmd("container-id");
        verify(dockerClient).execStartCmd("exec-id-123");
        assertNotNull(result); 
    }

    @Test
    void executeCommandFailure() {
        when(dockerClient.execCreateCmd(anyString())).thenThrow(new RuntimeException("Docker Down"));

        String result = containerService.executeCommand("container-id", "ls");
        assertTrue(result.contains("Error executing command"));
    }
}