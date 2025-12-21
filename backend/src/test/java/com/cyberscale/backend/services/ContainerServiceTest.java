package com.cyberscale.backend.services;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.RemoveContainerCmd;
import com.github.dockerjava.api.command.StartContainerCmd;
import com.github.dockerjava.api.command.StopContainerCmd;
import com.github.dockerjava.api.exception.InternalServerErrorException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ContainerServiceTest {

    @Mock
    private DockerClient dockerClient;

    @InjectMocks
    private ContainerService containerService;


    @Test
    void createContainer_Success_ShouldReturnId() {
        String imageId = "nginx:alpine";
        String expectedId = "container-123";

        CreateContainerCmd cmd = mock(CreateContainerCmd.class);
        CreateContainerResponse response = mock(CreateContainerResponse.class);

        when(dockerClient.createContainerCmd(imageId)).thenReturn(cmd);
        when(cmd.withTty(true)).thenReturn(cmd); 
        when(cmd.exec()).thenReturn(response);
        when(response.getId()).thenReturn(expectedId);

        String actualId = containerService.createContainer(imageId);

        assertEquals(expectedId, actualId);
        verify(cmd).exec(); 
    }

    @Test
    void startContainer_Success_ShouldNotThrow() {
        String containerId = "container-123";
        StartContainerCmd cmd = mock(StartContainerCmd.class);
        when(dockerClient.startContainerCmd(containerId)).thenReturn(cmd);

        assertDoesNotThrow(() -> containerService.startContainer(containerId));
        verify(cmd).exec();
    }

    @Test
    void stopAndRemoveContainer_Success_ShouldCallStopAndRemove() {
        String containerId = "container-123";
        StopContainerCmd stopCmd = mock(StopContainerCmd.class);
        RemoveContainerCmd removeCmd = mock(RemoveContainerCmd.class);

        when(dockerClient.stopContainerCmd(containerId)).thenReturn(stopCmd);
        when(dockerClient.removeContainerCmd(containerId)).thenReturn(removeCmd);

        containerService.stopAndRemoveContainer(containerId);

        verify(stopCmd).exec();   
        verify(removeCmd).exec(); 
    }




    @Test
    void createContainer_ShouldThrowRuntimeException_WhenDockerFails() {
        String imageId = "test image";
        
        CreateContainerCmd cmd = mock(CreateContainerCmd.class);
        when(dockerClient.createContainerCmd(anyString())).thenReturn(cmd);
        when(cmd.withTty(any(Boolean.class))).thenReturn(cmd);
        
        when(cmd.exec()).thenThrow(new InternalServerErrorException("Simulated Docker Error"));

        assertThrows(RuntimeException.class, () -> {
            containerService.createContainer(imageId);
        });
    }

    @Test
    void startContainer_ShouldThrowRuntimeException_WhenDockerFails() {

        String containerId = "bad id";
        
        StartContainerCmd cmd = mock(StartContainerCmd.class);
        when(dockerClient.startContainerCmd(containerId)).thenReturn(cmd);
        
        when(cmd.exec()).thenThrow(new InternalServerErrorException("Start Failed"));

        assertThrows(RuntimeException.class, () -> {
            containerService.startContainer(containerId);
        });
    }

    @Test
    void stopAndRemoveContainer_ShouldLogAndNotThrow_WhenDockerFails() {
        String containerId = "container fail";
        
        StopContainerCmd stopCmd = mock(StopContainerCmd.class);
        when(dockerClient.stopContainerCmd(containerId)).thenReturn(stopCmd);
        
        when(stopCmd.exec()).thenThrow(new InternalServerErrorException("Stop Failed"));

        assertDoesNotThrow(() -> {
            containerService.stopAndRemoveContainer(containerId);
        });

        verify(dockerClient).stopContainerCmd(containerId);
    }
    
    @Test
    void stopAndRemoveContainer_ShouldHandleRemoveError() {
        String containerId = "container remove";
        
        StopContainerCmd stopCmd = mock(StopContainerCmd.class);
        RemoveContainerCmd removeCmd = mock(RemoveContainerCmd.class);
        
        when(dockerClient.stopContainerCmd(containerId)).thenReturn(stopCmd);
        when(dockerClient.removeContainerCmd(containerId)).thenReturn(removeCmd);
        
        when(removeCmd.exec()).thenThrow(new InternalServerErrorException("Remove Failed"));

        assertDoesNotThrow(() -> containerService.stopAndRemoveContainer(containerId));
    }
}