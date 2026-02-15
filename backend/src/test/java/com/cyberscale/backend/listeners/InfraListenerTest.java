package com.cyberscale.backend.listeners;

import com.cyberscale.backend.dto.DeployDecoyRequest;
import com.cyberscale.backend.dto.ExecutionRequest;
import com.cyberscale.backend.services.ContainerService;
import com.cyberscale.backend.services.KubernetesService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InfraListenerTest {

    @Mock
    private ContainerService containerService;

    @Mock
    private KubernetesService kubernetesService;

    @InjectMocks
    private InfraListener infraListener;

    @Test
    void handleDeployRequest_ShouldCallKubernetesService() {
        // ARRANGE
        DeployDecoyRequest request = new DeployDecoyRequest("u1", "mysql");

        // ACT
        infraListener.handleDeployRequest(request);

        // ASSERT
        verify(kubernetesService).deployDecoy("mysql");
    }

    @Test
    void processExecution_ShouldCallContainerService() {
        ExecutionRequest request = new ExecutionRequest("container-1", "ls -la");
        infraListener.processExecution(request);
        verify(containerService).executeCommand("container-1", "ls -la");
    }
}
