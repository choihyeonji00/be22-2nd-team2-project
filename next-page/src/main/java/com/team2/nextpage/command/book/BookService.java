package com.team2.nextpage.command.book;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 소설 Command 서비스 (생성, 문장 작성)
 *
 * @author 최현지
 */
@Service
@Transactional
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /**
     * 소설 방 생성
     */
    public void createBook() {
        // Implementation
    }

    /**
     * 문장 이어 쓰기
     */
    public void appendSentence(Long bookId) {
        // Implementation
    }
}
