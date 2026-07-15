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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
                .andExpect(jsonPath("$.reviewCount").value(0))
                .andExpect(jsonPath("$.confidenceLevel").value(1))
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
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.page").value(0));

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

    @Test
    void searchesFiltersAndPaginatesQuestions() throws Exception {
        mockMvc.perform(post("/api/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Explain Java streams",
                                  "answer": "Streams process collections declaratively.",
                                  "answerStatus": "REVIEWED",
                                  "topic": "Java Streams",
                                  "difficulty": "EASY",
                                  "tags": ["java", "streams"]
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Explain Spring dependency injection",
                                  "answer": "Spring wires dependencies into beans.",
                                  "answerStatus": "DRAFT",
                                  "topic": "Spring",
                                  "difficulty": "MEDIUM",
                                  "tags": ["java", "spring"]
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Explain SQL joins",
                                  "answer": "Joins combine rows from related tables.",
                                  "answerStatus": "REVIEWED",
                                  "topic": "SQL",
                                  "difficulty": "HARD",
                                  "tags": ["database"]
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/questions")
                        .param("search", "java")
                        .param("difficulty", "EASY")
                        .param("status", "REVIEWED")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Explain Java streams"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));

        mockMvc.perform(get("/api/questions")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.page").value(1));
    }

    @Test
    void tracksReviewProgress() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Explain Optional",
                                  "answer": "Optional represents a value that may be absent.",
                                  "answerStatus": "DRAFT",
                                  "topic": "Java",
                                  "difficulty": "EASY",
                                  "tags": ["java", "optional"],
                                  "reviewCount": 2,
                                  "confidenceLevel": 3,
                                  "lastReviewedAt": "2026-07-01",
                                  "nextReviewAt": "2026-07-08"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reviewCount").value(2))
                .andExpect(jsonPath("$.confidenceLevel").value(3))
                .andExpect(jsonPath("$.lastReviewedAt").value("2026-07-01"))
                .andExpect(jsonPath("$.nextReviewAt").value("2026-07-08"))
                .andReturn();

        String location = createResult.getResponse().getHeader("Location");

        mockMvc.perform(patch(location + "/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reviewedAt": "2026-07-16",
                                  "nextReviewAt": "2026-07-23",
                                  "confidenceLevel": 4
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answerStatus").value("REVIEWED"))
                .andExpect(jsonPath("$.reviewCount").value(3))
                .andExpect(jsonPath("$.confidenceLevel").value(4))
                .andExpect(jsonPath("$.lastReviewedAt").value("2026-07-16"))
                .andExpect(jsonPath("$.nextReviewAt").value("2026-07-23"));
    }
}
