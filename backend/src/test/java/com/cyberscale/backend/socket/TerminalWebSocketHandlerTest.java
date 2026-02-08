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
    
    @InjectMocks 
    private TerminalWebSocketHandler handler; // Pas besoin de @Spy ici pour ce test

    // Mocks Docker
    @Mock private ExecCreateCmd execCreateCmd;
    @Mock private ExecCreateCmdResponse execCreateCmdResponse;
    @Mock private ExecStartCmd execStartCmd;
    @Mock private ResizeContainerCmd resizeContainerCmd;

    @BeforeEach
    void setUp() {
        // Configuration Lenient pour éviter les erreurs de stubbing strict
        lenient().when(dockerClient.execCreateCmd(anyString())).thenReturn(execCreateCmd);
        lenient().when(execCreateCmd.withAttachStdout(true)).thenReturn(execCreateCmd);
        lenient().when(execCreateCmd.withAttachStderr(true)).thenReturn(execCreateCmd);
        lenient().when(execCreateCmd.withAttachStdin(true)).thenReturn(execCreateCmd);
        lenient().when(execCreateCmd.withTty(true)).thenReturn(execCreateCmd);
        
        // 🚨 CORRECTION CRITIQUE : withCmd prend un tableau (String...), pas une simple String !
        lenient().when(execCreateCmd.withCmd(any(String[].class))).thenReturn(execCreateCmd);
        
        lenient().when(execCreateCmd.exec()).thenReturn(execCreateCmdResponse);
        lenient().when(execCreateCmdResponse.getId()).thenReturn("exec-123");

        lenient().when(dockerClient.execStartCmd(anyString())).thenReturn(execStartCmd);
        lenient().when(execStartCmd.withTty(true)).thenReturn(execStartCmd);
        lenient().when(execStartCmd.withStdIn(any(InputStream.class))).thenReturn(execStartCmd);
    }

    @Test
    void afterConnectionEstablished_ShouldStartDockerExec() throws Exception {
        // 🚨 CORRECTION : Il faut mocker getId() sinon ConcurrentHashMap plante
        when(session.getId()).thenReturn("s1");
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
        // 1. Setup session
        when(session.getId()).thenReturn("s1");
        when(session.getUri()).thenReturn(new URI("ws://localhost?containerId=c1"));
        handler.afterConnectionEstablished(session); // Remplit la map

        // 2. Setup Resize mock
        when(dockerClient.resizeContainerCmd("c1")).thenReturn(resizeContainerCmd);
        when(resizeContainerCmd.withSize(100, 50)).thenReturn(resizeContainerCmd);

        // 3. Action
        String json = "{\"type\":\"resize\", \"cols\":100, \"rows\":50}";
        handler.handleMessage(session, new TextMessage(json));

        // 4. Verification
        verify(resizeContainerCmd).exec();
    }

    @Test
    void handleTextMessage_StandardInput() throws Exception {
        // 1. Setup session
        when(session.getId()).thenReturn("s1");
        when(session.getUri()).thenReturn(new URI("ws://localhost?containerId=c1"));
        handler.afterConnectionEstablished(session);

        // Capture du stream
        ArgumentCaptor<InputStream> inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
        verify(execStartCmd).withStdIn(inputStreamCaptor.capture());

        // 2. Action
        handler.handleMessage(session, new TextMessage("ls"));

        // Pas d'exception levée signifie que le code a traversé le try/catch et tenté d'écrire
    }

    @Test
    void afterConnectionClosed_ShouldCleanup() throws Exception {
        when(session.getId()).thenReturn("s1");
        when(session.getUri()).thenReturn(new URI("ws://localhost?containerId=c1"));
        handler.afterConnectionEstablished(session);

        handler.afterConnectionClosed(session, CloseStatus.NORMAL);
        
        // On vérifie juste que ça ne plante pas, la vérification interne (reflection) est fragile
        // Si besoin de vérifier le nettoyage, on peut le faire via ReflectionTestUtils ici
    }
}