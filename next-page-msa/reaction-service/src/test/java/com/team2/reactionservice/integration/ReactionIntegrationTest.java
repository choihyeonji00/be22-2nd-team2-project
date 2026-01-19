package com.team2.reactionservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team2.reactionservice.command.reaction.dto.request.CreateCommentRequest;
import com.team2.commonmodule.feign.MemberServiceClient;
import com.team2.commonmodule.feign.StoryServiceClient;
import com.team2.commonmodule.feign.dto.MemberInfoDto;
import com.team2.commonmodule.response.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Reaction Service 통합 테스트")
class ReactionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MemberServiceClient memberServiceClient;

    @MockBean
    private StoryServiceClient storyServiceClient;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    @Test
    @DisplayName("댓글 작성 성공 테스트")
    void createCommentSuccess() throws Exception {
        // Given
        Long userId = 1L;
        Long bookId = 100L;
        String content = "Integration Test Comment";

        CreateCommentRequest request = new CreateCommentRequest(bookId, content, null);

        // Mock MemberService response
        MemberInfoDto memberInfo = MemberInfoDto.builder()
                .userId(userId)
                .userNicknm("TestUser")
                .userRole("USER")
                .build();
        given(memberServiceClient.getMemberInfo(userId)).willReturn(ApiResponse.success(memberInfo));

        // Mock StoryService (void return usually, or we just ensure no exception)
        // storyServiceClient.notify... handles exceptions in controller, so mocking not
        // strictly required if we don't verify calls,
        // but explicit mock prevents connection attempts.

        // When & Then
        mockMvc.perform(post("/api/reactions/comments")
                .header("X-User-Id", String.valueOf(userId))
                .header("X-User-Email", "test@example.com")
                .header("X-User-Role", "USER")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isNumber()); // returns commentId
    }

    @Test
    @DisplayName("댓글 작성 실패 - 내용 없음")
    void createCommentFailValidation() throws Exception {
        // Given
        Long userId = 1L;
        CreateCommentRequest request = new CreateCommentRequest(100L, "", null); // Empty content

        // When & Then
        mockMvc.perform(post("/api/reactions/comments")
                .header("X-User-Id", String.valueOf(userId))
                .header("X-User-Email", "test@example.com")
                .header("X-User-Role", "USER")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
