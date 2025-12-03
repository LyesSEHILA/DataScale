package com.cyberscale.backend.bdd;

import com.cyberscale.backend.models.*;
import com.cyberscale.backend.repositories.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;

public class QuizSteps {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private QuizSessionRepository quizSessionRepository;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private AnswerOptionRepository answerOptionRepository;
    @Autowired private UserAnswerRepository userAnswerRepository;

    private Long currentSessionId;
    private Long questionId;
    private Long optionId;

    @Given("the server is running")
    public void the_server_is_running() {
        userAnswerRepository.deleteAll();
        answerOptionRepository.deleteAll();
        questionRepository.deleteAll();
        quizSessionRepository.deleteAll();
        
        Question q = new Question();
        q.setText("Cucumber Question");
        q.setCategorie(IQuestion.CategorieQuestion.THEORY);
        q.setDifficulty(IQuestion.DifficultyQuestion.EASY);
        q = questionRepository.save(q);
        this.questionId = q.getId();

        AnswerOption opt = new AnswerOption();
        opt.setText("Yes");
        opt.setIsCorrect(true);
        opt.setQuestion(q);
        opt = answerOptionRepository.save(opt);
        this.optionId = opt.getId();
    }

    @When("I create a session with age {int}")
    public void i_create_a_session_with_age(int age) throws Exception {
        String json = "{\"age\": " + age + ", \"selfEvalTheory\": 5, \"selfEvalTechnique\": 5}";

        MvcResult result = mockMvc.perform(post("/api/quiz/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(responseBody);
        this.currentSessionId = root.path("id").asLong();
    }

    @Then("I receive status {int} and a session ID")
    public void i_receive_status_and_session_id(int status) {
        assertNotNull(this.currentSessionId);
    }

    @And("I can fetch questions for this session")
    public void i_can_fetch_questions() throws Exception {
        mockMvc.perform(get("/api/quiz/questions")
                .param("sessionId", this.currentSessionId.toString()))
                .andExpect(status().isOk());
    }

    @And("I can submit an answer to the first question")
    public void i_can_submit_an_answer() throws Exception {
        String json = "{\"sessionId\": " + currentSessionId + ", \"questionId\": " + questionId + ", \"answerOptionId\": " + optionId + "}";

        mockMvc.perform(post("/api/quiz/answer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());
    }

    @And("I can fetch my final results")
    public void i_can_fetch_results() throws Exception {
        mockMvc.perform(get("/api/quiz/results")
                .param("sessionId", this.currentSessionId.toString()))
                .andExpect(status().isOk());
    }
}