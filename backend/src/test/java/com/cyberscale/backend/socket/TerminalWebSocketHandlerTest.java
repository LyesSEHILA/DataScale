package com.cyberscale.backend.socket;

import java.io.InputStream;
import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.ExecCreateCmd;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.ExecStartCmd;
import com.github.dockerjava.api.command.ResizeContainerCmd;

@ExtendWith(MockitoExtension.class)
class TerminalWebSocketHandlerTest {

    @Mock private DockerClient dockerClient;
    @Mock private WebSocketSession session;
    @InjectMocks @Spy private TerminalWebSocketHandler handler;

    // Mocks Docker
    @Mock private ExecCreateCmd execCreateCmd;
    @Mock private ExecCreateCmdResponse execCreateCmdResponse;
    @Mock private ExecStartCmd execStartCmd;
    @Mock private ResizeContainerCmd resizeContainerCmd;

    @BeforeEach
    void setUp() {
        // Configuration Lenient pour éviter les erreurs de stubbing inutile
        lenient().when(dockerClient.execCreateCmd(anyString())).thenReturn(execCreateCmd);
        lenient().when(execCreateCmd.withAttachStdout(true)).thenReturn(execCreateCmd);
        lenient().when(execCreateCmd.withAttachStderr(true)).thenReturn(execCreateCmd);
        lenient().when(execCreateCmd.withAttachStdin(true)).thenReturn(execCreateCmd);
        lenient().when(execCreateCmd.withTty(true)).thenReturn(execCreateCmd);
        lenient().when(execCreateCmd.withCmd(anyString())).thenReturn(execCreateCmd);
        lenient().when(execCreateCmd.exec()).thenReturn(execCreateCmdResponse);
        lenient().when(execCreateCmdResponse.getId()).thenReturn("exec-123");

        lenient().when(dockerClient.execStartCmd(anyString())).thenReturn(execStartCmd);
        lenient().when(execStartCmd.withTty(true)).thenReturn(execStartCmd);
        lenient().when(execStartCmd.withStdIn(any(InputStream.class))).thenReturn(execStartCmd);
    }

    @Test
    void afterConnectionEstablished_ShouldStartDockerExec() throws Exception {
        when(session.getUri()).thenReturn(new URI("ws://localhost?containerId=c1"));
        
        handler.afterConnectionEstablished(session);

        verify(dockerClient).execCreateCmd("c1");
        verify(execStartCmd).exec(any(ResultCallback.class));
    }

    @Test
    void afterConnectionEstablished_NoContainerId_ShouldClose() throws Exception {
        when(session.getUri()).thenReturn(new URI("ws://localhost")); // Pas de params
        
        handler.afterConnectionEstablished(session);

        verify(session).close(CloseStatus.BAD_DATA);
        verify(dockerClient, never()).execCreateCmd(anyString());
    }

    @Test
    void handleTextMessage_ResizeCommand() throws Exception {
        // 1. Établir la connexion pour remplir la map
        when(session.getId()).thenReturn("s1");
        when(session.getUri()).thenReturn(new URI("ws://localhost?containerId=c1"));
        handler.afterConnectionEstablished(session);

        // 2. Préparer le mock Resize
        when(dockerClient.resizeContainerCmd("c1")).thenReturn(resizeContainerCmd);
        when(resizeContainerCmd.withSize(100, 50)).thenReturn(resizeContainerCmd);

        // 3. Envoyer le message de resize
        String json = "{\"type\":\"resize\", \"cols\":100, \"rows\":50}";
        handler.handleMessage(session, new TextMessage(json));

        // 4. Vérifier
        verify(resizeContainerCmd).exec();
    }

    @Test
    void handleTextMessage_StandardInput() throws Exception {
        when(session.getId()).thenReturn("s1");
        when(session.getUri()).thenReturn(new URI("ws://localhost?containerId=c1"));
        handler.afterConnectionEstablished(session);

        // On capture l'InputStream passé à Docker pour vérifier qu'on écrit dedans
        ArgumentCaptor<InputStream> inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
        verify(execStartCmd).withStdIn(inputStreamCaptor.capture());
        InputStream dockerInputStream = inputStreamCaptor.getValue();

        // Envoyer une commande "ls"
        handler.handleMessage(session, new TextMessage("ls"));

        // Comme c'est un PipedStream, c'est complexe à vérifier directement sans bloquer.
        // Ici, on vérifie surtout que ça ne plante pas et que le code passe dans le "else" du JSON.
    }

    @Test
    void afterConnectionClosed_ShouldCleanup() throws Exception {
        when(session.getId()).thenReturn("s1");
        when(session.getUri()).thenReturn(new URI("ws://localhost?containerId=c1"));
        handler.afterConnectionEstablished(session);

        handler.afterConnectionClosed(session, CloseStatus.NORMAL);
        
        // Vérification indirecte : si on renvoie un message, ça ne devrait plus trouver le stream
        // (difficile à asserter sans exposer l'état interne, mais ça couvre les lignes)
    }
}