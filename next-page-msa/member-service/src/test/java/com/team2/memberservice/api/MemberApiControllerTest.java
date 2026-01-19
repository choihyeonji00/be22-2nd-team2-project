package com.team2.memberservice.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

import com.team2.memberservice.command.member.entity.*;
import com.team2.memberservice.command.member.repository.MemberRepository;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.assertj.core.api.Assertions;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;

@ExtendWith(MockitoExtension.class)
class MemberApiControllerTest {

    @InjectMocks
    private MemberApiController memberApiController;

    @Mock
    private MemberRepository memberRepository;

    @Test
    @DisplayName("사용자 닉네임 조회 - 정상")
    void getUserNickname_Success() {
        // Given
        Long userId = 1L;
        Member member = Member.builder()
                .userEmail("test@test.com")
                .userPw("password")
                .userNicknm("TestUser")
                .userRole(UserRole.USER)
                .userStatus(UserStatus.ACTIVE)
                .build();

        given(memberRepository.findById(userId)).willReturn(Optional.of(member));

        // When
        String nickname = memberApiController.getUserNickname(userId);

        // Then
        assertThat(nickname).isEqualTo("TestUser");
    }

    @Test
    @DisplayName("사용자 닉네임 조회 - 사용자 없음 (Unknown 반환)")
    void getUserNickname_UserNotFound_ReturnsUnknown() {
        // Given
        Long userId = 999L;
        given(memberRepository.findById(userId)).willReturn(Optional.empty());

        // When
        String nickname = memberApiController.getUserNickname(userId);

        // Then
        assertThat(nickname).isEqualTo("Unknown");
    }

    @Test
    @DisplayName("사용자 닉네임 조회 - 존재하지 않는 사용자")
    void getUserNickname_NonExistentUser() {
        // Given
        Long userId = 12345L;
        given(memberRepository.findById(anyLong())).willReturn(Optional.empty());

        // When
        String nickname = memberApiController.getUserNickname(userId);

        // Then
        assertThat(nickname).isEqualTo("Unknown");
    }
}
