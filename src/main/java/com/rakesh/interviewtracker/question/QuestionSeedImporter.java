package com.rakesh.interviewtracker.question;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

@Component
public class QuestionSeedImporter implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuestionSeedImporter.class);
    private static final String SEED_FILE = "seed/java-stream-questions.tsv";

    private final QuestionService questionService;

    public QuestionSeedImporter(QuestionService questionService) {
        this.questionService = questionService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ClassPathResource resource = new ClassPathResource(SEED_FILE);
        int imported = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }
                String[] fields = line.split("\\t", -1);
                if (fields.length != 5) {
                    throw new IllegalStateException("Invalid seed data at line " + lineNumber);
                }
                Set<String> tags = new LinkedHashSet<>(Arrays.asList(fields[4].split(",")));
                QuestionSeed seed = new QuestionSeed(
                        fields[0], fields[3].replace("\\n", "\n"), fields[1],
                        Difficulty.valueOf(fields[2]), tags
                );
                if (questionService.createSeedQuestion(seed)) {
                    imported++;
                }
            }
        }

        LOGGER.info("Imported {} new interview questions from {}", imported, SEED_FILE);
    }
}
