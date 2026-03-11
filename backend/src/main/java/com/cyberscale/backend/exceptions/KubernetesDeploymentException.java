package com.cyberscale.backend.exceptions;

public class KubernetesDeploymentException extends RuntimeException {
    public KubernetesDeploymentException(String message) {
        super(message);
    }

    public KubernetesDeploymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
