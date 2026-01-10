package com.team2.nextpage.command.book;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 소설 Command 컨트롤러
 *
 * @author 최현지
 */
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * 소설 생성 API
     */
    @PostMapping
    public void create() {
        // impl
    }

    /**
     * 문장 이어쓰기 API
     */
    @PostMapping("/{bookId}/sentences")
    public void append(@PathVariable Long bookId) {
        // impl
    }
}
