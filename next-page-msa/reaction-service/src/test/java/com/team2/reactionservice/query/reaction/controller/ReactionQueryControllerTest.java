package com.team2.reactionservice.query.reaction.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team2.commonmodule.util.SecurityUtil;
import com.team2.reactionservice.command.reaction.controller.TestExceptionHandler;
import com.team2.reactionservice.query.reaction.dto.response.CommentDto;
import com.team2.reactionservice.query.reaction.dto.response.CommentPageResponse;
import com.team2.reactionservice.query.reaction.service.ReactionQueryService;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/**
 * ReactionQueryController 단위 테스트
 *
 * MockMvcBuilders.standaloneSetup을 사용하여 컨트롤러 계층만 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReactionQueryController 단위 테스트")
class ReactionQueryControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ReactionQueryService reactionQueryService;

    @InjectMocks
    private ReactionQueryController reactionQueryController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(reactionQueryController)
                .setControllerAdvice(new TestExceptionHandler.TestExceptionHandlerAdvice())
                .build();
    }

    @Nested
    @DisplayName("GET /api/reactions/comments/{bookId} - 댓글 목록 조회")
    class GetCommentsTest {

        @Test
        @DisplayName("성공 - 소설의 댓글 목록을 조회하면 200 OK를 반환한다")
        void getCommentsSuccess() throws Exception {
            // Given
            CommentDto commentDto = CommentDto.builder()
                    .commentId(1L)
                    .content("테스트 댓글")
                    .writerNicknm("테스터")
                    .build();
            List<CommentDto> comments = Collections.singletonList(commentDto);

            given(reactionQueryService.getComments(anyLong())).willReturn(comments);

            // When & Then
            mockMvc.perform(get("/api/reactions/comments/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].content").value("테스트 댓글"));
        }
    }

    @Nested
    @DisplayName("GET /api/reactions/mycomments - 내가 쓴 댓글 조회")
    class GetMyCommentsTest {

        @Test
        @DisplayName("성공 - 내가 쓴 댓글 목록을 조회하면 200 OK를 반환한다")
        void getMyCommentsSuccess() throws Exception {
            // Given
            CommentDto commentDto = CommentDto.builder()
                    .commentId(1L)
                    .content("내 댓글")
                    .build();
            CommentPageResponse response = CommentPageResponse.builder()
                    .content(Collections.singletonList(commentDto))
                    .page(0)
                    .size(10)
                    .totalElements(1L)
                    .totalPages(1)
                    .hasNext(false)
                    .build();

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(1L);
                given(reactionQueryService.getCommentsByUser(anyLong(), anyInt(), anyInt())).willReturn(response);

                // When & Then
                mockMvc.perform(get("/api/reactions/mycomments")
                        .param("page", "0")
                        .param("size", "10"))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.content[0].content").value("내 댓글"));
            }
        }

        @Test
        @DisplayName("실패 - 로그인하지 않은 경우 401 Unauthorized를 반환한다")
        void getMyCommentsFail_Unauthenticated() throws Exception {
            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(null);

                // When & Then
                mockMvc.perform(get("/api/reactions/mycomments"))
                        .andDo(print())
                        .andExpect(status().isUnauthorized())
                        .andExpect(jsonPath("$.code").value("A003"));
            }
        }
    }
}
