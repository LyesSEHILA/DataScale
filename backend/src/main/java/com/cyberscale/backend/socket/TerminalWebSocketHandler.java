package com.cyberscale.backend.socket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.AttachContainerCmd;
import com.github.dockerjava.api.model.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TerminalWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(TerminalWebSocketHandler.class);

    @Autowired
    private DockerClient dockerClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // SessionID -> OutputStream vers Docker
    private final Map<String, PipedOutputStream> activeOutputStreams = new ConcurrentHashMap<>();
    
    // Pour fermer proprement les callbacks Docker
    private final Map<String, ResultCallback.Adapter<Frame>> activeCallbacks = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String containerId = getContainerId(session);

        if (containerId == null) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        PipedOutputStream wsToDocker = new PipedOutputStream();
        PipedInputStream dockerInputStream = new PipedInputStream(wsToDocker);
        
        activeOutputStreams.put(session.getId(), wsToDocker);

        // On branche le tuyau d'entrée (Stdin) ICI
        AttachContainerCmd attachCmd = dockerClient.attachContainerCmd(containerId)
                .withStdErr(true)
                .withStdOut(true)
                .withFollowStream(true)
                .withStdIn(dockerInputStream); 

        ResultCallback.Adapter<Frame> callback = new ResultCallback.Adapter<>() {
            @Override
            public void onNext(Frame item) {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(new String(item.getPayload(), StandardCharsets.UTF_8)));
                    }
                } catch (IOException e) {
                    logger.error("Erreur lors de l'envoi du flux Docker vers WebSocket", e);
                }
            }
            
            @Override
            public void onError(Throwable throwable) {
                try {
                    session.close();
                } catch (IOException e) {
                   // ignore
                }
            }
        };

        attachCmd.exec(callback);
        activeCallbacks.put(session.getId(), callback);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        PipedOutputStream dockerInput = activeOutputStreams.get(session.getId());

        if (dockerInput == null) return;

        try {
            boolean isCommand = false;

            // 1. On regarde si ça ressemble à une commande JSON (resize)
            if (payload.trim().startsWith("{")) {
                try {
                    JsonNode node = objectMapper.readTree(payload);
                    if (node.has("type") && "resize".equals(node.get("type").asText())) {
                        String containerId = getContainerId(session);
                        if (containerId != null) {
                            int rows = node.has("rows") ? node.get("rows").asInt() : 24;
                            int cols = node.has("cols") ? node.get("cols").asInt() : 80;
                            
                            // CORRECTION ICI : Inversion rows/cols -> withSize(cols, rows)
                            dockerClient.resizeContainerCmd(containerId).withSize(cols, rows).exec();
                        }
                        isCommand = true; 
                    }
                } catch (Exception ignored) {
                    // Pas du JSON valide, on ignore
                }
            }

            // 2. Si ce n'était pas une commande système, on envoie au conteneur
            if (!isCommand) {
                dockerInput.write(payload.getBytes(StandardCharsets.UTF_8));
                dockerInput.flush();
            }

        } catch (IOException e) {
            logger.error("Erreur écriture vers Docker", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        
        if (activeOutputStreams.containsKey(sessionId)) {
            try {
                activeOutputStreams.get(sessionId).close();
            } catch (IOException e) {
                // ignore
            }
            activeOutputStreams.remove(sessionId);
        }
        
        if (activeCallbacks.containsKey(sessionId)) {
            try {
                activeCallbacks.get(sessionId).close();
            } catch (IOException e) {
                // ignore
            }
            activeCallbacks.remove(sessionId);
        }
    }

    private String getContainerId(WebSocketSession session) {
        try {
            return getQueryParam(session.getUri().getQuery(), "containerId");
        } catch (Exception e) {
            return null;
        }
    }

    private String getQueryParam(String query, String param) {
        if (query == null) return null;
        for (String pair : query.split("&")) {
            String[] split = pair.split("=");
            if (split.length == 2 && split[0].equals(param)) {
                return split[1];
            }
        }
        return null;
    }
}