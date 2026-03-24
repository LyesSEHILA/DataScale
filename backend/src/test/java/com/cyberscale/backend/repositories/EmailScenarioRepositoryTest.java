package com.cyberscale.backend.repositories;

import com.cyberscale.backend.models.EmailScenario;
import com.cyberscale.backend.models.IQuestion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // Utilise PostgreSQL si dispo ou H2 config
@ActiveProfiles("test")
class EmailScenarioRepositoryTest {

    @Autowired
    private EmailScenarioRepository repository;

    @Test
    void shouldSaveAndFindEmailScenario() {
        // GIVEN
        List<Map<String, Object>> traps = new ArrayList<>();
        traps.add(Map.of("id", "trap-1", "message", "Lien suspect"));
        
        EmailScenario scenario = new EmailScenario();
        scenario.setSender("Hacker <hacker@evil.com>");
        scenario.setSubject("Urgent: Votre compte est bloqué");
        scenario.setContentHtml("<p>Cliquez ici !</p>");
        scenario.setDifficulty(IQuestion.DifficultyQuestion.EASY);
        scenario.setTraps(traps);

        // WHEN
        EmailScenario saved = repository.save(scenario);
        Optional<EmailScenario> found = repository.findById(saved.getId());

        // THEN
        assertTrue(found.isPresent());
        assertEquals("Hacker <hacker@evil.com>", found.get().getSender());
        assertEquals(1, found.get().getTraps().size());
        assertEquals("trap-1", found.get().getTraps().get(0).get("id"));
        assertEquals(IQuestion.DifficultyQuestion.EASY, found.get().getDifficulty());
    }

    @Test
    void shouldFindAllScenarios() {
        repository.deleteAll();
        
        EmailScenario s1 = new EmailScenario("Sender 1", "Sujet 1", "HTML 1", IQuestion.DifficultyQuestion.MEDIUM, new ArrayList<>());
        EmailScenario s2 = new EmailScenario("Sender 2", "Sujet 2", "HTML 2", IQuestion.DifficultyQuestion.HARD, new ArrayList<>());
        
        repository.save(s1);
        repository.save(s2);

        List<EmailScenario> all = repository.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void shouldReturnEmpty_WhenIdDoesNotExist() {
        Optional<EmailScenario> found = repository.findById(9999L);
        assertFalse(found.isPresent());
    }
}