package com.cyberscale.backend.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ModelIntegrityTest {

    @Test
    void testUser_GettersSetters() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setPassword("password");
        user.setPoints(50);

        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("test@test.com", user.getEmail());
        assertEquals("password", user.getPassword());
        assertEquals(50, user.getPoints());
    }

    @Test
    void testChallenge_ConstructeurEtGetters() {
        Challenge challenge = new Challenge("C1", "Web Attack", "Exploit SQLi", "FLAG{TEST}", 100);

        assertEquals("C1", challenge.getId());
        assertEquals("Web Attack", challenge.getName());
        assertEquals("Exploit SQLi", challenge.getDescription());
        assertEquals("FLAG{TEST}", challenge.getFlagSecret());
        assertEquals(100, challenge.getPointsReward());
    }

    @Test
    void testAnswerOption_GettersSetters() {
        AnswerOption option = new AnswerOption();
        option.setId(1L);
        option.setText("La bonne réponse");
        option.setIsCorrect(true);

        assertEquals(1L, option.getId());
        assertEquals("La bonne réponse", option.getText());
        assertTrue(option.getIsCorrect());
    }
}