package com.rakesh.interviewtracker.question;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalDate;

public record ReviewRequest(
        LocalDate reviewedAt,
        LocalDate nextReviewAt,
        @Min(1) @Max(5) Integer confidenceLevel
) {
}
