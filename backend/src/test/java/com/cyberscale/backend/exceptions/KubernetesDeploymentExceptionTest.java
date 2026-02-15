package com.cyberscale.backend.exceptions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class KubernetesDeploymentExceptionTest {

    @Test
    void testConstructors() {
        // Test constructeur avec message
        KubernetesDeploymentException ex1 = new KubernetesDeploymentException("Erreur K8s");
        assertEquals("Erreur K8s", ex1.getMessage());

        // Test constructeur avec message et cause
        RuntimeException cause = new RuntimeException("Cause racine");
        KubernetesDeploymentException ex2 = new KubernetesDeploymentException("Erreur K8s", cause);
        assertEquals("Erreur K8s", ex2.getMessage());
        assertEquals(cause, ex2.getCause());
    }
}
