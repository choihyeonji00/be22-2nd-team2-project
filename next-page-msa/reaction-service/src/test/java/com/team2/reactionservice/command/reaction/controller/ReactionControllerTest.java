package com.team2.reactionservice.command.reaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team2.commonmodule.error.BusinessException;
import com.team2.commonmodule.error.ErrorCode;
import com.team2.commonmodule.util.SecurityUtil;
import com.team2.reactionservice.command.reaction.dto.request.CreateCommentRequest;
import com.team2.reactionservice.command.reaction.dto.request.UpdateCommentRequest;
import com.team2.reactionservice.command.reaction.dto.request.VoteRequest;
import com.team2.reactionservice.command.reaction.entity.VoteType;
import com.team2.reactionservice.command.reaction.entity.VoteType;
import com.team2.reactionservice.command.reaction.service.ReactionService;
import com.team2.commonmodule.feign.MemberServiceClient;
import com.team2.commonmodule.feign.StoryServiceClient;
import com.team2.commonmodule.feign.dto.CommentNotificationDto;
import com.team2.commonmodule.feign.dto.MemberInfoDto;
import com.team2.commonmodule.response.ApiResponse;

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
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ReactionController 단위 테스트
 *
 * MockMvcBuilders.standaloneSetup을 사용하여 컨트롤러 계층만 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReactionController 단위 테스트")
class ReactionControllerTest {

        private MockMvc mockMvc;
        private ObjectMapper objectMapper;

        @Mock
        private ReactionService reactionService;

        @Mock
        private MemberServiceClient memberServiceClient;

        @Mock
        private StoryServiceClient storyServiceClient;

        @InjectMocks
        private ReactionController reactionController;

        @BeforeEach
        void setUp() {
                objectMapper = new ObjectMapper();
                mockMvc = MockMvcBuilders.standaloneSetup(reactionController)
                                .setControllerAdvice(new TestExceptionHandler.TestExceptionHandlerAdvice())
                                .build();
        }

        @Nested
        @DisplayName("POST /api/reactions/comments - 댓글 작성")
        class CreateCommentTest {

                @Test
                @DisplayName("성공 - 유효한 요청으로 댓글을 작성하면 200 OK와 commentId를 반환한다")
                void createCommentSuccess() throws Exception {
                        // Given
                        CreateCommentRequest request = CreateCommentRequest.builder()
                                        .bookId(1L)
                                        .content("테스트 댓글입니다.")
                                        .build();

                        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(1L);
                                given(reactionService.addComment(any(CreateCommentRequest.class))).willReturn(100L);

                                // MemberServiceClient mock (for nickname)
                                MemberInfoDto memberInfo = MemberInfoDto.builder().userNicknm("Tester").build();
                                given(memberServiceClient.getMemberInfo(1L))
                                                .willReturn(ApiResponse.success(memberInfo));

                                // When & Then
                                mockMvc.perform(post("/api/reactions/comments")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                                .andDo(print())
                                                .andExpect(status().isOk())
                                                .andExpect(jsonPath("$.success").value(true))
                                                .andExpect(jsonPath("$.data").value(100));
                        }
                }

                @Test
                @DisplayName("성공 - Feign Client 오류가 발생해도 댓글 작성은 성공한다 (Fallback)")
                void createCommentSuccess_FeignFail() throws Exception {
                        // Given
                        CreateCommentRequest request = CreateCommentRequest.builder()
                                        .bookId(1L)
                                        .content("테스트 댓글입니다.")
                                        .build();

                        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(1L);
                                given(reactionService.addComment(any(CreateCommentRequest.class))).willReturn(100L);

                                // MemberServiceClient fails
                                given(memberServiceClient.getMemberInfo(1L))
                                                .willThrow(new RuntimeException("Service down"));

                                // StoryServiceClient fails
                                willThrow(new RuntimeException("Service down")).given(storyServiceClient)
                                                .notifyCommentCreated(any());

                                // When & Then
                                mockMvc.perform(post("/api/reactions/comments")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                                .andDo(print())
                                                .andExpect(status().isOk())
                                                .andExpect(jsonPath("$.success").value(true));
                        }
                }

                @Test
                @DisplayName("성공 - 대댓글 작성에 성공하면 200 OK를 반환한다")
                void createReplyCommentSuccess() throws Exception {
                        // Given
                        CreateCommentRequest request = CreateCommentRequest.builder()
                                        .bookId(1L)
                                        .content("대댓글입니다.")
                                        .parentId(50L)
                                        .build();

                        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(1L);
                                given(reactionService.addComment(any(CreateCommentRequest.class))).willReturn(101L);

                                // Mocks with default behavior (null/void) to simulate typical successful flow
                                // without specific interactions needing return
                                // (actually controller checks return, so we should mock if we want coverage of
                                // inside if block)
                                given(memberServiceClient.getMemberInfo(1L)).willReturn(null); // Should handle null
                                                                                               // gracefully

                                // When & Then
                                mockMvc.perform(post("/api/reactions/comments")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                                .andDo(print())
                                                .andExpect(status().isOk())
                                                .andExpect(jsonPath("$.success").value(true))
                                                .andExpect(jsonPath("$.data").value(101));
                        }
                }

                @Test
                @DisplayName("실패 - bookId가 누락되면 400 Bad Request를 반환한다")
                void createCommentFail_MissingBookId() throws Exception {
                        // Given
                        String requestJson = "{\"content\": \"테스트 댓글입니다.\"}";

                        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(1L);

                                // When & Then
                                mockMvc.perform(post("/api/reactions/comments")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(requestJson))
                                                .andDo(print())
                                                .andExpect(status().isBadRequest());
                        }
                }

                @Test
                @DisplayName("실패 - 내용이 비어있으면 400 Bad Request를 반환한다")
                void createCommentFail_EmptyContent() throws Exception {
                        // Given
                        CreateCommentRequest request = CreateCommentRequest.builder()
                                        .bookId(1L)
                                        .content("")
                                        .build();

                        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(1L);

                                // When & Then
                                mockMvc.perform(post("/api/reactions/comments")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                                .andDo(print())
                                                .andExpect(status().isBadRequest());
                        }
                }

                @Test
                @DisplayName("실패 - 부모 댓글이 존재하지 않으면 404 Not Found를 반환한다")
                void createCommentFail_ParentNotFound() throws Exception {
                        // Given
                        CreateCommentRequest request = CreateCommentRequest.builder()
                                        .bookId(1L)
                                        .content("대댓글입니다.")
                                        .parentId(999L)
                                        .build();

                        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                                securityUtil.when(SecurityUtil::getCurrentUserId).thenReturn(1L);
                                given(reactionService.addComment(any(CreateCommentRequest.class)))
                                                .willThrow(new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

                                // When & Then
                                mockMvc.perform(post("/api/reactions/comments")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                                .andDo(print())
                                                .andExpect(status().isNotFound())
                                                .andExpect(jsonPath("$.code").value("R001"));
                        }
                }
        }

        @Nested
        @DisplayName("PATCH /api/reactions/comments/{commentId} - 댓글 수정")
        class ModifyCommentTest {

                @Test
                @DisplayName("성공 - 작성자가 댓글을 수정하면 200 OK를 반환한다")
                void modifyCommentSuccess() throws Exception {
                        // Given
                        UpdateCommentRequest request = UpdateCommentRequest.builder()
                                        .content("수정된 댓글입니다.")
                                        .build();

                        willDoNothing().given(reactionService).modifyComment(anyLong(),
                                        any(UpdateCommentRequest.class));

                        // When & Then
                        mockMvc.perform(patch("/api/reactions/comments/100")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andDo(print())
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true));
                }

                @Test
                @DisplayName("실패 - 작성자가 아니면 403 Forbidden을 반환한다")
                void modifyCommentFail_NotOwner() throws Exception {
                        // Given
                        UpdateCommentRequest request = UpdateCommentRequest.builder()
                                        .content("수정된 댓글입니다.")
                                        .build();

                        willThrow(new BusinessException(ErrorCode.NOT_COMMENT_OWNER))
                                        .given(reactionService)
                                        .modifyComment(anyLong(), any(UpdateCommentRequest.class));

                        // When & Then
                        mockMvc.perform(patch("/api/reactions/comments/100")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andDo(print())
                                        .andExpect(status().isForbidden())
                                        .andExpect(jsonPath("$.code").value("R002"));
                }

                @Test
                @DisplayName("실패 - 존재하지 않는 댓글이면 404 Not Found를 반환한다")
                void modifyCommentFail_NotFound() throws Exception {
                        // Given
                        UpdateCommentRequest request = UpdateCommentRequest.builder()
                                        .content("수정된 댓글입니다.")
                                        .build();

                        willThrow(new BusinessException(ErrorCode.COMMENT_NOT_FOUND))
                                        .given(reactionService)
                                        .modifyComment(anyLong(), any(UpdateCommentRequest.class));

                        // When & Then
                        mockMvc.perform(patch("/api/reactions/comments/999")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andDo(print())
                                        .andExpect(status().isNotFound());
                }
        }

        @Nested
        @DisplayName("DELETE /api/reactions/comments/{commentId} - 댓글 삭제")
        class RemoveCommentTest {

                @Test
                @DisplayName("성공 - 작성자가 댓글을 삭제하면 200 OK를 반환한다")
                void removeCommentSuccess() throws Exception {
                        // Given
                        willDoNothing().given(reactionService).removeComment(anyLong());

                        // When & Then
                        mockMvc.perform(delete("/api/reactions/comments/100"))
                                        .andDo(print())
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true));
                }

                @Test
                @DisplayName("실패 - 작성자가 아니면 403 Forbidden을 반환한다")
                void removeCommentFail_NotOwner() throws Exception {
                        // Given
                        willThrow(new BusinessException(ErrorCode.NOT_COMMENT_OWNER))
                                        .given(reactionService).removeComment(anyLong());

                        // When & Then
                        mockMvc.perform(delete("/api/reactions/comments/100"))
                                        .andDo(print())
                                        .andExpect(status().isForbidden());
                }
        }

        @Nested
        @DisplayName("POST /api/reactions/votes/books - 소설 투표")
        class VoteBookTest {

                @Test
                @DisplayName("성공 - 새로운 투표가 반영되면 200 OK와 true를 반환한다")
                void voteBookSuccess_NewVote() throws Exception {
                        // Given
                        VoteRequest request = VoteRequest.builder()
                                        .bookId(1L)
                                        .voteType(VoteType.LIKE)
                                        .build();

                        given(reactionService.voteBook(any(VoteRequest.class))).willReturn(true);

                        // When & Then
                        mockMvc.perform(post("/api/reactions/votes/books")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andDo(print())
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data").value(true));
                }

                @Test
                @DisplayName("성공 - 동일 투표 재클릭으로 취소되면 200 OK와 false를 반환한다")
                void voteBookSuccess_ToggleCancel() throws Exception {
                        // Given
                        VoteRequest request = VoteRequest.builder()
                                        .bookId(1L)
                                        .voteType(VoteType.LIKE)
                                        .build();

                        given(reactionService.voteBook(any(VoteRequest.class))).willReturn(false);

                        // When & Then
                        mockMvc.perform(post("/api/reactions/votes/books")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andDo(print())
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data").value(false));
                }

                @Test
                @DisplayName("실패 - voteType이 누락되면 400 Bad Request를 반환한다")
                void voteBookFail_MissingVoteType() throws Exception {
                        // Given
                        String requestJson = "{\"bookId\": 1}";

                        // When & Then
                        mockMvc.perform(post("/api/reactions/votes/books")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestJson))
                                        .andDo(print())
                                        .andExpect(status().isBadRequest());
                }
        }

        @Nested
        @DisplayName("POST /api/reactions/votes/sentences/{sentenceId} - 문장 투표")
        class VoteSentenceTest {

                @Test
                @DisplayName("성공 - 새로운 투표가 반영되면 200 OK와 true를 반환한다")
                void voteSentenceSuccess_NewVote() throws Exception {
                        // Given
                        VoteRequest request = VoteRequest.builder()
                                        .voteType(VoteType.LIKE)
                                        .build();

                        given(reactionService.voteSentence(anyLong(), any(VoteRequest.class))).willReturn(true);

                        // When & Then
                        mockMvc.perform(post("/api/reactions/votes/sentences/100")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andDo(print())
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data").value(true));
                }

                @Test
                @DisplayName("성공 - 동일 투표 재클릭으로 취소되면 200 OK와 false를 반환한다")
                void voteSentenceSuccess_ToggleCancel() throws Exception {
                        // Given
                        VoteRequest request = VoteRequest.builder()
                                        .voteType(VoteType.DISLIKE)
                                        .build();

                        given(reactionService.voteSentence(anyLong(), any(VoteRequest.class))).willReturn(false);

                        // When & Then
                        mockMvc.perform(post("/api/reactions/votes/sentences/100")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andDo(print())
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data").value(false));
                }

                @Test
                @DisplayName("성공 - 투표 타입 변경이 반영되면 200 OK와 true를 반환한다")
                void voteSentenceSuccess_ChangeVoteType() throws Exception {
                        // Given
                        VoteRequest request = VoteRequest.builder()
                                        .voteType(VoteType.DISLIKE) // LIKE에서 DISLIKE로 변경
                                        .build();

                        given(reactionService.voteSentence(anyLong(), any(VoteRequest.class))).willReturn(true);

                        // When & Then
                        mockMvc.perform(post("/api/reactions/votes/sentences/100")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andDo(print())
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data").value(true));
                }
        }
}
