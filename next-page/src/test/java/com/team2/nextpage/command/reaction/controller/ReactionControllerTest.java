package com.team2.nextpage.command.reaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team2.nextpage.command.member.entity.Member;
import com.team2.nextpage.command.member.repository.MemberRepository;
import com.team2.nextpage.command.reaction.dto.request.CreateCommentRequest;
import com.team2.nextpage.command.reaction.dto.request.VoteRequest;
import com.team2.nextpage.command.reaction.service.ReactionService;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.doNothing;
import static org.mockito.BDDMockito.doThrow;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReactionController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReactionService reactionService;

    @MockitoBean
    private SimpMessagingTemplate messagingTemplate;

    @MockitoBean
    private MemberRepository memberRepository;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("댓글 작성 성공")
    void createComment_Success() throws Exception {
        // given
        Long userId = 1L;
        CreateCommentRequest request = RequestDtoTestBuilder.commentRequest(10L, "test comment");
        Member member = mock(Member.class);
        given(member.getUserNicknm()).willReturn("nick");

        try (MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
            given(reactionService.addComment(any(CreateCommentRequest.class))).willReturn(100L);
            given(memberRepository.findById(userId)).willReturn(Optional.of(member));

            // when & then
            mockMvc.perform(post("/api/reactions/comments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value(100L));

            verify(reactionService).addComment(any(CreateCommentRequest.class));
        }
    }

    @Test
    @DisplayName("댓글 작성 실패 - 인증되지 않은 사용자")
    void createComment_Unauthenticated() throws Exception {
        // given
        CreateCommentRequest request = RequestDtoTestBuilder.commentRequest(10L, "test comment");

        try (MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentUserId).thenReturn(null);

            // when & then
            mockMvc.perform(post("/api/reactions/comments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    @DisplayName("댓글 작성 실패 - 사용자를 찾을 수 없음")
    void createComment_MemberNotFound() throws Exception {
        // given
        Long userId = 1L;
        CreateCommentRequest request = RequestDtoTestBuilder.commentRequest(10L, "test comment");

        try (MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
            given(reactionService.addComment(any(CreateCommentRequest.class))).willReturn(100L);
            given(memberRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            mockMvc.perform(post("/api/reactions/comments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    void deleteComment_Success() throws Exception {
        // given
        Long userId = 1L;
        Long commentId = 100L;

        try (MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
            doNothing().when(reactionService).deleteComment(commentId, userId);

            // when & then
            mockMvc.perform(delete("/api/reactions/comments/{commentId}", commentId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(reactionService).deleteComment(commentId, userId);
        }
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 인증되지 않은 사용자")
    void deleteComment_Unauthenticated() throws Exception {
        // given
        Long commentId = 100L;

        try (MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentUserId).thenReturn(null);

            // when & then
            mockMvc.perform(delete("/api/reactions/comments/{commentId}", commentId))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 권한 없음")
    void deleteComment_Unauthorized() throws Exception {
        // given
        Long userId = 2L;
        Long commentId = 100L;

        try (MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
            doThrow(new BusinessException(ErrorCode.UNAUTHORIZED))
                    .when(reactionService).deleteComment(commentId, userId);

            // when & then
            mockMvc.perform(delete("/api/reactions/comments/{commentId}", commentId))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    @DisplayName("소설 투표 성공 - LIKE")
    void voteBook_Like_Success() throws Exception {
        // given
        Long userId = 1L;
        VoteRequest request = new VoteRequest();
        request.setBookId(10L);
        request.setVoteType("LIKE");

        try (MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
            doNothing().when(reactionService).voteBook(anyLong(), anyLong(), anyString());

            // when & then
            mockMvc.perform(post("/api/reactions/books/vote")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(reactionService).voteBook(10L, userId, "LIKE");
        }
    }

    @Test
    @DisplayName("소설 투표 성공 - DISLIKE")
    void voteBook_Dislike_Success() throws Exception {
        // given
        Long userId = 1L;
        VoteRequest request = new VoteRequest();
        request.setBookId(10L);
        request.setVoteType("DISLIKE");

        try (MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
            doNothing().when(reactionService).voteBook(anyLong(), anyLong(), anyString());

            // when & then
            mockMvc.perform(post("/api/reactions/books/vote")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(reactionService).voteBook(10L, userId, "DISLIKE");
        }
    }

    @Test
    @DisplayName("소설 투표 실패 - 인증되지 않은 사용자")
    void voteBook_Unauthenticated() throws Exception {
        // given
        VoteRequest request = new VoteRequest();
        request.setBookId(10L);
        request.setVoteType("LIKE");

        try (MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentUserId).thenReturn(null);

            // when & then
            mockMvc.perform(post("/api/reactions/books/vote")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    @DisplayName("문장 투표 성공 - LIKE")
    void voteSentence_Like_Success() throws Exception {
        // given
        Long userId = 1L;
        Long sentenceId = 100L;
        VoteRequest request = new VoteRequest();
        request.setVoteType("LIKE");

        try (MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
            doNothing().when(reactionService).voteSentence(anyLong(), anyLong(), anyString());

            // when & then
            mockMvc.perform(post("/api/reactions/sentences/{sentenceId}/vote", sentenceId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(reactionService).voteSentence(sentenceId, userId, "LIKE");
        }
    }

    @Test
    @DisplayName("문장 투표 성공 - DISLIKE")
    void voteSentence_Dislike_Success() throws Exception {
        // given
        Long userId = 1L;
        Long sentenceId = 100L;
        VoteRequest request = new VoteRequest();
        request.setVoteType("DISLIKE");

        try (MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
            doNothing().when(reactionService).voteSentence(anyLong(), anyLong(), anyString());

            // when & then
            mockMvc.perform(post("/api/reactions/sentences/{sentenceId}/vote", sentenceId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(reactionService).voteSentence(sentenceId, userId, "DISLIKE");
        }
    }

    @Test
    @DisplayName("문장 투표 실패 - 인증되지 않은 사용자")
    void voteSentence_Unauthenticated() throws Exception {
        // given
        Long sentenceId = 100L;
        VoteRequest request = new VoteRequest();
        request.setVoteType("LIKE");

        try (MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentUserId).thenReturn(null);

            // when & then
            mockMvc.perform(post("/api/reactions/sentences/{sentenceId}/vote", sentenceId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    @DisplayName("문장 투표 실패 - 문장을 찾을 수 없음")
    void voteSentence_SentenceNotFound() throws Exception {
        // given
        Long userId = 1L;
        Long sentenceId = 999L;
        VoteRequest request = new VoteRequest();
        request.setVoteType("LIKE");

        try (MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
            doThrow(new BusinessException(ErrorCode.MEMBER_NOT_FOUND))
                    .when(reactionService).voteSentence(anyLong(), anyLong(), anyString());

            // when & then
            mockMvc.perform(post("/api/reactions/sentences/{sentenceId}/vote", sentenceId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }
}
