package com.rakesh.interviewtracker.question;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping
    public PageResponse<QuestionResponse> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Difficulty difficulty,
            @RequestParam(required = false, name = "status") AnswerStatus answerStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return questionService.findAll(search, difficulty, answerStatus, page, size);
    }

    @GetMapping("/{id}")
    public QuestionResponse findById(@PathVariable Long id) {
        return questionService.findById(id);
    }

    @PostMapping
    public ResponseEntity<QuestionResponse> create(
            @Valid @RequestBody QuestionRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        QuestionResponse response = questionService.create(request);
        URI location = uriBuilder.path("/api/questions/{id}").build(response.id());
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public QuestionResponse update(@PathVariable Long id, @Valid @RequestBody QuestionRequest request) {
        return questionService.update(id, request);
    }

    @PatchMapping("/{id}/review")
    public QuestionResponse markReviewed(@PathVariable Long id, @Valid @RequestBody ReviewRequest request) {
        return questionService.markReviewed(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        questionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
