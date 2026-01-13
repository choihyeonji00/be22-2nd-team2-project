package com.team2.nextpage.query.book.controller;

import com.team2.nextpage.query.book.dto.request.BookSearchRequest;
import com.team2.nextpage.query.book.dto.response.BookDetailDto;
import com.team2.nextpage.query.book.dto.response.BookDto;
import com.team2.nextpage.query.book.dto.response.BookPageResponse;
import com.team2.nextpage.query.book.service.BookQueryService;
import com.team2.nextpage.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 소설 Query 컨트롤러
 *
 * @author 정진호
 */
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookQueryController {

    private final BookQueryService bookQueryService;

    /**
     * 소설 목록 조회 API (페이징/정렬/필터링)
     * GET /api/books
     *
     * @param request 검색 조건 (page, size, sortBy, sortOrder, status, categoryId,
     *                keyword)
     * @return 페이징된 소설 목록
     */
    @GetMapping
    public ResponseEntity<ApiResponse<BookPageResponse>> list(@ModelAttribute BookSearchRequest request) {
        BookPageResponse result = bookQueryService.searchBooks(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 소설 상세 조회 API
     * GET /api/books/{bookId}
     *
     * @param bookId 소설 ID
     * @return 소설 기본 정보
     */
    @GetMapping("/{bookId}")
    public ResponseEntity<ApiResponse<BookDto>> detail(@PathVariable Long bookId) {
        return ResponseEntity.ok(ApiResponse.success(bookQueryService.getBook(bookId)));
    }

    /**
     * 소설 뷰어 모드 조회 API (문장 목록 포함)
     * GET /api/books/{bookId}/view
     *
     * @param bookId 소설 ID
     * @return 소설 상세 정보 (문장 목록, 투표 통계 포함)
     */
    @GetMapping("/{bookId}/view")
    public ResponseEntity<ApiResponse<BookDetailDto>> view(@PathVariable Long bookId) {
        BookDetailDto bookDetail = bookQueryService.getBookForViewer(bookId);
        return ResponseEntity.ok(ApiResponse.success(bookDetail));
    }
}
