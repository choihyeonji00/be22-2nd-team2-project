package com.team2.memberservice.query.member.service;

import com.team2.commonmodule.error.BusinessException;
import com.team2.commonmodule.error.ErrorCode;
import com.team2.commonmodule.feign.ReactionServiceClient;
import com.team2.commonmodule.feign.StoryServiceClient;
import com.team2.commonmodule.feign.dto.MemberReactionStatsDto;
import com.team2.commonmodule.feign.dto.MemberStoryStatsDto;
import com.team2.commonmodule.response.ApiResponse;
import com.team2.memberservice.query.member.dto.response.MemberDto;
import com.team2.memberservice.query.member.mapper.MemberMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MemberQueryServiceTest {

    @InjectMocks
    private MemberQueryService memberQueryService;

    @Mock
    private MemberMapper memberMapper;

    @Mock
    private StoryServiceClient storyServiceClient;

    @Mock
    private ReactionServiceClient reactionServiceClient;

    // ===== getMyPage(String) 테스트 =====

    @Test
    @DisplayName("마이페이지 조회 - 정상 조회 (모든 통계 성공)")
    void getMyPage_Success_AllStatsSucceed() {
        // Given
        String userEmail = "test@test.com";

        MemberDto memberDto = MemberDto.builder()
                .userId(1L)
                .userEmail(userEmail)
                .userNicknm("TestUser")
                .userRole("USER")
                .build();

        given(memberMapper.findByUserEmail(userEmail)).willReturn(Optional.of(memberDto));

        // Feign Mock - Story Service 성공
        MemberStoryStatsDto storyStats = MemberStoryStatsDto.builder()
                .createdBookCount(5)
                .writtenSentenceCount(20)
                .build();
        ApiResponse<MemberStoryStatsDto> storyResponse = ApiResponse.success(storyStats);
        given(storyServiceClient.getMemberStoryStats(1L)).willReturn(storyResponse);

        // Feign Mock - Reaction Service 성공
        MemberReactionStatsDto reactionStats = MemberReactionStatsDto.builder()
                .writtenCommentCount(10)
                .build();
        ApiResponse<MemberReactionStatsDto> reactionResponse = ApiResponse.success(reactionStats);
        given(reactionServiceClient.getMemberReactionStats(1L)).willReturn(reactionResponse);

        // When
        MemberDto result = memberQueryService.getMyPage(userEmail);

        // Then
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getUserEmail()).isEqualTo(userEmail);
        assertThat(result.getUserNicknm()).isEqualTo("TestUser");
        assertThat(result.getCreatedBookCount()).isEqualTo(5);
        assertThat(result.getWrittenSentenceCount()).isEqualTo(20);
        assertThat(result.getWrittenCommentCount()).isEqualTo(10);
    }

    @Test
    @DisplayName("마이페이지 조회 - 회원 찾을 수 없음 → BusinessException")
    void getMyPage_MemberNotFound_ThrowsException() {
        // Given
        String userEmail = "notfound@test.com";
        given(memberMapper.findByUserEmail(userEmail)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> memberQueryService.getMyPage(userEmail))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("마이페이지 조회 - Story Service 호출 실패 (통계 0 유지)")
    void getMyPage_StoryServiceFailure_KeepsDefaultZero() {
        // Given
        String userEmail = "test@test.com";

        MemberDto memberDto = MemberDto.builder()
                .userId(1L)
                .userEmail(userEmail)
                .build();

        given(memberMapper.findByUserEmail(userEmail)).willReturn(Optional.of(memberDto));

        // Feign Mock - Story Service 실패
        given(storyServiceClient.getMemberStoryStats(1L))
                .willThrow(new RuntimeException("Service unavailable"));

        // Feign Mock - Reaction Service 성공
        MemberReactionStatsDto reactionStats = MemberReactionStatsDto.builder()
                .writtenCommentCount(5)
                .build();
        ApiResponse<MemberReactionStatsDto> reactionResponse = ApiResponse.success(reactionStats);
        given(reactionServiceClient.getMemberReactionStats(1L)).willReturn(reactionResponse);

        // When
        MemberDto result = memberQueryService.getMyPage(userEmail);

        // Then
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getCreatedBookCount()).isNull(); // 실패 시 null 유지
        assertThat(result.getWrittenSentenceCount()).isNull(); // 실패 시 null 유지
        assertThat(result.getWrittenCommentCount()).isEqualTo(5); // Reaction은 성공
    }

    @Test
    @DisplayName("마이페이지 조회 - Reaction Service 호출 실패 (통계 0 유지)")
    void getMyPage_ReactionServiceFailure_KeepsDefaultZero() {
        // Given
        String userEmail = "test@test.com";

        MemberDto memberDto = MemberDto.builder()
                .userId(1L)
                .userEmail(userEmail)
                .build();

        given(memberMapper.findByUserEmail(userEmail)).willReturn(Optional.of(memberDto));

        // Feign Mock - Story Service 성공
        MemberStoryStatsDto storyStats = MemberStoryStatsDto.builder()
                .createdBookCount(3)
                .writtenSentenceCount(15)
                .build();
        ApiResponse<MemberStoryStatsDto> storyResponse = ApiResponse.success(storyStats);
        given(storyServiceClient.getMemberStoryStats(1L)).willReturn(storyResponse);

        // Feign Mock - Reaction Service 실패
        given(reactionServiceClient.getMemberReactionStats(1L))
                .willThrow(new RuntimeException("Service unavailable"));

        // When
        MemberDto result = memberQueryService.getMyPage(userEmail);

        // Then
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getCreatedBookCount()).isEqualTo(3); // Story는 성공
        assertThat(result.getWrittenSentenceCount()).isEqualTo(15); // Story는 성공
        assertThat(result.getWrittenCommentCount()).isNull(); // 실패 시 null 유지
    }

    @Test
    @DisplayName("마이페이지 조회 - 모든 Feign 호출 실패 (기본값 유지)")
    void getMyPage_AllFeignCallsFailure_KeepsDefaultValues() {
        // Given
        String userEmail = "test@test.com";

        MemberDto memberDto = MemberDto.builder()
                .userId(1L)
                .userEmail(userEmail)
                .userNicknm("TestUser")
                .build();

        given(memberMapper.findByUserEmail(userEmail)).willReturn(Optional.of(memberDto));

        // Feign Mock - Story Service 실패
        given(storyServiceClient.getMemberStoryStats(1L))
                .willThrow(new RuntimeException("Service unavailable"));

        // Feign Mock - Reaction Service 실패
        given(reactionServiceClient.getMemberReactionStats(1L))
                .willThrow(new RuntimeException("Service unavailable"));

        // When
        MemberDto result = memberQueryService.getMyPage(userEmail);

        // Then
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getUserNicknm()).isEqualTo("TestUser");
        assertThat(result.getCreatedBookCount()).isNull();
        assertThat(result.getWrittenSentenceCount()).isNull();
        assertThat(result.getWrittenCommentCount()).isNull();
    }

}
