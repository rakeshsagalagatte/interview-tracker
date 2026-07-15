package com.rakesh.interviewtracker.question;

import com.rakesh.interviewtracker.question.exception.QuestionNotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class QuestionService {

    private final QuestionRepository questionRepository;

    public QuestionService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public List<QuestionResponse> findAll() {
        return questionRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(QuestionResponse::from)
                .toList();
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
        return QuestionResponse.from(questionRepository.save(question));
    }

    @Transactional
    public QuestionResponse update(Long id, QuestionRequest request) {
        Question question = getQuestion(id);
        question.update(
                request.title(), request.answer(), request.topic(),
                request.difficulty(), request.tags(), request.answerStatus()
        );
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
