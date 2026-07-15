package com.rakesh.interviewtracker.question;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AnswerStatus answerStatus;

    @Column(nullable = false, length = 100)
    private String topic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Difficulty difficulty;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "question_tags", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "tag", nullable = false, length = 50)
    private Set<String> tags = new LinkedHashSet<>();

    @Column(name = "last_reviewed_at")
    private LocalDate lastReviewedAt;

    @Column(name = "next_review_at")
    private LocalDate nextReviewAt;

    @Column(name = "review_count")
    private Integer reviewCount = 0;

    @Column(name = "confidence_level")
    private Integer confidenceLevel = 1;

    protected Question() {
    }

    public Question(String title, String answer, String topic, Difficulty difficulty,
                    Set<String> tags, AnswerStatus answerStatus) {
        update(title, answer, topic, difficulty, tags, answerStatus, null, null, 0, 1);
    }

    public void update(String title, String answer, String topic, Difficulty difficulty,
                       Set<String> tags, AnswerStatus answerStatus, LocalDate lastReviewedAt,
                       LocalDate nextReviewAt, Integer reviewCount, Integer confidenceLevel) {
        this.title = title;
        this.answer = answer;
        this.answerStatus = answerStatus == null ? AnswerStatus.DRAFT : answerStatus;
        this.topic = topic;
        this.difficulty = difficulty;
        this.tags = tags == null ? new LinkedHashSet<>() : new LinkedHashSet<>(tags);
        this.lastReviewedAt = lastReviewedAt;
        this.nextReviewAt = nextReviewAt;
        this.reviewCount = reviewCount == null ? 0 : reviewCount;
        this.confidenceLevel = confidenceLevel == null ? 1 : confidenceLevel;
    }

    public void markReviewed(LocalDate reviewDate, LocalDate nextReviewDate, Integer confidenceLevel) {
        this.answerStatus = AnswerStatus.REVIEWED;
        this.lastReviewedAt = reviewDate;
        this.nextReviewAt = nextReviewDate;
        this.reviewCount = getReviewCount() + 1;
        this.confidenceLevel = confidenceLevel == null ? getConfidenceLevel() : confidenceLevel;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getAnswer() { return answer; }
    public AnswerStatus getAnswerStatus() { return answerStatus; }
    public String getTopic() { return topic; }
    public Difficulty getDifficulty() { return difficulty; }
    public Set<String> getTags() { return Set.copyOf(tags); }
    public LocalDate getLastReviewedAt() { return lastReviewedAt; }
    public LocalDate getNextReviewAt() { return nextReviewAt; }
    public Integer getReviewCount() { return reviewCount == null ? 0 : reviewCount; }
    public Integer getConfidenceLevel() { return confidenceLevel == null ? 1 : confidenceLevel; }
}
