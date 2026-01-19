package com.team2.nextpage.command.book.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team2.nextpage.command.book.dto.request.CreateBookRequest;
import com.team2.nextpage.command.book.dto.request.SentenceAppendRequest;
import com.team2.nextpage.command.book.service.BookService;
import com.team2.nextpage.common.error.BusinessException;
import com.team2.nextpage.common.error.ErrorCode;
import com.team2.nextpage.common.util.SecurityUtil;
import com.team2.nextpage.fixtures.RequestDtoTestBuilder;
import com.team2.nextpage.jwt.JwtAuthenticationFilter;
import com.team2.nextpage.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.doNothing;
import static org.mockito.BDDMockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("소설 생성 성공")
    void createBook_Success() throws Exception {
        // given
        Long userId = 1L;
        Long createdBookId = 10L;
        CreateBookRequest request = RequestDtoTestBuilder.createBookRequest(
            "Title",
            "FANTASY",
            "Start",
            10
        );

        try (MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
            given(bookService.createBook(eq(userId), any(CreateBookRequest.class)))
                .willReturn(createdBookId);

            // when & then
            mockMvc.perform(post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value(createdBookId));

            verify(bookService).createBook(eq(userId), any(CreateBookRequest.class));
        }
    }

    @Test
    @DisplayName("소설 생성 실패 - 인증되지 않은 사용자")
    void createBook_Unauthenticated() throws Exception {
        // given
        CreateBookRequest request = RequestDtoTestBuilder.createBookRequest(
            "Title",
            "FANTASY",
            "Start",
            10
        );

        try (MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentUserId).thenReturn(null);

            // when & then
            mockMvc.perform(post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    @DisplayName("문장 추가 성공")
    void appendSentence_Success() throws Exception {
        // given
        Long userId = 1L;
        Long bookId = 10L;
        Long sentenceId = 100L;
        SentenceAppendRequest request = RequestDtoTestBuilder.sentenceRequest("Next sentence");

        try (MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
            given(bookService.appendSentence(eq(bookId), eq(userId), any(SentenceAppendRequest.class)))
                    .willReturn(sentenceId);

            // when & then
            mockMvc.perform(post("/api/books/{bookId}/sentences", bookId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value(sentenceId));

            verify(bookService).appendSentence(eq(bookId), eq(userId), any(SentenceAppendRequest.class));
        }
    }

    @Test
    @DisplayName("문장 추가 실패 - 인증되지 않은 사용자")
    void appendSentence_Unauthenticated() throws Exception {
        // given
        Long bookId = 10L;
        SentenceAppendRequest request = RequestDtoTestBuilder.sentenceRequest("Next sentence");

        try (MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentUserId).thenReturn(null);

            // when & then
            mockMvc.perform(post("/api/books/{bookId}/sentences", bookId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    @DisplayName("문장 추가 실패 - 소설을 찾을 수 없음")
    void appendSentence_BookNotFound() throws Exception {
        // given
        Long userId = 1L;
        Long bookId = 999L;
        SentenceAppendRequest request = RequestDtoTestBuilder.sentenceRequest("Next sentence");

        try (MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
            given(bookService.appendSentence(eq(bookId), eq(userId), any(SentenceAppendRequest.class)))
                    .willThrow(new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

            // when & then
            mockMvc.perform(post("/api/books/{bookId}/sentences", bookId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    @DisplayName("소설 완료 처리 성공")
    void completeBook_Success() throws Exception {
        // given
        Long userId = 1L;
        Long bookId = 10L;

        try (MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
            doNothing().when(bookService).completeBook(bookId, userId);

            // when & then
            mockMvc.perform(patch("/api/books/{bookId}/complete", bookId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(bookService).completeBook(bookId, userId);
        }
    }

    @Test
    @DisplayName("소설 완료 처리 실패 - 인증되지 않은 사용자")
    void completeBook_Unauthenticated() throws Exception {
        // given
        Long bookId = 10L;

        try (MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentUserId).thenReturn(null);

            // when & then
            mockMvc.perform(patch("/api/books/{bookId}/complete", bookId))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    @DisplayName("소설 완료 처리 실패 - 이미 완료된 소설")
    void completeBook_AlreadyCompleted() throws Exception {
        // given
        Long userId = 1L;
        Long bookId = 10L;

        try (MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
            doThrow(new BusinessException(ErrorCode.ALREADY_COMPLETED))
                    .when(bookService).completeBook(bookId, userId);

            // when & then
            mockMvc.perform(patch("/api/books/{bookId}/complete", bookId))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    @DisplayName("소설 완료 처리 실패 - 권한 없음")
    void completeBook_Unauthorized() throws Exception {
        // given
        Long userId = 2L;
        Long bookId = 10L;

        try (MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
            doThrow(new BusinessException(ErrorCode.UNAUTHORIZED))
                    .when(bookService).completeBook(bookId, userId);

            // when & then
            mockMvc.perform(patch("/api/books/{bookId}/complete", bookId))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    @DisplayName("소설 삭제 성공")
    void deleteBook_Success() throws Exception {
        // given
        Long userId = 1L;
        Long bookId = 10L;

        try (MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
            doNothing().when(bookService).deleteBook(bookId, userId);

            // when & then
            mockMvc.perform(delete("/api/books/{bookId}", bookId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(bookService).deleteBook(bookId, userId);
        }
    }

    @Test
    @DisplayName("소설 삭제 실패 - 인증되지 않은 사용자")
    void deleteBook_Unauthenticated() throws Exception {
        // given
        Long bookId = 10L;

        try (MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentUserId).thenReturn(null);

            // when & then
            mockMvc.perform(delete("/api/books/{bookId}", bookId))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    @DisplayName("소설 삭제 실패 - 권한 없음")
    void deleteBook_Unauthorized() throws Exception {
        // given
        Long userId = 2L;
        Long bookId = 10L;

        try (MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
            doThrow(new BusinessException(ErrorCode.UNAUTHORIZED))
                    .when(bookService).deleteBook(bookId, userId);

            // when & then
            mockMvc.perform(delete("/api/books/{bookId}", bookId))
                    .andExpect(status().isForbidden());
        }
    }
}
