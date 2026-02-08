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
    
    @InjectMocks 
    private TerminalWebSocketHandler handler; 

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
    void afterConnectionEstablishedSuccess() throws Exception {
        when(session.getId()).thenReturn("s1");
        when(session.getUri()).thenReturn(new URI(WS_URI));
        
        handler.afterConnectionEstablished(session);

        verify(dockerClient).execCreateCmd("c1");
    }

    @Test
    void afterConnectionEstablishedNoIdClosesSession() throws Exception {
        when(session.getUri()).thenReturn(new URI("ws://localhost")); 
        
        handler.afterConnectionEstablished(session);

        verify(session).close(CloseStatus.BAD_DATA);
    }
    
    @Test
    void afterConnectionEstablishedException() throws Exception {
        when(session.getUri()).thenReturn(new URI(WS_URI));
        // Force une exception Docker
        when(dockerClient.execCreateCmd(anyString())).thenThrow(new RuntimeException("Docker Error"));
        
        handler.afterConnectionEstablished(session);
        
        verify(session).close(CloseStatus.SERVER_ERROR);
    }

    @Test
    void handleTextMessageResizeSuccess() throws Exception {
        setupSession();
        when(dockerClient.resizeContainerCmd("c1")).thenReturn(resizeContainerCmd);
        when(resizeContainerCmd.withSize(anyInt(), anyInt())).thenReturn(resizeContainerCmd);

        String json = "{\"type\":\"resize\", \"cols\":100, \"rows\":50}";
        handler.handleMessage(session, new TextMessage(json));

        verify(resizeContainerCmd).exec();
    }
    
    @Test
    void handleTextMessageResizeMalformedJson() throws Exception {
        setupSession();
        // JSON cassé -> Doit passer dans le catch sans planter
        String badJson = "{\"type\":\"resize\", \"cols\":"; 
        handler.handleMessage(session, new TextMessage(badJson));
    }

    @Test
    void handleTextMessageStandardInput() throws Exception {
        setupSession();
        handler.handleMessage(session, new TextMessage("ls"));
    }
    
    @Test
    void handleTextMessageStreamError() throws Exception {
        // Mock session avec un stream qui plante
        when(session.getId()).thenReturn("s1");
        // ✅ CORRECTION : Suppression de session.getUri() qui ne sert à rien ici
        
        PipedOutputStream mockStream = mock(PipedOutputStream.class);
        doThrow(new IOException("Write failed")).when(mockStream).write(any(byte[].class));
        
        Map<String, PipedOutputStream> streams = new ConcurrentHashMap<>();
        streams.put("s1", mockStream);
        ReflectionTestUtils.setField(handler, "activeOutputStreams", streams);
        
        // Ne doit pas throw d'exception, juste logger
        handler.handleMessage(session, new TextMessage("ls"));
    }

    @Test
    void afterConnectionClosedCleanly() throws Exception {
        setupSession();
        handler.afterConnectionClosed(session, CloseStatus.NORMAL);
    }
    
    // Helper pour initialiser une session valide dans la Map interne
    private void setupSession() throws Exception {
        when(session.getId()).thenReturn("s1");
        when(session.getUri()).thenReturn(new URI(WS_URI));
        handler.afterConnectionEstablished(session);
    }
}