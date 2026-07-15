package com.rakesh.interviewtracker.question;

import com.rakesh.interviewtracker.question.exception.QuestionNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class QuestionService {

    private static final int MAX_PAGE_SIZE = 100;

    private final QuestionRepository questionRepository;

    public QuestionService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public PageResponse<QuestionResponse> findAll(
            String search,
            Difficulty difficulty,
            AnswerStatus answerStatus,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        PageRequest pageRequest = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.ASC, "id"));

        return PageResponse.from(questionRepository.search(search, difficulty, answerStatus, pageRequest)
                .map(QuestionResponse::from));
    }

    public QuestionResponse findById(Long id) {
        return QuestionResponse.from(getQuestion(id));
    }

    @Transactional
    public QuestionResponse create(QuestionRequest request) {
        Question question = new Question(
                request.title(), request.answer(), request.topic(),
                request.difficulty(), request.tags(), request.answerStatus()
        );
        question.update(
                request.title(), request.answer(), request.topic(), request.difficulty(),
                request.tags(), request.answerStatus(), request.lastReviewedAt(), request.nextReviewAt(),
                request.reviewCount(), request.confidenceLevel()
        );
        return QuestionResponse.from(questionRepository.save(question));
    }

    @Transactional
    public QuestionResponse update(Long id, QuestionRequest request) {
        Question question = getQuestion(id);
        question.update(
                request.title(), request.answer(), request.topic(),
                request.difficulty(), request.tags(), request.answerStatus(),
                request.lastReviewedAt(), request.nextReviewAt(), request.reviewCount(),
                request.confidenceLevel()
        );
        return QuestionResponse.from(question);
    }

    @Transactional
    public QuestionResponse markReviewed(Long id, ReviewRequest request) {
        Question question = getQuestion(id);
        LocalDate reviewedAt = request.reviewedAt() == null ? LocalDate.now() : request.reviewedAt();
        LocalDate nextReviewAt = request.nextReviewAt() == null ? reviewedAt.plusDays(7) : request.nextReviewAt();
        question.markReviewed(reviewedAt, nextReviewAt, request.confidenceLevel());
        return QuestionResponse.from(question);
    }

    @Transactional
    public void delete(Long id) {
        Question question = getQuestion(id);
        questionRepository.delete(question);
    }

    @Transactional
    public boolean createSeedQuestion(QuestionSeed seed) {
        if (questionRepository.existsByTitleIgnoreCase(seed.title())) {
            return false;
        }
        questionRepository.save(new Question(
                seed.title(), seed.answer(), seed.topic(), seed.difficulty(),
                seed.tags(), AnswerStatus.REVIEWED
        ));
        return true;
    }

    private Question getQuestion(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new QuestionNotFoundException(id));
    }
}
