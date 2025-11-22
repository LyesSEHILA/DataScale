package com.cyberscale.backend.bdd;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import io.cucumber.spring.CucumberContextConfiguration;

@CucumberContextConfiguration
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = "spring.sql.init.data-locations=") // On ignore data.sql comme d'habitude
public class CucumberSpringConfiguration {
    // Cette classe sert juste de point d'entrée pour la configuration
}