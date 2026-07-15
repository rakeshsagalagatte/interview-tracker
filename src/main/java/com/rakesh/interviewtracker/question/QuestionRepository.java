package com.rakesh.interviewtracker.question;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    boolean existsByTitleIgnoreCase(String title);

    @Query(
            value = """
                    select distinct q
                    from Question q
                    left join q.tags tag
                    where (:search is null
                        or :search = ''
                        or lower(q.title) like lower(concat('%', :search, '%'))
                        or lower(q.topic) like lower(concat('%', :search, '%'))
                        or lower(tag) like lower(concat('%', :search, '%')))
                    and (:difficulty is null or q.difficulty = :difficulty)
                    and (:answerStatus is null or q.answerStatus = :answerStatus)
                    """,
            countQuery = """
                    select count(distinct q)
                    from Question q
                    left join q.tags tag
                    where (:search is null
                        or :search = ''
                        or lower(q.title) like lower(concat('%', :search, '%'))
                        or lower(q.topic) like lower(concat('%', :search, '%'))
                        or lower(tag) like lower(concat('%', :search, '%')))
                    and (:difficulty is null or q.difficulty = :difficulty)
                    and (:answerStatus is null or q.answerStatus = :answerStatus)
                    """
    )
    Page<Question> search(
            @Param("search") String search,
            @Param("difficulty") Difficulty difficulty,
            @Param("answerStatus") AnswerStatus answerStatus,
            Pageable pageable
    );
}
