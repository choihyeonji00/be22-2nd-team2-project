package com.team2.nextpage.auth.service;

import com.team2.nextpage.command.member.entity.Member;
import com.team2.nextpage.command.member.repository.MemberRepository;
import com.team2.nextpage.config.security.CustomUserDetails;
import com.team2.nextpage.fixtures.MemberTestBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private MemberRepository memberRepository;

    @Test
    @DisplayName("이메일로 사용자 정보 조회 성공")
    void loadUserByUsername_Success() {
        // given
        String email = "test@test.com";
        Member member = MemberTestBuilder.aMember()
                .withEmail(email)
                .withUserId(1L)
                .withNickname("테스터")
                .build();

        given(memberRepository.findByUserEmail(email)).willReturn(Optional.of(member));

        // when
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(email);
        assertThat(userDetails).isInstanceOf(CustomUserDetails.class);

        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        assertThat(customUserDetails.getUserId()).isEqualTo(1L);
        assertThat(customUserDetails.getNickname()).isEqualTo("테스터");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 조회 시 예외 발생")
    void loadUserByUsername_NotFound_ThrowsException() {
        // given
        String email = "notfound@test.com";
        given(memberRepository.findByUserEmail(email)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(email))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");
    }

    @Test
    @DisplayName("ID로 사용자 정보 조회 성공")
    void loadUserById_Success() {
        // given
        Long userId = 1L;
        Member member = MemberTestBuilder.aMember()
                .withUserId(userId)
                .withEmail("test@test.com")
                .withNickname("테스터")
                .build();

        given(memberRepository.findById(userId)).willReturn(Optional.of(member));

        // when
        UserDetails userDetails = customUserDetailsService.loadUserById(userId);

        // then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails).isInstanceOf(CustomUserDetails.class);

        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        assertThat(customUserDetails.getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회 시 예외 발생")
    void loadUserById_NotFound_ThrowsException() {
        // given
        Long userId = 999L;
        given(memberRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> customUserDetailsService.loadUserById(userId))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("관리자 사용자 정보 조회 성공")
    void loadUserByUsername_Admin_Success() {
        // given
        String email = "admin@test.com";
        Member admin = MemberTestBuilder.aMember()
                .withEmail(email)
                .withUserId(2L)
                .withNickname("관리자")
                .asAdmin()
                .build();

        given(memberRepository.findByUserEmail(email)).willReturn(Optional.of(admin));

        // when
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // then
        assertThat(userDetails).isNotNull();
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        assertThat(customUserDetails.getRole()).isEqualTo("ADMIN");
    }
}
