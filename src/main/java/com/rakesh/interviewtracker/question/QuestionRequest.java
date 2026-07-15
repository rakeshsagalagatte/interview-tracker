package com.rakesh.interviewtracker.question;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record QuestionRequest(
        @NotBlank @Size(max = 500) String title,
        @NotBlank String answer,
        AnswerStatus answerStatus,
        @NotBlank @Size(max = 100) String topic,
        @NotNull Difficulty difficulty,
        Set<@NotBlank @Size(max = 50) String> tags
) {
}
