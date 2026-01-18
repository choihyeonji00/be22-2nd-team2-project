package com.team2.storyservice.command.book.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team2.commonmodule.error.BusinessException;
import com.team2.commonmodule.error.ErrorCode;
import com.team2.commonmodule.util.SecurityUtil;
import com.team2.storyservice.command.book.dto.request.CreateBookRequest;
import com.team2.storyservice.command.book.dto.request.SentenceAppendRequest;
import com.team2.storyservice.command.book.dto.request.UpdateBookRequest;
import com.team2.storyservice.command.book.dto.request.UpdateSentenceRequest;
import com.team2.storyservice.command.book.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * BookController 단위 테스트
 *
 * MockMvcBuilders.standaloneSetup을 사용하여 컨트롤러 계층만 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookController 단위 테스트")
class BookControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookController bookController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(bookController)
                .setControllerAdvice(new TestExceptionHandler.TestExceptionHandlerAdvice())
                .build();
    }

    @Nested
    @DisplayName("POST /api/books - 소설 생성")
    class CreateBookTest {

        @Test
        @DisplayName("성공 - 유효한 요청으로 소설을 생성하면 200 OK와 bookId를 반환한다")
        void createBookSuccess() throws Exception {
            // Given
            CreateBookRequest request = CreateBookRequest.builder()
                    .title("테스트 소설")
                    .categoryId("1")
                    .maxSequence(50)
                    .firstSentence("옛날 옛적에...")
                    .build();

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(1L);
                given(bookService.createBook(anyLong(), any(CreateBookRequest.class))).willReturn(100L);

                // When & Then
                mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data").value(100));
            }
        }

        @Test
        @DisplayName("실패 - 제목이 누락되면 400 Bad Request를 반환한다")
        void createBookFail_MissingTitle() throws Exception {
            // Given
            String requestJson = "{\"categoryId\": \"1\", \"maxSequence\": 50, \"firstSentence\": \"옛날 옛적에...\"}";

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(1L);

                // When & Then
                mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                        .andDo(print())
                        .andExpect(status().isBadRequest());
            }
        }

        @Test
        @DisplayName("실패 - maxSequence가 범위를 벗어나면 400 Bad Request를 반환한다")
        void createBookFail_InvalidMaxSequence() throws Exception {
            // Given
            CreateBookRequest request = CreateBookRequest.builder()
                    .title("테스트 소설")
                    .categoryId("1")
                    .maxSequence(5) // min 10 미만
                    .firstSentence("옛날 옛적에...")
                    .build();

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(1L);

                // When & Then
                mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andDo(print())
                        .andExpect(status().isBadRequest());
            }
        }
    }

    @Nested
    @DisplayName("POST /api/books/{bookId}/sentences - 문장 이어쓰기")
    class AppendSentenceTest {

        @Test
        @DisplayName("성공 - 유효한 요청으로 문장을 추가하면 200 OK와 sentenceId를 반환한다")
        void appendSentenceSuccess() throws Exception {
            // Given
            SentenceAppendRequest request = SentenceAppendRequest.builder()
                    .content("새로운 문장입니다.")
                    .build();

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(1L);
                given(bookService.appendSentence(anyLong(), anyLong(), any(SentenceAppendRequest.class)))
                        .willReturn(200L);

                // When & Then
                mockMvc.perform(post("/api/books/100/sentences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data").value(200));
            }
        }

        @Test
        @DisplayName("실패 - 연속 작성이면 400 Bad Request를 반환한다")
        void appendSentenceFail_ConsecutiveWriting() throws Exception {
            // Given
            SentenceAppendRequest request = SentenceAppendRequest.builder()
                    .content("새로운 문장입니다.")
                    .build();

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(1L);
                given(bookService.appendSentence(anyLong(), anyLong(), any(SentenceAppendRequest.class)))
                        .willThrow(new BusinessException(ErrorCode.CONSECUTIVE_WRITING_NOT_ALLOWED));

                // When & Then
                mockMvc.perform(post("/api/books/100/sentences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andDo(print())
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.code").value("B003"));
            }
        }

        @Test
        @DisplayName("실패 - 이미 완결된 소설이면 400 Bad Request를 반환한다")
        void appendSentenceFail_AlreadyCompleted() throws Exception {
            // Given
            SentenceAppendRequest request = SentenceAppendRequest.builder()
                    .content("새로운 문장입니다.")
                    .build();

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(1L);
                given(bookService.appendSentence(anyLong(), anyLong(), any(SentenceAppendRequest.class)))
                        .willThrow(new BusinessException(ErrorCode.ALREADY_COMPLETED));

                // When & Then
                mockMvc.perform(post("/api/books/100/sentences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andDo(print())
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.code").value("B002"));
            }
        }
    }

    @Nested
    @DisplayName("POST /api/books/{bookId}/complete - 소설 완결")
    class CompleteBookTest {

        @Test
        @DisplayName("성공 - 작성자가 소설을 완결하면 200 OK를 반환한다")
        void completeBookSuccess() throws Exception {
            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(1L);
                willDoNothing().given(bookService).completeBook(anyLong(), anyLong());

                // When & Then
                mockMvc.perform(post("/api/books/100/complete"))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true));
            }
        }

        @Test
        @DisplayName("실패 - 작성자가 아니면 403 Forbidden을 반환한다")
        void completeBookFail_NotOwner() throws Exception {
            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(2L);
                willThrow(new BusinessException(ErrorCode.NOT_BOOK_OWNER))
                        .given(bookService).completeBook(anyLong(), anyLong());

                // When & Then
                mockMvc.perform(post("/api/books/100/complete"))
                        .andDo(print())
                        .andExpect(status().isForbidden())
                        .andExpect(jsonPath("$.code").value("B005"));
            }
        }
    }

    @Nested
    @DisplayName("PATCH /api/books/{bookId} - 소설 제목 수정")
    class UpdateBookTitleTest {

        @Test
        @DisplayName("성공 - 작성자가 제목을 수정하면 200 OK를 반환한다")
        void updateBookTitleSuccess() throws Exception {
            // Given
            UpdateBookRequest request = UpdateBookRequest.builder()
                    .title("수정된 제목")
                    .build();

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(1L);
                willDoNothing().given(bookService).updateBookTitle(anyLong(), anyLong(), anyString());

                // When & Then
                mockMvc.perform(patch("/api/books/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true));
            }
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 소설이면 404 Not Found를 반환한다")
        void updateBookTitleFail_NotFound() throws Exception {
            // Given
            UpdateBookRequest request = UpdateBookRequest.builder()
                    .title("수정된 제목")
                    .build();

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(1L);
                willThrow(new BusinessException(ErrorCode.BOOK_NOT_FOUND))
                        .given(bookService).updateBookTitle(anyLong(), anyLong(), anyString());

                // When & Then
                mockMvc.perform(patch("/api/books/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andDo(print())
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.code").value("B006"));
            }
        }
    }

    @Nested
    @DisplayName("PATCH /api/books/{bookId}/sentences/{sentenceId} - 문장 수정")
    class UpdateSentenceTest {

        @Test
        @DisplayName("성공 - 작성자가 문장을 수정하면 200 OK를 반환한다")
        void updateSentenceSuccess() throws Exception {
            // Given
            UpdateSentenceRequest request = UpdateSentenceRequest.builder()
                    .content("수정된 문장입니다.")
                    .build();

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(1L);
                willDoNothing().given(bookService).updateSentence(anyLong(), anyLong(), anyLong(), anyString());

                // When & Then
                mockMvc.perform(patch("/api/books/100/sentences/200")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true));
            }
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 문장이면 404 Not Found를 반환한다")
        void updateSentenceFail_NotFound() throws Exception {
            // Given
            UpdateSentenceRequest request = UpdateSentenceRequest.builder()
                    .content("수정된 문장입니다.")
                    .build();

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(1L);
                willThrow(new BusinessException(ErrorCode.SENTENCE_NOT_FOUND))
                        .given(bookService).updateSentence(anyLong(), anyLong(), anyLong(), anyString());

                // When & Then
                mockMvc.perform(patch("/api/books/100/sentences/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andDo(print())
                        .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.code").value("B007"));
            }
        }
    }

    @Nested
    @DisplayName("DELETE /api/books/{bookId} - 소설 삭제")
    class DeleteBookTest {

        @Test
        @DisplayName("성공 - 작성자가 소설을 삭제하면 200 OK를 반환한다")
        void deleteBookSuccess() throws Exception {
            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(1L);
                willDoNothing().given(bookService).deleteBook(anyLong(), anyLong());

                // When & Then
                mockMvc.perform(delete("/api/books/100"))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true));
            }
        }

        @Test
        @DisplayName("실패 - 권한이 없으면 403 Forbidden을 반환한다")
        void deleteBookFail_AccessDenied() throws Exception {
            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(2L);
                willThrow(new BusinessException(ErrorCode.ACCESS_DENIED))
                        .given(bookService).deleteBook(anyLong(), anyLong());

                // When & Then
                mockMvc.perform(delete("/api/books/100"))
                        .andDo(print())
                        .andExpect(status().isForbidden());
            }
        }
    }

    @Nested
    @DisplayName("DELETE /api/books/{bookId}/sentences/{sentenceId} - 문장 삭제")
    class DeleteSentenceTest {

        @Test
        @DisplayName("성공 - 작성자가 문장을 삭제하면 200 OK를 반환한다")
        void deleteSentenceSuccess() throws Exception {
            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(1L);
                willDoNothing().given(bookService).deleteSentence(anyLong(), anyLong(), anyLong());

                // When & Then
                mockMvc.perform(delete("/api/books/100/sentences/200"))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true));
            }
        }

        @Test
        @DisplayName("실패 - 권한이 없으면 403 Forbidden을 반환한다")
        void deleteSentenceFail_AccessDenied() throws Exception {
            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(2L);
                willThrow(new BusinessException(ErrorCode.ACCESS_DENIED))
                        .given(bookService).deleteSentence(anyLong(), anyLong(), anyLong());

                // When & Then
                mockMvc.perform(delete("/api/books/100/sentences/200"))
                        .andDo(print())
                        .andExpect(status().isForbidden());
            }
        }
    }
}
