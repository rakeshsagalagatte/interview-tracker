package com.rakesh.interviewtracker;

import com.rakesh.interviewtracker.question.AnswerStatus;
import com.rakesh.interviewtracker.question.QuestionRepository;
import com.rakesh.interviewtracker.question.QuestionSeedImporter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class InterviewTrackerApplicationTests {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionSeedImporter questionSeedImporter;

    @Test
    void importsAllReviewedSeedQuestionsIdempotently() throws Exception {
        assertThat(questionRepository.count()).isEqualTo(50);
        assertThat(questionRepository.findAll())
                .allMatch(question -> question.getAnswerStatus() == AnswerStatus.REVIEWED)
                .allMatch(question -> !question.getAnswer().isBlank());

        questionSeedImporter.run(new DefaultApplicationArguments());

        assertThat(questionRepository.count()).isEqualTo(50);
    }
}
