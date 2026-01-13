package com.team2.nextpage.view.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 화면 연결을 위한 View Controller
 * Thymeleaf 템플릿과 URL을 매핑합니다.
 */
@Controller
public class ViewController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/mypage")
    public String mypage() {
        return "mypage";
    }

    @GetMapping("/websocket-test")
    public String websocketTest() {
        return "websocket-test";
    }

    @GetMapping("/books/new")
    public String createBook() {
        return "create-book";
    }

    @GetMapping("/books/{bookId}")
    public String bookDetail(@PathVariable Long bookId) {
        return "book-detail";
    }

    @GetMapping("/books/{bookId}/viewer")
    public String viewer(@PathVariable Long bookId) {
        return "viewer";
    }
}
