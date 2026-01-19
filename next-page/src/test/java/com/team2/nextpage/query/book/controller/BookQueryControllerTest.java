package com.team2.nextpage.query.book.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team2.nextpage.command.book.controller.BookController;
import com.team2.nextpage.command.reaction.controller.ReactionController;
import com.team2.nextpage.common.util.SecurityUtil;
import com.team2.nextpage.jwt.JwtAuthenticationFilter;
import com.team2.nextpage.jwt.JwtTokenProvider;
import com.team2.nextpage.query.book.dto.request.BookSearchRequest;
import com.team2.nextpage.query.book.dto.response.*;
import com.team2.nextpage.query.book.service.BookQueryService;
import com.team2.nextpage.query.reaction.controller.ReactionQueryController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookQueryController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookQueryService bookQueryService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // Controllers for HATEOAS
    @MockitoBean
    private ReactionQueryController reactionQueryController;

    @MockitoBean
    private ReactionController reactionController;

    @MockitoBean
    private BookController bookController;

    @Test
    @DisplayName("소설 목록 조회 - 성공")
    void listBooks_Success() throws Exception {
        // given
        BookDto book1 = new BookDto();
        book1.setBookId(1L);
        book1.setTitle("Test Book 1");
        book1.setStatus("IN_PROGRESS");
        book1.setWriterId(1L);
        book1.setCategoryId("FANTASY");
        book1.setCurrentSequence(5);
        book1.setMaxSequence(10);
        book1.setCreatedAt(LocalDateTime.now());

        BookDto book2 = new BookDto();
        book2.setBookId(2L);
        book2.setTitle("Test Book 2");
        book2.setStatus("COMPLETED");
        book2.setWriterId(2L);
        book2.setCategoryId("ROMANCE");
        book2.setCurrentSequence(10);
        book2.setMaxSequence(10);
        book2.setCreatedAt(LocalDateTime.now());

        BookPageResponse response = new BookPageResponse(
            Arrays.asList(book1, book2),
            0,
            10,
            2L
        );

        given(bookQueryService.searchBooks(any(BookSearchRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/books")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "createdAt")
                        .param("sortOrder", "DESC")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].bookId").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("Test Book 1"))
                .andExpect(jsonPath("$.data.content[1].bookId").value(2))
                .andExpect(jsonPath("$.data.totalElements").value(2));

        verify(bookQueryService).searchBooks(any(BookSearchRequest.class));
    }

    @Test
    @DisplayName("소설 목록 조회 - 필터링 적용")
    void listBooks_WithFilters() throws Exception {
        // given
        BookPageResponse response = new BookPageResponse(
            Collections.emptyList(),
            0,
            10,
            0L
        );

        given(bookQueryService.searchBooks(any(BookSearchRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/books")
                        .param("status", "COMPLETED")
                        .param("categoryId", "FANTASY")
                        .param("keyword", "test")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(0));

        verify(bookQueryService).searchBooks(any(BookSearchRequest.class));
    }

    @Test
    @DisplayName("소설 목록 조회 - 빈 결과")
    void listBooks_EmptyContent() throws Exception {
        // given
        BookPageResponse response = new BookPageResponse(
            null,
            0,
            10,
            0L
        );

        given(bookQueryService.searchBooks(any(BookSearchRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/books")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    @DisplayName("소설 상세 조회 - 성공")
    void detailBook_Success() throws Exception {
        // given
        Long bookId = 1L;
        BookDto book = new BookDto();
        book.setBookId(bookId);
        book.setTitle("Test Book");
        book.setStatus("IN_PROGRESS");
        book.setWriterId(1L);
        book.setCategoryId("FANTASY");
        book.setCurrentSequence(5);
        book.setMaxSequence(10);
        book.setCreatedAt(LocalDateTime.now());

        given(bookQueryService.getBook(bookId)).willReturn(book);

        // when & then
        mockMvc.perform(get("/api/books/{bookId}", bookId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.bookId").value(bookId))
                .andExpect(jsonPath("$.data.title").value("Test Book"))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));

        verify(bookQueryService).getBook(bookId);
    }

    @Test
    @DisplayName("소설 뷰어 조회 - IN_PROGRESS 상태")
    void viewBook_InProgressStatus() throws Exception {
        // given
        Long bookId = 1L;
        SentenceDto sentence1 = new SentenceDto();
        sentence1.setSentenceId(1L);
        sentence1.setSequenceNo(1);
        sentence1.setContent("First sentence");
        sentence1.setWriterId(1L);
        sentence1.setWriterNicknm("writer1");

        SentenceDto sentence2 = new SentenceDto();
        sentence2.setSentenceId(2L);
        sentence2.setSequenceNo(2);
        sentence2.setContent("Second sentence");
        sentence2.setWriterId(2L);
        sentence2.setWriterNicknm("writer2");

        BookDetailDto bookDetail = new BookDetailDto();
        bookDetail.setBookId(bookId);
        bookDetail.setTitle("Test Book");
        bookDetail.setStatus("IN_PROGRESS");
        bookDetail.setWriterId(1L);
        bookDetail.setWriterNicknm("mainWriter");
        bookDetail.setCategoryId("FANTASY");
        bookDetail.setCurrentSequence(2);
        bookDetail.setMaxSequence(10);
        bookDetail.setSentences(Arrays.asList(sentence1, sentence2));
        bookDetail.setLikeCount(5);
        bookDetail.setDislikeCount(1);
        bookDetail.setMyVote("LIKE");

        given(bookQueryService.getBookForViewer(bookId)).willReturn(bookDetail);

        // when & then
        mockMvc.perform(get("/api/books/{bookId}/view", bookId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.bookId").value(bookId))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.data.sentences[0].sentenceId").value(1))
                .andExpect(jsonPath("$.data.sentences[1].sentenceId").value(2))
                .andExpect(jsonPath("$.data.likeCount").value(5))
                .andExpect(jsonPath("$.data.dislikeCount").value(1));

        verify(bookQueryService).getBookForViewer(bookId);
    }

    @Test
    @DisplayName("소설 뷰어 조회 - COMPLETED 상태")
    void viewBook_CompletedStatus() throws Exception {
        // given
        Long bookId = 2L;
        BookDetailDto bookDetail = new BookDetailDto();
        bookDetail.setBookId(bookId);
        bookDetail.setTitle("Completed Book");
        bookDetail.setStatus("COMPLETED");
        bookDetail.setWriterId(1L);
        bookDetail.setWriterNicknm("writer");
        bookDetail.setSentences(Collections.emptyList());
        bookDetail.setLikeCount(10);
        bookDetail.setDislikeCount(2);

        given(bookQueryService.getBookForViewer(bookId)).willReturn(bookDetail);

        // when & then
        mockMvc.perform(get("/api/books/{bookId}/view", bookId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        verify(bookQueryService).getBookForViewer(bookId);
    }

    @Test
    @DisplayName("소설 뷰어 조회 - 문장이 null인 경우")
    void viewBook_NullSentences() throws Exception {
        // given
        Long bookId = 3L;
        BookDetailDto bookDetail = new BookDetailDto();
        bookDetail.setBookId(bookId);
        bookDetail.setTitle("Book with no sentences");
        bookDetail.setStatus("IN_PROGRESS");
        bookDetail.setWriterId(1L);
        bookDetail.setSentences(null);

        given(bookQueryService.getBookForViewer(bookId)).willReturn(bookDetail);

        // when & then
        mockMvc.perform(get("/api/books/{bookId}/view", bookId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.bookId").value(bookId));

        verify(bookQueryService).getBookForViewer(bookId);
    }

    @Test
    @DisplayName("내가 쓴 문장 조회 - 성공")
    void getMySentences_Success() throws Exception {
        // given
        Long userId = 1L;
        SentenceDto sentence = new SentenceDto();
        sentence.setSentenceId(1L);
        sentence.setSequenceNo(1);
        sentence.setContent("My sentence");
        sentence.setWriterId(userId);
        sentence.setWriterNicknm("me");
        sentence.setBookId(1L);
        sentence.setBookTitle("My Book");
        sentence.setLikeCount(3);
        sentence.setDislikeCount(0);
        sentence.setMyVote("LIKE");

        SentencePageResponse response = new SentencePageResponse();
        response.setContent(Arrays.asList(sentence));
        response.setTotalElements(1L);
        response.setTotalPages(1);
        response.setCurrentPage(0);
        response.setPageSize(10);

        given(bookQueryService.getSentencesByUser(userId, 0, 10)).willReturn(response);

        try (MockedStatic<SecurityUtil> mockedStatic = mockStatic(SecurityUtil.class)) {
            mockedStatic.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            // when & then
            mockMvc.perform(get("/api/books/mysentences")
                            .param("page", "0")
                            .param("size", "10")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content[0].sentenceId").value(1))
                    .andExpect(jsonPath("$.data.content[0].content").value("My sentence"))
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }

        verify(bookQueryService).getSentencesByUser(userId, 0, 10);
    }

    @Test
    @DisplayName("내가 쓴 문장 조회 - 인증되지 않은 사용자")
    void getMySentences_Unauthenticated() throws Exception {
        // given
        try (MockedStatic<SecurityUtil> mockedStatic = mockStatic(SecurityUtil.class)) {
            mockedStatic.when(SecurityUtil::getCurrentUserId).thenReturn(null);

            // when & then
            mockMvc.perform(get("/api/books/mysentences")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        verify(bookQueryService, never()).getSentencesByUser(anyLong(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("내가 쓴 문장 조회 - 빈 결과")
    void getMySentences_EmptyContent() throws Exception {
        // given
        Long userId = 1L;
        SentencePageResponse response = new SentencePageResponse();
        response.setContent(null);
        response.setTotalElements(0L);
        response.setTotalPages(0);
        response.setCurrentPage(0);
        response.setPageSize(10);

        given(bookQueryService.getSentencesByUser(userId, 0, 10)).willReturn(response);

        try (MockedStatic<SecurityUtil> mockedStatic = mockStatic(SecurityUtil.class)) {
            mockedStatic.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            // when & then
            mockMvc.perform(get("/api/books/mysentences")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalElements").value(0));
        }

        verify(bookQueryService).getSentencesByUser(userId, 0, 10);
    }

    @Test
    @DisplayName("내가 쓴 문장 조회 - 커스텀 페이지 파라미터")
    void getMySentences_CustomPageParams() throws Exception {
        // given
        Long userId = 1L;
        SentencePageResponse response = new SentencePageResponse();
        response.setContent(Collections.emptyList());
        response.setTotalElements(0L);
        response.setTotalPages(0);
        response.setCurrentPage(2);
        response.setPageSize(20);

        given(bookQueryService.getSentencesByUser(userId, 2, 20)).willReturn(response);

        try (MockedStatic<SecurityUtil> mockedStatic = mockStatic(SecurityUtil.class)) {
            mockedStatic.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            // when & then
            mockMvc.perform(get("/api/books/mysentences")
                            .param("page", "2")
                            .param("size", "20")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.currentPage").value(2))
                    .andExpect(jsonPath("$.data.pageSize").value(20));
        }

        verify(bookQueryService).getSentencesByUser(userId, 2, 20);
    }
}
