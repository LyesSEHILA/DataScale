package com.cyberscale.backend.dto;

public record DeployDecoyRequest(String userId, String decoyType) {
    // decoyType sera par exemple "mysql" ou "nginx"
}