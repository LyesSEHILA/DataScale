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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.InputStream;
import java.net.URI;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TerminalWebSocketHandlerTest {

    // SONAR FIX: Extract constant
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

    // SONAR FIX: Method naming (camelCase)
    @Test
    void afterConnectionEstablishedShouldStartDockerExec() throws Exception {
        when(session.getId()).thenReturn("s1");
        when(session.getUri()).thenReturn(new URI(WS_URI));
        
        handler.afterConnectionEstablished(session);

        verify(dockerClient).execCreateCmd("c1");
        verify(execStartCmd).exec(any(ResultCallback.class));
    }

    @Test
    void afterConnectionEstablishedNoContainerIdShouldClose() throws Exception {
        when(session.getUri()).thenReturn(new URI("ws://localhost")); 
        
        handler.afterConnectionEstablished(session);

        verify(session).close(CloseStatus.BAD_DATA);
        verify(dockerClient, never()).execCreateCmd(anyString());
    }

    @Test
    void handleTextMessageResizeCommand() throws Exception {
        when(session.getId()).thenReturn("s1");
        when(session.getUri()).thenReturn(new URI(WS_URI));
        handler.afterConnectionEstablished(session); 

        when(dockerClient.resizeContainerCmd("c1")).thenReturn(resizeContainerCmd);
        when(resizeContainerCmd.withSize(100, 50)).thenReturn(resizeContainerCmd);

        String json = "{\"type\":\"resize\", \"cols\":100, \"rows\":50}";
        handler.handleMessage(session, new TextMessage(json));

        verify(resizeContainerCmd).exec();
    }

    @Test
    void handleTextMessageStandardInput() throws Exception {
        when(session.getId()).thenReturn("s1");
        when(session.getUri()).thenReturn(new URI(WS_URI));
        handler.afterConnectionEstablished(session);

        ArgumentCaptor<InputStream> inputStreamCaptor = ArgumentCaptor.forClass(InputStream.class);
        verify(execStartCmd).withStdIn(inputStreamCaptor.capture());

        handler.handleMessage(session, new TextMessage("ls"));
    }

    @Test
    void afterConnectionClosedShouldCleanup() throws Exception {
        when(session.getId()).thenReturn("s1");
        when(session.getUri()).thenReturn(new URI(WS_URI));
        handler.afterConnectionEstablished(session);

        handler.afterConnectionClosed(session, CloseStatus.NORMAL);
    }
}