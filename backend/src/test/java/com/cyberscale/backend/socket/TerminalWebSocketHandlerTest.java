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
    void handleTextMessage_ShouldResize() throws Exception {
        // 1. Simuler une session
        String sessionId = "s1";
        when(session.getId()).thenReturn(sessionId);
        when(session.getUri()).thenReturn(new URI("ws://localhost?containerId=c1"));

        // 2. Injecter le stream manuellement (car on ne lance pas la connexion réelle)
        Map<String, PipedOutputStream> streams = new ConcurrentHashMap<>();
        streams.put(sessionId, new PipedOutputStream());
        // On utilise ReflectionTestUtils pour accéder au champ privé
        ReflectionTestUtils.setField(handler, "activeOutputStreams", streams);

        // 3. Mocker Docker
        when(dockerClient.resizeContainerCmd(anyString())).thenReturn(resizeCmd);
        when(resizeCmd.withSize(anyInt(), anyInt())).thenReturn(resizeCmd);

        // 4. Action : Envoyer le JSON de resize
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"resize\", \"cols\":80, \"rows\":24}"));

        // 5. Vérif
        verify(dockerClient).resizeContainerCmd("c1");
        verify(resizeCmd).withSize(80, 24);
        verify(resizeCmd).exec();
    }
}