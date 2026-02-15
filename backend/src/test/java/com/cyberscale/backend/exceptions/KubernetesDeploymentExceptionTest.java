package com.cyberscale.backend.exceptions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class KubernetesDeploymentExceptionTest {

    private static final String ERROR_MSG = "Erreur K8s";

    @Test
    void testConstructors() {
        // Test constructeur avec message
        KubernetesDeploymentException ex1 = new KubernetesDeploymentException(ERROR_MSG);
        assertEquals(ERROR_MSG, ex1.getMessage());

        // Test constructeur avec message et cause
        RuntimeException cause = new RuntimeException("Cause racine");
        KubernetesDeploymentException ex2 = new KubernetesDeploymentException(ERROR_MSG, cause);
        assertEquals(ERROR_MSG, ex2.getMessage());
        assertEquals(cause, ex2.getCause());
    }
}
