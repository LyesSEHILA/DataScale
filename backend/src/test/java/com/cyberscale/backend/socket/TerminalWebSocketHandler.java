package com.cyberscale.backend.socket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.ExecCreateCmd; // Import corrigé
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
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
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TerminalWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(TerminalWebSocketHandler.class);

    @Autowired
    private DockerClient dockerClient;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, PipedOutputStream> activeOutputStreams = new ConcurrentHashMap<>();
    private final Map<String, ResultCallback<Frame>> activeCallbacks = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String containerId = getContainerId(session);
        if (containerId == null) {
            logger.error("Aucun containerId trouvé dans l'URL");
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        logger.info("🔌 Connexion Terminal (Exec) sur : " + containerId);

        try {
            // Création de l'exec Docker (bash)
            // ✅ Utilisation de la bonne classe ExecCreateCmd
            ExecCreateCmd cmd = dockerClient.execCreateCmd(containerId); 
            ExecCreateCmdResponse execResponse = cmd
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .withAttachStdin(true)
                    .withTty(true)
                    .withCmd("/bin/sh") // ou /bin/bash selon l'image
                    .exec();

            String execId = execResponse.getId();
            PipedOutputStream dockerInput = new PipedOutputStream();
            activeOutputStreams.put(session.getId(), dockerInput);

            // Callback pour recevoir les données de Docker
            ResultCallback<Frame> callback = new ResultCallback.Adapter<Frame>() {
                @Override
                public void onNext(Frame item) {
                    try {
                        if (session.isOpen()) {
                            session.sendMessage(new TextMessage(new String(item.getPayload(), StandardCharsets.UTF_8)));
                        }
                    } catch (IOException e) {
                        logger.error("Erreur envoi WebSocket", e);
                    }
                }
            };

            activeCallbacks.put(session.getId(), callback);

            // Démarrage de l'exec avec stdin connecté
            dockerClient.execStartCmd(execId)
                    .withTty(true)
                    .withStdIn(new java.io.PipedInputStream(dockerInput))
                    .exec(callback);

        } catch (Exception e) {
            logger.error("Impossible de lancer le shell", e);
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        PipedOutputStream dockerInput = activeOutputStreams.get(session.getId());
        if (dockerInput == null) return;

        try {
            String payload = message.getPayload();

            // ✅ CORRECTION : Activation du redimensionnement (Resize)
            if (payload.trim().startsWith("{")) {
                try {
                    JsonNode json = objectMapper.readTree(payload);
                    if (json.has("type") && "resize".equals(json.get("type").asText())) {
                        int cols = json.get("cols").asInt();
                        int rows = json.get("rows").asInt();
                        String containerId = getContainerId(session);
                        if (containerId != null) {
                            dockerClient.resizeContainerCmd(containerId)
                                    .withSize(cols, rows)
                                    .exec();
                            logger.info("Terminal resized: {}x{}", cols, rows);
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Erreur parsing JSON resize", e);
                }
                return;
            }

            // Envoi des touches au conteneur
            dockerInput.write(payload.getBytes(StandardCharsets.UTF_8));
            dockerInput.flush();

        } catch (IOException e) {
            logger.error("Erreur écriture vers Docker", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sId = session.getId();
        if (activeOutputStreams.containsKey(sId)) {
            try { activeOutputStreams.get(sId).close(); } catch (IOException e) {}
            activeOutputStreams.remove(sId);
        }
        if (activeCallbacks.containsKey(sId)) {
            try { activeCallbacks.get(sId).close(); } catch (IOException e) {}
            activeCallbacks.remove(sId);
        }
    }

    private String getContainerId(WebSocketSession session) {
        try {
            if (session.getUri() == null) return null;
            String query = session.getUri().getQuery();
            if (query == null) return null;
            
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2 && "containerId".equals(pair[0])) return pair[1];
            }
        } catch (Exception e) { return null; }
        return null;
    }
}