package com.team2.storyservice.query.book.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team2.commonmodule.util.SecurityUtil;
import com.team2.storyservice.command.book.controller.TestExceptionHandler;
import com.team2.storyservice.query.book.dto.request.BookSearchRequest;
import com.team2.storyservice.query.book.dto.response.BookDetailDto;
import com.team2.storyservice.query.book.dto.response.BookDto;
import com.team2.storyservice.query.book.dto.response.BookPageResponse;
import com.team2.storyservice.query.book.dto.response.SentenceDto;
import com.team2.storyservice.query.book.dto.response.SentencePageResponse;
import com.team2.storyservice.query.book.service.BookQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookQueryController 단위 테스트")
class BookQueryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BookQueryService bookQueryService;

    @InjectMocks
    private BookQueryController bookQueryController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bookQueryController)
                .setControllerAdvice(new TestExceptionHandler.TestExceptionHandlerAdvice())
                .build();
    }

    @Nested
    @DisplayName("GET /api/books - 소설 목록 조회")
    class SearchBooksTest {

        @Test
        @DisplayName("성공 - 검색 조건에 맞는 소설 목록을 반환한다")
        void searchBooksSuccess() throws Exception {
            // Given
            BookDto bookDto = new BookDto();
            bookDto.setBookId(1L);
            bookDto.setTitle("테스트 소설");
            bookDto.setWriterNicknm("작가님");

            BookPageResponse response = new BookPageResponse(List.of(bookDto), 0, 10, 1L);

            given(bookQueryService.searchBooks(any(BookSearchRequest.class))).willReturn(response);

            // When & Then
            mockMvc.perform(get("/api/books")
                    .param("page", "0")
                    .param("size", "10")
                    .param("keyword", "테스트"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content[0].title").value("테스트 소설"));
        }
    }

    @Nested
    @DisplayName("GET /api/books/{bookId} - 소설 상세 조회")
    class GetBookTest {

        @Test
        @DisplayName("성공 - 존재하는 소설 ID로 조회하면 200 OK와 소설 정보를 반환한다")
        void getBookSuccess() throws Exception {
            // Given
            BookDto bookDto = new BookDto();
            bookDto.setBookId(1L);
            bookDto.setTitle("테스트 소설");

            given(bookQueryService.getBook(anyLong())).willReturn(bookDto);

            // When & Then
            mockMvc.perform(get("/api/books/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.bookId").value(1));
        }
    }

    @Nested
    @DisplayName("GET /api/books/{bookId}/view - 소설 뷰어 조회")
    class GetBookForViewerTest {

        @Test
        @DisplayName("성공 - IN_PROGRESS 상태인 소설을 조회하면 append-sentence 링크가 포함된다")
        void getBookForViewerSuccess_InProgress() throws Exception {
            // Given
            BookDetailDto detailDto = new BookDetailDto();
            detailDto.setBookId(1L);
            detailDto.setTitle("테스트 소설");
            detailDto.setStatus("IN_PROGRESS");
            detailDto.setSentences(Collections.emptyList());

            given(bookQueryService.getBookForViewer(anyLong())).willReturn(detailDto);

            // When & Then
            mockMvc.perform(get("/api/books/1/view"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));
        }

        @Test
        @DisplayName("성공 - COMPLETED 상태인 소설을 조회하면 append-sentence 링크가 없다")
        void getBookForViewerSuccess_Completed() throws Exception {
            // Given
            BookDetailDto detailDto = new BookDetailDto();
            detailDto.setBookId(1L);
            detailDto.setTitle("테스트 소설");
            detailDto.setStatus("COMPLETED");
            detailDto.setSentences(Collections.emptyList());

            given(bookQueryService.getBookForViewer(anyLong())).willReturn(detailDto);

            // When & Then
            mockMvc.perform(get("/api/books/1/view"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.status").value("COMPLETED"));
        }
    }

    @Nested
    @DisplayName("GET /api/books/mysentences - 내가 쓴 문장 조회")
    class GetMySentencesTest {

        @Test
        @DisplayName("성공 - 로그인한 사용자의 문장 목록을 반환한다")
        void getMySentencesSuccess() throws Exception {
            // Given
            SentenceDto sentenceDto = new SentenceDto();
            sentenceDto.setSentenceId(1L);
            sentenceDto.setContent("내가 쓴 문장");

            SentencePageResponse response = new SentencePageResponse();
            response.setContent(List.of(sentenceDto));
            response.setTotalPages(1);
            response.setTotalElements(1L);

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(1L);
                given(bookQueryService.getSentencesByUser(anyLong(), anyInt(), anyInt())).willReturn(response);

                // When & Then
                mockMvc.perform(get("/api/books/mysentences")
                        .param("page", "0")
                        .param("size", "10"))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.content[0].content").value("내가 쓴 문장"));
            }
        }

        @Test
        @DisplayName("실패 - 로그인하지 않은 경우 예외(또는 401/500)가 발생한다")
        void getMySentencesFail_Unauthenticated() throws Exception {
            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(null);

                // When & Then
                mockMvc.perform(get("/api/books/mysentences"))
                        .andDo(print())
                        .andExpect(status().isUnauthorized())
                        .andExpect(jsonPath("$.code").value("A003"));
            }
        }
    }
}
