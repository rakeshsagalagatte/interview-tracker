package com.rakesh.interviewtracker.question;

import java.util.Set;

public record QuestionResponse(
        Long id,
        String title,
        String answer,
        AnswerStatus answerStatus,
        String topic,
        Difficulty difficulty,
        Set<String> tags
) {
    public static QuestionResponse from(Question question) {
        return new QuestionResponse(
                question.getId(), question.getTitle(), question.getAnswer(), question.getAnswerStatus(),
                question.getTopic(), question.getDifficulty(), question.getTags()
        );
    }
}
