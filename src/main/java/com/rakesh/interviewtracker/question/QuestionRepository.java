package com.rakesh.interviewtracker.question;

import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    boolean existsByTitleIgnoreCase(String title);
}
