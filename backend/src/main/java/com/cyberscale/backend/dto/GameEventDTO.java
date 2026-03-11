package com.cyberscale.backend.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public class GameEventDTO implements Serializable {
    private String playerId;
    private String action;      // Avant c'était "command", maintenant c'est "action"
    private String containerId;
    private LocalDateTime timestamp;

    public GameEventDTO() {}

    public GameEventDTO(String playerId, String action, String containerId) {
        this.playerId = playerId;
        this.action = action;
        this.containerId = containerId;
        this.timestamp = LocalDateTime.now();
    }

    // Getters & Setters
    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getContainerId() { return containerId; }
    public void setContainerId(String containerId) { this.containerId = containerId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}