package com.cyberscale.backend.socket;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.ExecCreateCmd;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.ExecStartCmd;
import com.github.dockerjava.api.command.ResizeContainerCmd;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TerminalWebSocketHandlerTest {

    private static final String WS_URI = "ws://localhost?containerId=c1";

    @Mock private DockerClient dockerClient;
    @Mock private WebSocketSession session;
    @InjectMocks private TerminalWebSocketHandler handler;

    @Mock private ExecCreateCmd execCreateCmd;
    @Mock private ExecCreateCmdResponse execCreateCmdResponse;
    @Mock private ExecStartCmd execStartCmd;
    @Mock private ResizeContainerCmd resizeContainerCmd;

    @BeforeEach
    void setUp() {
        lenient().when(dockerClient.execCreateCmd(anyString())).thenReturn(execCreateCmd);
        lenient().when(execCreateCmd.withAttachStdout(true)).thenReturn(execCreateCmd);
        lenient().when(execCreateCmd.withAttachStderr(true)).thenReturn(execCreateCmd);
        lenient().when(execCreateCmd.withAttachStdin(true)).thenReturn(execCreateCmd);
        lenient().when(execCreateCmd.withTty(true)).thenReturn(execCreateCmd);
        lenient().when(execCreateCmd.withCmd(any(String[].class))).thenReturn(execCreateCmd);
        lenient().when(execCreateCmd.exec()).thenReturn(execCreateCmdResponse);
        lenient().when(execCreateCmdResponse.getId()).thenReturn("exec-123");
        lenient().when(dockerClient.execStartCmd(anyString())).thenReturn(execStartCmd);
        lenient().when(execStartCmd.withTty(true)).thenReturn(execStartCmd);
        lenient().when(execStartCmd.withStdIn(any(InputStream.class))).thenReturn(execStartCmd);
    }

    @Test
    void handleTextMessageJsonMalformedShouldLogAndNotThrow() throws Exception {
        when(session.getId()).thenReturn("s1");
        when(session.getUri()).thenReturn(new URI(WS_URI));
        handler.afterConnectionEstablished(session);

        // Envoi d'un JSON cassé pour déclencher le catch(Exception e) dans le bloc resize
        String brokenJson = "{\"type\":\"resize\", \"cols\":"; // JSON incomplet
        handler.handleMessage(session, new TextMessage(brokenJson));
        
        // Si pas d'exception, le test passe (le catch a fonctionné)
    }

    @Test
    void afterConnectionClosedWithExceptionShouldNotCrash() throws Exception {
        when(session.getId()).thenReturn("s1");
        when(session.getUri()).thenReturn(new URI(WS_URI));
        handler.afterConnectionEstablished(session);

        // On injecte un Stream qui plante à la fermeture pour tester le catch(IOException)
        PipedOutputStream faultyStream = mock(PipedOutputStream.class);
        doThrow(new IOException("Close failed")).when(faultyStream).close();

        Map<String, PipedOutputStream> streams = new ConcurrentHashMap<>();
        streams.put("s1", faultyStream);
        ReflectionTestUtils.setField(handler, "activeOutputStreams", streams);

        // Devrait avaler l'exception
        handler.afterConnectionClosed(session, CloseStatus.NORMAL);
    }
}