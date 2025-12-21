package com.cyberscale.backend.socket;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.AttachContainerCmd;
import com.github.dockerjava.api.command.ResizeContainerCmd;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.InputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TerminalWebSocketHandlerTest {

    @Mock private DockerClient dockerClient;
    @Mock private WebSocketSession session;
    @Mock private ResizeContainerCmd resizeCmd;
    @Mock private AttachContainerCmd attachCmd;

    @InjectMocks private TerminalWebSocketHandler handler;

    // --- Test 1 : Connexion Réussie ---
    @Test
    void afterConnectionEstablished_Success() throws Exception {
        when(session.getId()).thenReturn("s1");
        when(session.getUri()).thenReturn(new URI("ws://localhost?containerId=c1"));
        
        // Mock de la chaine Attach
        when(dockerClient.attachContainerCmd("c1")).thenReturn(attachCmd);
        when(attachCmd.withStdErr(true)).thenReturn(attachCmd);
        when(attachCmd.withStdOut(true)).thenReturn(attachCmd);
        when(attachCmd.withFollowStream(true)).thenReturn(attachCmd);
        when(attachCmd.withStdIn(any(InputStream.class))).thenReturn(attachCmd);

        handler.afterConnectionEstablished(session);

        verify(attachCmd).exec(any()); // Vérifie qu'on lance le callback Docker
    }

    // --- Test 2 : Connexion sans ID (Erreur) ---
    @Test
    void afterConnectionEstablished_NoId() throws Exception {
        when(session.getUri()).thenReturn(new URI("ws://localhost")); // Pas d'ID

        handler.afterConnectionEstablished(session);

        verify(session).close(CloseStatus.BAD_DATA);
    }

    // --- Test 3 : Resize (JSON Valide) ---
    @Test
    void handleTextMessage_Resize() throws Exception {
        setupSessionState("s1", "c1"); // Helper method

        when(dockerClient.resizeContainerCmd("c1")).thenReturn(resizeCmd);
        when(resizeCmd.withSize(anyInt(), anyInt())).thenReturn(resizeCmd);

        // Envoi du JSON
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"resize\", \"cols\":100, \"rows\":50}"));

        verify(resizeCmd).withSize(100, 50);
        verify(resizeCmd).exec();
    }

    // --- Test 4 : Input Standard (Pas du JSON, ou JSON invalide) ---
    @Test
    void handleTextMessage_StandardInput() throws Exception {
        PipedOutputStream outputStream = mock(PipedOutputStream.class);
        setupSessionStateWithStream("s1", "c1", outputStream);

        // Envoi d'une commande "ls"
        handler.handleTextMessage(session, new TextMessage("ls"));

        verify(outputStream).write(any(byte[].class)); // Vérifie qu'on écrit dans le tuyau Docker
    }

    // --- Test 5 : JSON Invalide (ne doit pas planter, doit être traité comme input) ---
    @Test
    void handleTextMessage_InvalidJson() throws Exception {
        PipedOutputStream outputStream = mock(PipedOutputStream.class);
        setupSessionStateWithStream("s1", "c1", outputStream);

        // JSON cassé -> Catch exception -> Traité comme texte
        handler.handleTextMessage(session, new TextMessage("{broken_json"));

        verify(outputStream).write(any(byte[].class));
    }

    // --- Test 6 : Fermeture ---
    @Test
    void afterConnectionClosed() throws Exception {
        setupSessionState("s1", "c1");
        
        handler.afterConnectionClosed(session, CloseStatus.NORMAL);
        
        // Vérifie via Reflection que la map est vide
        Map<?, ?> streams = (Map<?, ?>) ReflectionTestUtils.getField(handler, "activeOutputStreams");
        assert streams != null;
        assert streams.isEmpty();
    }

    // --- Helpers avec Correction Lenient ---
    private void setupSessionState(String sessionId, String containerId) throws Exception {
        when(session.getId()).thenReturn(sessionId);
        // CORRECTION : Utilisation de lenient() car getUri() n'est pas appelé dans tous les tests
        lenient().when(session.getUri()).thenReturn(new URI("ws://localhost?containerId=" + containerId));
        
        Map<String, PipedOutputStream> streams = new ConcurrentHashMap<>();
        streams.put(sessionId, new PipedOutputStream());
        ReflectionTestUtils.setField(handler, "activeOutputStreams", streams);
    }

    private void setupSessionStateWithStream(String sessionId, String containerId, PipedOutputStream stream) throws Exception {
        when(session.getId()).thenReturn(sessionId);
        // CORRECTION : Utilisation de lenient()
        lenient().when(session.getUri()).thenReturn(new URI("ws://localhost?containerId=" + containerId));
        
        Map<String, PipedOutputStream> streams = new ConcurrentHashMap<>();
        streams.put(sessionId, stream);
        ReflectionTestUtils.setField(handler, "activeOutputStreams", streams);
    }
}