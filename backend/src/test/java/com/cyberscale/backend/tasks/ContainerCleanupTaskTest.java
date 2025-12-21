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
    
    @Mock private Container oldValidContainer;
    @Mock private Container youngContainer;
    @Mock private Container otherImageContainer;

    @InjectMocks private ContainerCleanupTask cleanupTask;

    @Test
    void cleanupOldContainers_FullCoverage() {
        long now = Instant.now().getEpochSecond();

        // 1. Cas : Vieux conteneur CyberScale (Doit être supprimé)
        when(oldValidContainer.getId()).thenReturn("c1");
        when(oldValidContainer.getImage()).thenReturn("cyberscale/challenge");
        when(oldValidContainer.getCreated()).thenReturn(now - 4000); // > 30min

        // 2. Cas : Jeune conteneur (Ne doit PAS être supprimé)
        when(youngContainer.getImage()).thenReturn("cyberscale/challenge");
        when(youngContainer.getCreated()).thenReturn(now - 100);

        // 3. Cas : Vieux conteneur mais pas à nous (Postgres...)
        when(otherImageContainer.getImage()).thenReturn("postgres:latest");
        // Pas besoin de mocker le temps, le if(name) doit échouer avant

        // Mock de la liste
        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
        when(listContainersCmd.withShowAll(true)).thenReturn(listContainersCmd);
        when(listContainersCmd.exec()).thenReturn(List.of(oldValidContainer, youngContainer, otherImageContainer));

        // Action
        cleanupTask.cleanupOldContainers();

        // Vérifications
        verify(containerService).stopAndRemoveContainer("c1"); // Appel unique
        verify(containerService, never()).stopAndRemoveContainer("c2");
    }

    @Test
    void cleanupOldContainers_ExceptionHandling() {
        // Teste le bloc catch(Exception e) global
        when(dockerClient.listContainersCmd()).thenThrow(new RuntimeException("Docker Down"));

        // Ne doit pas planter l'appli
        cleanupTask.cleanupOldContainers();
    }
}