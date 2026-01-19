package com.team2.nextpage.common.util;

import com.team2.nextpage.command.member.entity.Member;
import com.team2.nextpage.config.security.CustomUserDetails;
import com.team2.nextpage.fixtures.MemberTestBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityUtilTest {

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setAuthenticatedUser(Member member) {
        CustomUserDetails userDetails = new CustomUserDetails(member);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("현재 사용자 ID 반환 성공")
    void getCurrentUserId_Success() {
        // given
        Member member = MemberTestBuilder.aMember()
                .withUserId(1L)
                .withEmail("test@test.com")
                .build();
        setAuthenticatedUser(member);

        // when
        Long userId = SecurityUtil.getCurrentUserId();

        // then
        assertThat(userId).isEqualTo(1L);
    }

    @Test
    @DisplayName("인증되지 않은 상태에서 사용자 ID 반환 시 null")
    void getCurrentUserId_NotAuthenticated_ReturnsNull() {
        // when
        Long userId = SecurityUtil.getCurrentUserId();

        // then
        assertThat(userId).isNull();
    }

    @Test
    @DisplayName("현재 사용자 이메일 반환 성공")
    void getCurrentUserEmail_Success() {
        // given
        Member member = MemberTestBuilder.aMember()
                .withUserId(1L)
                .withEmail("test@test.com")
                .build();
        setAuthenticatedUser(member);

        // when
        String email = SecurityUtil.getCurrentUserEmail();

        // then
        assertThat(email).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("인증되지 않은 상태에서 사용자 이메일 반환 시 null")
    void getCurrentUserEmail_NotAuthenticated_ReturnsNull() {
        // when
        String email = SecurityUtil.getCurrentUserEmail();

        // then
        assertThat(email).isNull();
    }

    @Test
    @DisplayName("현재 사용자 닉네임 반환 성공")
    void getCurrentUserNickname_Success() {
        // given
        Member member = MemberTestBuilder.aMember()
                .withUserId(1L)
                .withEmail("test@test.com")
                .withNickname("테스터")
                .build();
        setAuthenticatedUser(member);

        // when
        String nickname = SecurityUtil.getCurrentUserNickname();

        // then
        assertThat(nickname).isEqualTo("테스터");
    }

    @Test
    @DisplayName("인증되지 않은 상태에서 사용자 닉네임 반환 시 null")
    void getCurrentUserNickname_NotAuthenticated_ReturnsNull() {
        // when
        String nickname = SecurityUtil.getCurrentUserNickname();

        // then
        assertThat(nickname).isNull();
    }

    @Test
    @DisplayName("현재 사용자 역할 반환 성공 - 일반 유저")
    void getCurrentUserRole_User_Success() {
        // given
        Member member = MemberTestBuilder.aMember()
                .withUserId(1L)
                .withEmail("test@test.com")
                .asUser()
                .build();
        setAuthenticatedUser(member);

        // when
        String role = SecurityUtil.getCurrentUserRole();

        // then
        assertThat(role).isEqualTo("USER");
    }

    @Test
    @DisplayName("현재 사용자 역할 반환 성공 - 관리자")
    void getCurrentUserRole_Admin_Success() {
        // given
        Member member = MemberTestBuilder.aMember()
                .withUserId(1L)
                .withEmail("admin@test.com")
                .asAdmin()
                .build();
        setAuthenticatedUser(member);

        // when
        String role = SecurityUtil.getCurrentUserRole();

        // then
        assertThat(role).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("인증되지 않은 상태에서 사용자 역할 반환 시 null")
    void getCurrentUserRole_NotAuthenticated_ReturnsNull() {
        // when
        String role = SecurityUtil.getCurrentUserRole();

        // then
        assertThat(role).isNull();
    }

    @Test
    @DisplayName("현재 사용자 정보 전체 반환 성공")
    void getCurrentUser_Success() {
        // given
        Member member = MemberTestBuilder.aMember()
                .withUserId(1L)
                .withEmail("test@test.com")
                .withNickname("테스터")
                .build();
        setAuthenticatedUser(member);

        // when
        CustomUserDetails userDetails = SecurityUtil.getCurrentUser();

        // then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUserId()).isEqualTo(1L);
        assertThat(userDetails.getUsername()).isEqualTo("test@test.com");
        assertThat(userDetails.getNickname()).isEqualTo("테스터");
    }

    @Test
    @DisplayName("인증되지 않은 상태에서 사용자 정보 전체 반환 시 null")
    void getCurrentUser_NotAuthenticated_ReturnsNull() {
        // when
        CustomUserDetails userDetails = SecurityUtil.getCurrentUser();

        // then
        assertThat(userDetails).isNull();
    }

    @Test
    @DisplayName("인증 여부 확인 - 인증됨")
    void isAuthenticated_True() {
        // given
        Member member = MemberTestBuilder.aMember()
                .withUserId(1L)
                .withEmail("test@test.com")
                .build();
        setAuthenticatedUser(member);

        // when
        boolean isAuthenticated = SecurityUtil.isAuthenticated();

        // then
        assertThat(isAuthenticated).isTrue();
    }

    @Test
    @DisplayName("인증 여부 확인 - 인증되지 않음")
    void isAuthenticated_False() {
        // when
        boolean isAuthenticated = SecurityUtil.isAuthenticated();

        // then
        assertThat(isAuthenticated).isFalse();
    }

    @Test
    @DisplayName("관리자 여부 확인 - 관리자")
    void isAdmin_True() {
        // given
        Member member = MemberTestBuilder.aMember()
                .withUserId(1L)
                .withEmail("admin@test.com")
                .asAdmin()
                .build();
        setAuthenticatedUser(member);

        // when
        boolean isAdmin = SecurityUtil.isAdmin();

        // then
        assertThat(isAdmin).isTrue();
    }

    @Test
    @DisplayName("관리자 여부 확인 - 일반 사용자")
    void isAdmin_False_WhenUser() {
        // given
        Member member = MemberTestBuilder.aMember()
                .withUserId(1L)
                .withEmail("user@test.com")
                .asUser()
                .build();
        setAuthenticatedUser(member);

        // when
        boolean isAdmin = SecurityUtil.isAdmin();

        // then
        assertThat(isAdmin).isFalse();
    }

    @Test
    @DisplayName("관리자 여부 확인 - 인증되지 않음")
    void isAdmin_False_WhenNotAuthenticated() {
        // when
        boolean isAdmin = SecurityUtil.isAdmin();

        // then
        assertThat(isAdmin).isFalse();
    }

    @Test
    @DisplayName("인증 컨텍스트에 String principal이 있을 때 null 반환")
    void getCurrentUserId_WithStringPrincipal_ReturnsNull() {
        // given
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("anonymousUser", null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // when
        Long userId = SecurityUtil.getCurrentUserId();

        // then
        assertThat(userId).isNull();
    }
}
