package com.cyberscale.backend.services;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.RemoveContainerCmd;
import com.github.dockerjava.api.command.StartContainerCmd;
import com.github.dockerjava.api.command.StopContainerCmd;
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

    // Mocks spécifiques
    @Mock private CreateContainerCmd createContainerCmd;
    @Mock private CreateContainerResponse createContainerResponse;
    @Mock private StartContainerCmd startContainerCmd;
    @Mock private StopContainerCmd stopContainerCmd;
    @Mock private RemoveContainerCmd removeContainerCmd;

    @Test
    void createContainer_Success_ShouldReturnId() {
        // 1. On configure TOUTE la chaîne (Fluent API)
        when(dockerClient.createContainerCmd(anyString())).thenReturn(createContainerCmd);
        when(createContainerCmd.withTty(anyBoolean())).thenReturn(createContainerCmd);
        when(createContainerCmd.withStdinOpen(anyBoolean())).thenReturn(createContainerCmd);
        when(createContainerCmd.exec()).thenReturn(createContainerResponse);
        when(createContainerResponse.getId()).thenReturn("container-123");

        // 2. Appel
        String id = containerService.createContainer("nginx:alpine");

        // 3. Vérifs
        assertEquals("container-123", id);
        verify(createContainerCmd).withTty(true);
        verify(createContainerCmd).withStdinOpen(true);
    }

    @Test
    void createContainer_ShouldThrowRuntimeException_WhenDockerFails() {
        // CORRECTION : On fait échouer le PREMIER appel pour éviter les "UnnecessaryStubbing" sur la suite
        when(dockerClient.createContainerCmd(anyString())).thenThrow(new DockerException("Docker is down", 500));

        assertThrows(RuntimeException.class, () -> containerService.createContainer("bad-image"));
    }

    @Test
    void startContainer_ShouldExecStartCmd() {
        when(dockerClient.startContainerCmd(anyString())).thenReturn(startContainerCmd);
        containerService.startContainer("123");
        verify(startContainerCmd).exec();
    }

    @Test
    void stopAndRemoveContainer_ShouldExecStopAndRemoveCmd() {
        when(dockerClient.stopContainerCmd(anyString())).thenReturn(stopContainerCmd);
        when(dockerClient.removeContainerCmd(anyString())).thenReturn(removeContainerCmd);

        containerService.stopAndRemoveContainer("123");

        verify(stopContainerCmd).exec();
        verify(removeContainerCmd).exec();
    }
}