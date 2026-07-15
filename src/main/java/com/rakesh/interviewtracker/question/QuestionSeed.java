package com.rakesh.interviewtracker.question;

import java.util.Set;

public record QuestionSeed(
        String title,
        String answer,
        String topic,
        Difficulty difficulty,
        Set<String> tags
) {
}
