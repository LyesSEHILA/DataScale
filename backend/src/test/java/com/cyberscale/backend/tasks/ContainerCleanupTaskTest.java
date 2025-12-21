package com.cyberscale.backend.tasks;

import com.cyberscale.backend.services.ContainerService;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ListContainersCmd;
import com.github.dockerjava.api.model.Container;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContainerCleanupTaskTest {

    @Mock private DockerClient dockerClient;
    @Mock private ContainerService containerService;
    @Mock private ListContainersCmd listContainersCmd;
    @Mock private Container oldContainer;
    @Mock private Container newContainer;
    @Mock private Container otherContainer;

    @InjectMocks
    private ContainerCleanupTask cleanupTask;

    @Test
    void cleanupOldContainers_ShouldRemoveOnlyExpiredContainers() {
        // 1. SETUP - On prépare nos faux conteneurs
        long now = Instant.now().getEpochSecond();
        
        // Conteneur vieux (> 30 min) et valide (cyberscale)
        when(oldContainer.getId()).thenReturn("old-123");
        when(oldContainer.getImage()).thenReturn("cyberscale/base-challenge");
        when(oldContainer.getCreated()).thenReturn(now - 4000); // Il y a 4000s (> 1800s)

        // Conteneur récent (< 30 min)
        when(newContainer.getImage()).thenReturn("cyberscale/base-challenge");
        when(newContainer.getCreated()).thenReturn(now - 100);

        // Conteneur vieux MAIS qui n'est pas à nous (ex: base de données)
        when(otherContainer.getImage()).thenReturn("postgres:latest");
        // Pas besoin de mocker le temps car il doit être filtré par le nom avant

        // Mock de la commande Docker "docker ps -a"
        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
        when(listContainersCmd.withShowAll(true)).thenReturn(listContainersCmd);
        when(listContainersCmd.exec()).thenReturn(List.of(oldContainer, newContainer, otherContainer));

        // 2. EXECUTION
        cleanupTask.cleanupOldContainers();

        // 3. VERIFICATION
        // Doit supprimer le vieux conteneur cyberscale
        verify(containerService, times(1)).stopAndRemoveContainer("old-123");
        
        // Ne doit PAS supprimer les autres
        verify(containerService, never()).stopAndRemoveContainer("new-456");
    }
}