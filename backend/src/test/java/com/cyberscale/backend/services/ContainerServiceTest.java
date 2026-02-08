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

    @Mock private DockerClient dockerClient;
    @InjectMocks private ContainerService containerService;

    // Mocks Docker
    @Mock private CreateContainerCmd createContainerCmd;
    @Mock private CreateContainerResponse createContainerResponse;
    @Mock private StartContainerCmd startContainerCmd;
    @Mock private StopContainerCmd stopContainerCmd;
    @Mock private RemoveContainerCmd removeContainerCmd;

    // Mocks Exec
    @Mock private ExecCreateCmd execCreateCmd;
    @Mock private ExecCreateCmdResponse execCreateCmdResponse;
    @Mock private ExecStartCmd execStartCmd;
    @Mock private ExecStartResultCallback execStartResultCallback;

    @Test
    void createContainer_Success() {
        when(dockerClient.createContainerCmd(anyString())).thenReturn(createContainerCmd);
        when(createContainerCmd.withTty(anyBoolean())).thenReturn(createContainerCmd);
        when(createContainerCmd.withStdinOpen(anyBoolean())).thenReturn(createContainerCmd);
        when(createContainerCmd.exec()).thenReturn(createContainerResponse);
        when(createContainerResponse.getId()).thenReturn("id-123");

        String res = containerService.createContainer("img");
        assertEquals("id-123", res);
    }

    @Test
    void createContainer_Failure_ShouldThrowRuntimeException() {
        when(dockerClient.createContainerCmd(anyString())).thenThrow(new DockerException("Docker Error", 500));
        assertThrows(RuntimeException.class, () -> containerService.createContainer("img"));
    }

    @Test
    void startContainer_Success() {
        when(dockerClient.startContainerCmd(anyString())).thenReturn(startContainerCmd);
        containerService.startContainer("id");
        verify(startContainerCmd).exec();
    }

    @Test
    void startContainer_Failure_ShouldThrowRuntimeException() {
        when(dockerClient.startContainerCmd(anyString())).thenThrow(new DockerException("Start Error", 500));
        assertThrows(RuntimeException.class, () -> containerService.startContainer("id"));
    }

    // 🚨 CORRECTION : On utilise lenient() sur les méthodes du Builder
    @Test
    void createChallengeContainer_ShouldInjectEnvVar() {
        String challengeId = "linux-challenge";
        String rawFlag = "a1b2c3d4";

        when(dockerClient.createContainerCmd(anyString())).thenReturn(createContainerCmd);
        
        // Mockito est strict : si ton code n'appelle pas .withName(), il lance une erreur.
        // lenient() rend ces stubs "optionnels" (utilisés si appelés, ignorés sinon).
        lenient().when(createContainerCmd.withTty(anyBoolean())).thenReturn(createContainerCmd);
        lenient().when(createContainerCmd.withStdinOpen(anyBoolean())).thenReturn(createContainerCmd);
        lenient().when(createContainerCmd.withName(anyString())).thenReturn(createContainerCmd);
        
        // Idem pour withEnv : on utilise lenient() + any(String[].class) pour couvrir tous les cas
        lenient().when(createContainerCmd.withEnv(any(String[].class))).thenReturn(createContainerCmd);
        
        when(createContainerCmd.exec()).thenReturn(createContainerResponse);
        when(createContainerResponse.getId()).thenReturn("container-99");
        when(dockerClient.startContainerCmd("container-99")).thenReturn(startContainerCmd);

        String resultId = containerService.createChallengeContainer(challengeId, rawFlag);

        assertEquals("container-99", resultId);

        // Vérification finale : on s'assure que withEnv a bien été appelé avec le flag
        ArgumentCaptor<String> envCaptor = ArgumentCaptor.forClass(String.class);
        verify(createContainerCmd).withEnv(envCaptor.capture());

        String capturedEnv = envCaptor.getValue();
        assertTrue(capturedEnv.contains("CHALLENGE_FLAG=" + rawFlag), 
            "La variable CHALLENGE_FLAG doit être injectée");
    }

    @Test
    void stopAndRemoveContainer_Success() {
        when(dockerClient.stopContainerCmd(anyString())).thenReturn(stopContainerCmd);
        when(dockerClient.removeContainerCmd(anyString())).thenReturn(removeContainerCmd);
        containerService.stopAndRemoveContainer("id");
        verify(stopContainerCmd).exec();
        verify(removeContainerCmd).exec();
    }

    @Test
    void stopAndRemoveContainer_WhenAlreadyStopped() {
        when(dockerClient.stopContainerCmd(anyString())).thenReturn(stopContainerCmd);
        when(dockerClient.removeContainerCmd(anyString())).thenReturn(removeContainerCmd);
        doThrow(new NotModifiedException("Stopped")).when(stopContainerCmd).exec();

        containerService.stopAndRemoveContainer("id");
        verify(removeContainerCmd).exec();
    }

    @Test
    void stopAndRemoveContainer_WhenStopFailsGeneric() {
        when(dockerClient.stopContainerCmd(anyString())).thenReturn(stopContainerCmd);
        when(dockerClient.removeContainerCmd(anyString())).thenReturn(removeContainerCmd);
        doThrow(new RuntimeException("Crash")).when(stopContainerCmd).exec();

        containerService.stopAndRemoveContainer("id");
        verify(removeContainerCmd).exec();
    }

    @Test
    void stopAndRemoveContainer_WhenRemoveFails() {
        when(dockerClient.stopContainerCmd(anyString())).thenReturn(stopContainerCmd);
        when(dockerClient.removeContainerCmd(anyString())).thenReturn(removeContainerCmd);
        doThrow(new RuntimeException("Crash Remove")).when(removeContainerCmd).exec();

        assertDoesNotThrow(() -> containerService.stopAndRemoveContainer("id"));
    }

    @Test
    void executeCommand_Success() throws InterruptedException {
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
    void executeCommand_Failure() {
        when(dockerClient.execCreateCmd(anyString())).thenThrow(new RuntimeException("Docker Down"));

        String result = containerService.executeCommand("container-id", "ls");
        
        assertTrue(result.contains("Error executing command"), "Le message d'erreur doit correspondre à celui du service");
    }

    @Test
    void executeCommand_Exception_Internal() {
        // Teste le bloc catch(Exception e)
        when(dockerClient.execCreateCmd(anyString())).thenThrow(new RuntimeException("Docker Crash"));

        String result = containerService.executeCommand("container-id", "ls");
        
        // Vérifie qu'on retourne bien le message d'erreur et qu'on ne plante pas
        assertEquals("Error executing command", result);
    }

    @Test
    void stopAndRemoveContainer_Exception_OnStop() {
        // Teste le catch(Exception e) dans le stop
        when(dockerClient.stopContainerCmd(anyString())).thenReturn(stopContainerCmd);
        // Force une erreur générique
        doThrow(new RuntimeException("Stop Failed")).when(stopContainerCmd).exec();
        
        // Mock remove pour qu'il fonctionne
        when(dockerClient.removeContainerCmd(anyString())).thenReturn(removeContainerCmd);

        assertDoesNotThrow(() -> containerService.stopAndRemoveContainer("id"));
        
        // Vérifie que remove est quand même appelé même si stop a planté
        verify(removeContainerCmd).exec();
    }

    @Test
    void stopAndRemoveContainer_Exception_OnRemove() {
        // Teste le catch(Exception e) dans le remove
        when(dockerClient.stopContainerCmd(anyString())).thenReturn(stopContainerCmd);
        when(dockerClient.removeContainerCmd(anyString())).thenReturn(removeContainerCmd);
        
        doThrow(new RuntimeException("Remove Failed")).when(removeContainerCmd).exec();

        assertDoesNotThrow(() -> containerService.stopAndRemoveContainer("id"));
    }
    
    @Test
    void isCommandDangerous_ShouldBlock() {
        // Ce test couvre la méthode privée si tu utilises Reflection ou si executeCommand l'appelle
        String result = containerService.executeCommand("c1", "rm -rf /");
        assertTrue(result.contains("blocked"));
        
        String result2 = containerService.executeCommand("c1", ":(){:|:&};:"); // Fork bomb
        assertTrue(result2.contains("blocked"));
    }
    
}
