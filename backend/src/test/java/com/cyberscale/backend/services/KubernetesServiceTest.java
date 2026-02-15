package com.cyberscale.backend.services;

import com.cyberscale.backend.exceptions.KubernetesDeploymentException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KubernetesServiceTest {

    @Mock
    private TemplateGenerator templateGenerator;

    @InjectMocks
    private KubernetesService kubernetesService;

    @Test
    void deployDecoy_Success() throws IOException {
        // 1. ARRANGE
        // On simule un template valide (AVEC le runtime sécurisé kata)
        String validYaml = "apiVersion: v1\nkind: Pod\nspec:\n  runtimeClassName: kata";
        when(templateGenerator.generateYaml("mysql")).thenReturn(validYaml);

        // 2. Mock du ProcessBuilder (la commande système)
        // Cette syntaxe permet d'intercepter tous les "new ProcessBuilder()"
        try (MockedConstruction<ProcessBuilder> mockedPb = mockConstruction(ProcessBuilder.class,
                (mock, context) -> {
                    // On simule le processus créé
                    Process mockProcess = mock(Process.class);
                    when(mock.start()).thenReturn(mockProcess);
                    
                    // Simulation d'une exécution réussie (code 0)
                    when(mockProcess.waitFor(anyLong(), any())).thenReturn(true);
                    when(mockProcess.exitValue()).thenReturn(0);
                    
                    // Simulation de la sortie standard (logs)
                    when(mockProcess.getInputStream()).thenReturn(
                            new ByteArrayInputStream("pod/honeypot created".getBytes(StandardCharsets.UTF_8)));
                })) {

            // 3. ACT
            kubernetesService.deployDecoy("mysql");

            // 4. ASSERT
            // On vérifie que le template a été appelé
            verify(templateGenerator).generateYaml("mysql");
            // On vérifie qu'un ProcessBuilder a bien été créé (c'est notre mock)
            assertEquals(1, mockedPb.constructed().size());
        }
    }

    @Test
    void deployDecoy_SecurityCheck_ShouldFail() throws IOException {
        // On simule un template INVALIDE (SANS kata)
        String unsafeYaml = "apiVersion: v1\nkind: Pod\nspec:\n  runtimeClassName: standard";
        when(templateGenerator.generateYaml("hack")).thenReturn(unsafeYaml);

        // CORRECTION ICI : On attend l'exception métier, pas l'exception brute
        KubernetesDeploymentException e = assertThrows(KubernetesDeploymentException.class, () -> {
            kubernetesService.deployDecoy("hack");
        });
        
        // On vérifie que la cause interne est bien une erreur de sécurité
        assertTrue(e.getCause() instanceof SecurityException, "La cause devrait être une SecurityException");
        assertTrue(e.getCause().getMessage().contains("Runtime Kata manquant"));
    }

    @Test
    void deployDecoy_KubectlFailure_ShouldThrowException() throws IOException {
        String validYaml = "spec:\n  runtimeClassName: kata";
        when(templateGenerator.generateYaml("mysql")).thenReturn(validYaml);

        try (MockedConstruction<ProcessBuilder> mockedPb = mockConstruction(ProcessBuilder.class,
                (mock, context) -> {
                    Process mockProcess = mock(Process.class);
                    when(mock.start()).thenReturn(mockProcess);
                    // Simulation d'un échec (code 1)
                    when(mockProcess.waitFor(anyLong(), any())).thenReturn(true);
                    when(mockProcess.exitValue()).thenReturn(1); // Erreur
                    when(mockProcess.getInputStream()).thenReturn(new ByteArrayInputStream("Error".getBytes()));
                })) {

            assertThrows(KubernetesDeploymentException.class, () -> {
                kubernetesService.deployDecoy("mysql");
            });
        }
    }
}