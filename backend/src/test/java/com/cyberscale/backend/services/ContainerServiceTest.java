package com.cyberscale.backend.services;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.Frame;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ContainerServiceTest {

    @Mock
    private DockerClient dockerClient;

    @Mock
    private CreateContainerCmd createContainerCmd;

    @Mock
    private CreateContainerResponse createContainerResponse;

    @Mock
    private StartContainerCmd startContainerCmd;

    @Mock
    private ExecCreateCmd execCreateCmd;

    @Mock
    private ExecCreateCmdResponse execCreateCmdResponse;

    @Mock
    private ExecStartCmd execStartCmd;

    @InjectMocks
    private ContainerService containerService;

    @Test
    void createContainer_Success() {
        // ARRANGE
        when(dockerClient.createContainerCmd(anyString())).thenReturn(createContainerCmd);
        when(createContainerCmd.withTty(true)).thenReturn(createContainerCmd);
        when(createContainerCmd.withStdinOpen(true)).thenReturn(createContainerCmd);
        when(createContainerCmd.exec()).thenReturn(createContainerResponse);
        when(createContainerResponse.getId()).thenReturn("container123");

        // ACT
        String containerId = containerService.createContainer("ubuntu:latest");

        // ASSERT
        assertEquals("container123", containerId);
    }

    @Test
    void executeCommand_Success() throws InterruptedException {
        // ARRANGE
        String containerId = "container123";
        String command = "echo hello";

        // Mock de la création de commande EXEC
        when(dockerClient.execCreateCmd(containerId)).thenReturn(execCreateCmd);
        when(execCreateCmd.withAttachStdout(true)).thenReturn(execCreateCmd);
        when(execCreateCmd.withAttachStderr(true)).thenReturn(execCreateCmd);
        when(execCreateCmd.withCmd(any(String[].class))).thenReturn(execCreateCmd);
        when(execCreateCmd.exec()).thenReturn(execCreateCmdResponse);
        when(execCreateCmdResponse.getId()).thenReturn("exec123");

        // Mock du démarrage de commande EXEC
        when(dockerClient.execStartCmd("exec123")).thenReturn(execStartCmd);
        

        when(execStartCmd.exec(any(ResultCallback.class))).thenAnswer(invocation -> {
            ResultCallback.Adapter<Frame> callback = invocation.getArgument(0);
            callback.onComplete(); 
            return callback;
        });

        // ACT
        String result = containerService.executeCommand(containerId, command);

        // ASSERT
        assertNotNull(result); 
        verify(dockerClient).execCreateCmd(containerId);
    }
}