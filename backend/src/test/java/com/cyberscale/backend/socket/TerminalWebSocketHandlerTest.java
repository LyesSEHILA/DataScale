package com.cyberscale.backend.socket;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ResizeContainerCmd;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.PipedOutputStream;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TerminalWebSocketHandlerTest {

    @Mock private DockerClient dockerClient;
    @Mock private WebSocketSession session;
    @Mock private ResizeContainerCmd resizeCmd;

    @InjectMocks
    private TerminalWebSocketHandler handler;

    @Test
    void handleTextMessage_ShouldResizeOnJsonCommand() throws Exception {
        // 1. Mock de la session
        String sessionId = "session-1";
        when(session.getId()).thenReturn(sessionId);
        when(session.getUri()).thenReturn(new URI("ws://localhost/ws/terminal?containerId=abc-123"));

        // 2. Mock Docker
        when(dockerClient.resizeContainerCmd(anyString())).thenReturn(resizeCmd);
        // Attention : on utilise .withSize(w, h) car c'est la méthode que nous avons corrigée ensemble
        when(resizeCmd.withSize(anyInt(), anyInt())).thenReturn(resizeCmd);

        // 3. CORRECTION MAJEURE : On injecte un stream dans la map privée "activeOutputStreams"
        // Sinon le handler retourne immédiatement car il croit la session invalide
        Map<String, PipedOutputStream> streams = new ConcurrentHashMap<>();
        streams.put(sessionId, new PipedOutputStream());
        ReflectionTestUtils.setField(handler, "activeOutputStreams", streams);

        // 4. Action
        String jsonResize = "{\"type\":\"resize\", \"cols\":100, \"rows\":50}";
        handler.handleTextMessage(session, new TextMessage(jsonResize));

        // 5. Vérif
        verify(dockerClient).resizeContainerCmd("abc-123");
        verify(resizeCmd).withSize(100, 50); // cols=100, rows=50
        verify(resizeCmd).exec();
    }
}