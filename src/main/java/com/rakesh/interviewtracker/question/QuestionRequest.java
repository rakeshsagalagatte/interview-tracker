package com.rakesh.interviewtracker.question;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

public record QuestionRequest(
        @NotBlank @Size(max = 500) String title,
        @NotBlank String answer,
        AnswerStatus answerStatus,
        @NotBlank @Size(max = 100) String topic,
        @NotNull Difficulty difficulty,
        Set<@NotBlank @Size(max = 50) String> tags,
        LocalDate lastReviewedAt,
        LocalDate nextReviewAt,
        @Min(0) Integer reviewCount,
        @Min(1) @Max(5) Integer confidenceLevel
) {
}
