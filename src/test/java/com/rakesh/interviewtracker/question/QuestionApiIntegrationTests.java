package com.rakesh.interviewtracker.question;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class QuestionApiIntegrationTests {

    private static final String VALID_QUESTION = """
            {
              "title": "What is dependency injection?",
              "answer": "Dependencies are provided to an object instead of being created by it.",
              "topic": "Spring",
              "difficulty": "EASY",
              "tags": ["java", "spring"]
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private QuestionRepository questionRepository;

    @BeforeEach
    void clearQuestions() {
        questionRepository.deleteAll();
    }

    @Test
    void supportsCompleteCrudLifecycle() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_QUESTION))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.title").value("What is dependency injection?"))
                .andExpect(jsonPath("$.difficulty").value("EASY"))
                .andReturn();

        String location = createResult.getResponse().getHeader("Location");

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topic").value("Spring"))
                .andExpect(jsonPath("$.tags.length()").value(2));

        String updatedQuestion = """
                {
                  "title": "Explain dependency injection",
                  "answer": "A container supplies an object's dependencies.",
                  "topic": "Design Patterns",
                  "difficulty": "MEDIUM",
                  "tags": ["architecture"]
                }
                """;

        mockMvc.perform(put(location)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedQuestion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Explain dependency injection"))
                .andExpect(jsonPath("$.difficulty").value("MEDIUM"));

        mockMvc.perform(get("/api/questions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(delete(location))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(location))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void rejectsInvalidQuestions() throws Exception {
        String invalidQuestion = """
                {
                  "title": " ",
                  "answer": "",
                  "topic": "Java",
                  "difficulty": null,
                  "tags": []
                }
                """;

        mockMvc.perform(post("/api/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidQuestion))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.validationErrors.title").exists())
                .andExpect(jsonPath("$.validationErrors.answer").exists())
                .andExpect(jsonPath("$.validationErrors.difficulty").exists());
    }
}
