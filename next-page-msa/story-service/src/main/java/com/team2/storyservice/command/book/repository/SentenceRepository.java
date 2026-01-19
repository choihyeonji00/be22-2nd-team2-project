package com.team2.storyservice.command.book.repository;

import com.team2.storyservice.command.book.entity.Book;
import com.team2.storyservice.command.book.entity.Sentence;

import java.util.Optional;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

/**
 * 문장(Sentence) Command Repository
 *
 * @author 정진호
 */
public interface SentenceRepository extends JpaRepository<Sentence, Long> {

        @Modifying
        @Query("UPDATE Sentence s SET s.sequenceNo = s.sequenceNo - 1 WHERE s.book.bookId = :bookId AND s.sequenceNo > :sequenceNo")
        void decreaseSequenceAfter(@Param("bookId") Long bookId,
                        @Param("sequenceNo") Integer sequenceNo);

        Optional<Sentence> findByBookAndSequenceNo(Book book, Integer sequenceNo);

        int countByWriterId(Long writerId);
}
