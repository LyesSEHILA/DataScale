package com.cyberscale.backend.services;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.exception.NotModifiedException;
import com.github.dockerjava.api.model.Frame;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

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
        lenient().when(createContainerCmd.withEnv(anyString())).thenReturn(createContainerCmd); // Fix varargs
        
        when(createContainerCmd.exec()).thenReturn(createContainerResponse);
        when(createContainerResponse.getId()).thenReturn(CONTAINER_ID);
        when(dockerClient.startContainerCmd(CONTAINER_ID)).thenReturn(startContainerCmd);

        String id = containerService.createChallengeContainer("chall", "flag");
        assertEquals(CONTAINER_ID, id);
    }

    @Test
    void executeCommandSuccess() throws InterruptedException {
        // Arrange
        when(dockerClient.execCreateCmd(anyString())).thenReturn(execCreateCmd);
        when(execCreateCmd.withAttachStdout(true)).thenReturn(execCreateCmd);
        when(execCreateCmd.withAttachStderr(true)).thenReturn(execCreateCmd);
        when(execCreateCmd.withCmd(any(String[].class))).thenReturn(execCreateCmd);
        when(execCreateCmd.exec()).thenReturn(execCreateCmdResponse);
        when(execCreateCmdResponse.getId()).thenReturn("exec-1");

        when(dockerClient.execStartCmd("exec-1")).thenReturn(execStartCmd);

        // Simulation du Callback
        when(execStartCmd.exec(any(ResultCallback.class))).thenAnswer((Answer<ResultCallback>) invocation -> {
            ResultCallback.Adapter<Frame> callback = invocation.getArgument(0);
            callback.onComplete(); // On termine immédiatement pour ne pas bloquer
            return callback;
        });

        // Act
        String res = containerService.executeCommand(CONTAINER_ID, "ls");
        
        // Assert
        assertNotNull(res);
        verify(dockerClient).execCreateCmd(CONTAINER_ID);
    }
}