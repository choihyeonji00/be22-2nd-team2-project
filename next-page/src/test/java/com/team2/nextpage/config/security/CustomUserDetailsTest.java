package com.team2.nextpage.config.security;

import com.team2.nextpage.command.member.entity.Member;
import com.team2.nextpage.fixtures.MemberTestBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class CustomUserDetailsTest {

    @Test
    @DisplayName("일반 사용자 권한 테스트")
    void getAuthorities_UserRole() {
        // given
        Member member = MemberTestBuilder.aMember()
                .withUserId(1L)
                .withEmail("user@test.com")
                .asUser()
                .build();
        CustomUserDetails userDetails = new CustomUserDetails(member);

        // when
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        // then
        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("관리자 권한 테스트")
    void getAuthorities_AdminRole() {
        // given
        Member member = MemberTestBuilder.aMember()
                .withUserId(1L)
                .withEmail("admin@test.com")
                .asAdmin()
                .build();
        CustomUserDetails userDetails = new CustomUserDetails(member);

        // when
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        // then
        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("비밀번호 반환 테스트")
    void getPassword() {
        // given
        Member member = MemberTestBuilder.aMember()
                .withPassword("encodedPassword")
                .build();
        CustomUserDetails userDetails = new CustomUserDetails(member);

        // when
        String password = userDetails.getPassword();

        // then
        assertThat(password).isEqualTo("encodedPassword");
    }

    @Test
    @DisplayName("사용자 이메일 반환 테스트")
    void getUsername() {
        // given
        Member member = MemberTestBuilder.aMember()
                .withEmail("test@test.com")
                .build();
        CustomUserDetails userDetails = new CustomUserDetails(member);

        // when
        String username = userDetails.getUsername();

        // then
        assertThat(username).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("계정 만료 여부 테스트")
    void isAccountNonExpired() {
        // given
        Member member = MemberTestBuilder.aMember().build();
        CustomUserDetails userDetails = new CustomUserDetails(member);

        // when
        boolean isNonExpired = userDetails.isAccountNonExpired();

        // then
        assertThat(isNonExpired).isTrue();
    }

    @Test
    @DisplayName("계정 잠금 여부 테스트")
    void isAccountNonLocked() {
        // given
        Member member = MemberTestBuilder.aMember().build();
        CustomUserDetails userDetails = new CustomUserDetails(member);

        // when
        boolean isNonLocked = userDetails.isAccountNonLocked();

        // then
        assertThat(isNonLocked).isTrue();
    }

    @Test
    @DisplayName("자격 증명 만료 여부 테스트")
    void isCredentialsNonExpired() {
        // given
        Member member = MemberTestBuilder.aMember().build();
        CustomUserDetails userDetails = new CustomUserDetails(member);

        // when
        boolean isNonExpired = userDetails.isCredentialsNonExpired();

        // then
        assertThat(isNonExpired).isTrue();
    }

    @Test
    @DisplayName("계정 활성화 여부 테스트 - 활성화")
    void isEnabled_Active() {
        // given
        Member member = MemberTestBuilder.aMember().build(); // 기본 ACTIVE
        CustomUserDetails userDetails = new CustomUserDetails(member);

        // when
        boolean isEnabled = userDetails.isEnabled();

        // then
        assertThat(isEnabled).isTrue();
    }

    @Test
    @DisplayName("계정 활성화 여부 테스트 - 비활성화(삭제됨)")
    void isEnabled_Deleted() {
        // given
        Member member = MemberTestBuilder.aMember().deleted().build();
        CustomUserDetails userDetails = new CustomUserDetails(member);

        // when
        boolean isEnabled = userDetails.isEnabled();

        // then
        assertThat(isEnabled).isFalse();
    }

    @Test
    @DisplayName("사용자 ID 반환 테스트")
    void getUserId() {
        // given
        Member member = MemberTestBuilder.aMember()
                .withUserId(123L)
                .build();
        CustomUserDetails userDetails = new CustomUserDetails(member);

        // when
        Long userId = userDetails.getUserId();

        // then
        assertThat(userId).isEqualTo(123L);
    }

    @Test
    @DisplayName("닉네임 반환 테스트")
    void getNickname() {
        // given
        Member member = MemberTestBuilder.aMember()
                .withNickname("테스터닉네임")
                .build();
        CustomUserDetails userDetails = new CustomUserDetails(member);

        // when
        String nickname = userDetails.getNickname();

        // then
        assertThat(nickname).isEqualTo("테스터닉네임");
    }

    @Test
    @DisplayName("역할 반환 테스트 - USER")
    void getRole_User() {
        // given
        Member member = MemberTestBuilder.aMember()
                .asUser()
                .build();
        CustomUserDetails userDetails = new CustomUserDetails(member);

        // when
        String role = userDetails.getRole();

        // then
        assertThat(role).isEqualTo("USER");
    }

    @Test
    @DisplayName("역할 반환 테스트 - ADMIN")
    void getRole_Admin() {
        // given
        Member member = MemberTestBuilder.aMember()
                .asAdmin()
                .build();
        CustomUserDetails userDetails = new CustomUserDetails(member);

        // when
        String role = userDetails.getRole();

        // then
        assertThat(role).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("Member 엔티티 반환 테스트")
    void getMember() {
        // given
        Member member = MemberTestBuilder.aMember()
                .withUserId(1L)
                .withEmail("test@test.com")
                .build();
        CustomUserDetails userDetails = new CustomUserDetails(member);

        // when
        Member returnedMember = userDetails.getMember();

        // then
        assertThat(returnedMember).isEqualTo(member);
        assertThat(returnedMember.getUserId()).isEqualTo(1L);
        assertThat(returnedMember.getUserEmail()).isEqualTo("test@test.com");
    }
}
