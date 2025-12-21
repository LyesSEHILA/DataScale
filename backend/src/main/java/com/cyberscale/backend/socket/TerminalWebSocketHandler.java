package com.cyberscale.backend.socket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.AttachContainerCmd;
import com.github.dockerjava.api.model.Frame;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TerminalWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(TerminalWebSocketHandler.class);

    @Autowired
    private DockerClient dockerClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // On garde une map pour stocker les Streams d'entrée de chaque session active
    // SessionID -> OutputStream vers Docker
    private final Map<String, PipedOutputStream> activeOutputStreams = new ConcurrentHashMap<>();
    
    // Pour fermer proprement les callbacks Docker
    private final Map<String, ResultCallback.Adapter<Frame>> activeCallbacks = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 1. Récupérer l'ID du conteneur depuis l'URL (ex: ws://localhost:8080/ws/terminal?containerId=xxx)
        String query = session.getUri().getQuery();
        String containerId = getQueryParam(query, "containerId");

        if (containerId == null) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        PipedOutputStream wsToDocker = new PipedOutputStream();
        PipedInputStream dockerInputStream = new PipedInputStream(wsToDocker);
        
        activeOutputStreams.put(session.getId(), wsToDocker);

        AttachContainerCmd attachCmd = dockerClient.attachContainerCmd(containerId)
                .withStdErr(true)
                .withStdOut(true)
                .withFollowStream(true)
                .withStdIn(dockerInputStream); 

        ResultCallback.Adapter<Frame> callback = new ResultCallback.Adapter<>() {
            @Override
            public void onNext(Frame item) {
                try {
                    // On envoie le texte brut au Frontend
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
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        PipedOutputStream dockerInput = activeOutputStreams.get(session.getId());

        if (dockerInput == null) return;

        if (payload.startsWith("{") && payload.contains("cols")) {
            try {
                JsonNode json = objectMapper.readTree(payload);
                if (json.has("type") && "resize".equals(json.get("type").asText())) {
                    String containerId = getQueryParam(session.getUri().getQuery(), "containerId");
                    int rows = json.get("rows").asInt();
                    int cols = json.get("cols").asInt();

                    dockerClient.resizeContainerCmd(containerId)
                            .withSize(cols, rows)
                            .exec();
                            
                    return; 
                }
            } catch (Exception e) {

            }
        }
        dockerInput.write(payload.getBytes(StandardCharsets.UTF_8));
        dockerInput.flush();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        
        if (activeOutputStreams.containsKey(sessionId)) {
            activeOutputStreams.get(sessionId).close();
            activeOutputStreams.remove(sessionId);
        }
        
        if (activeCallbacks.containsKey(sessionId)) {
            activeCallbacks.get(sessionId).close();
            activeCallbacks.remove(sessionId);
        }
    }

    // Utilitaire simple pour parser la Query String (?containerId=123)
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