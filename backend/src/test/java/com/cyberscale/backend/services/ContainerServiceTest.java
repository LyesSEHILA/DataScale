package com.cyberscale.backend.services;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.exception.NotModifiedException;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContainerServiceTest {

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
    void createChallengeContainerSuccess() {
        when(dockerClient.createContainerCmd(anyString())).thenReturn(createContainerCmd);
        lenient().when(createContainerCmd.withTty(anyBoolean())).thenReturn(createContainerCmd);
        lenient().when(createContainerCmd.withStdinOpen(anyBoolean())).thenReturn(createContainerCmd);
        lenient().when(createContainerCmd.withEnv(any(String[].class))).thenReturn(createContainerCmd);
        when(createContainerCmd.exec()).thenReturn(createContainerResponse);
        when(createContainerResponse.getId()).thenReturn("c99");
        when(dockerClient.startContainerCmd("c99")).thenReturn(startContainerCmd);

        String id = containerService.createChallengeContainer("chal1", "flag");
        assertEquals("c99", id);
    }

    @Test
    void stopAndRemoveContainerFullExceptionCoverage() {
        when(dockerClient.stopContainerCmd(anyString())).thenReturn(stopContainerCmd);
        when(dockerClient.removeContainerCmd(anyString())).thenReturn(removeContainerCmd);

        // 1. Exception générique sur STOP
        doThrow(new RuntimeException("Stop Fail")).when(stopContainerCmd).exec();
        // 2. Exception générique sur REMOVE
        doThrow(new RuntimeException("Remove Fail")).when(removeContainerCmd).exec();

        assertDoesNotThrow(() -> containerService.stopAndRemoveContainer("id"));
        
        verify(stopContainerCmd).exec();
        verify(removeContainerCmd).exec();
    }

    @Test
    void stopAndRemoveContainerNotModified() {
        when(dockerClient.stopContainerCmd(anyString())).thenReturn(stopContainerCmd);
        when(dockerClient.removeContainerCmd(anyString())).thenReturn(removeContainerCmd);
        
        // Couverture spécifique de NotModifiedException
        doThrow(new NotModifiedException("Already stopped")).when(stopContainerCmd).exec();

        containerService.stopAndRemoveContainer("id");
        verify(removeContainerCmd).exec();
    }

    @Test
    void executeCommandGenericException() {
        // Simuler une exception inattendue (NullPointer par exemple)
        when(dockerClient.execCreateCmd(anyString())).thenThrow(new NullPointerException("Surprise"));

        String result = containerService.executeCommand("c1", "ls");
        assertEquals("Error executing command", result);
    }
}