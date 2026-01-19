package com.team2.nextpage.jwt;

import com.team2.nextpage.command.member.entity.Member;
import com.team2.nextpage.config.security.CustomUserDetails;
import com.team2.nextpage.fixtures.MemberTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserDetailsService userDetailsService;

    private static final String SECRET_KEY = "testsecretkey12345678901234567890123456789012345678901234567890";
    private static final long ACCESS_TOKEN_VALIDITY = 3600L;
    private static final long REFRESH_TOKEN_VALIDITY = 604800L;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(userDetailsService);
        ReflectionTestUtils.setField(jwtTokenProvider, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenValidityInSeconds", ACCESS_TOKEN_VALIDITY);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenValidityInSeconds", REFRESH_TOKEN_VALIDITY);
        jwtTokenProvider.init();
    }

    @Test
    @DisplayName("Access Token 생성 성공")
    void createAccessToken_Success() {
        // given
        Member member = MemberTestBuilder.aMember()
                .withEmail("test@test.com")
                .withUserId(1L)
                .build();
        CustomUserDetails userDetails = new CustomUserDetails(member);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        // when
        String token = jwtTokenProvider.createAccessToken(authentication);

        // then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("Refresh Token 생성 성공")
    void createRefreshToken_Success() {
        // given
        Member member = MemberTestBuilder.aMember()
                .withEmail("test@test.com")
                .withUserId(1L)
                .build();
        CustomUserDetails userDetails = new CustomUserDetails(member);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        // when
        String token = jwtTokenProvider.createRefreshToken(authentication);

        // then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("토큰에서 이메일 추출 성공")
    void getUserEmailFromToken_Success() {
        // given
        String email = "test@test.com";
        Member member = MemberTestBuilder.aMember()
                .withEmail(email)
                .withUserId(1L)
                .build();
        CustomUserDetails userDetails = new CustomUserDetails(member);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        String token = jwtTokenProvider.createAccessToken(authentication);

        // when
        String extractedEmail = jwtTokenProvider.getUserEmailFromToken(token);

        // then
        assertThat(extractedEmail).isEqualTo(email);
    }

    @Test
    @DisplayName("토큰으로부터 Authentication 객체 생성 성공")
    void getAuthentication_Success() {
        // given
        String email = "test@test.com";
        Member member = MemberTestBuilder.aMember()
                .withEmail(email)
                .withUserId(1L)
                .build();
        CustomUserDetails userDetails = new CustomUserDetails(member);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        String token = jwtTokenProvider.createAccessToken(authentication);

        given(userDetailsService.loadUserByUsername(email)).willReturn(userDetails);

        // when
        Authentication resultAuth = jwtTokenProvider.getAuthentication(token);

        // then
        assertThat(resultAuth).isNotNull();
        assertThat(resultAuth.getName()).isEqualTo(email);
    }

    @Test
    @DisplayName("유효하지 않은 토큰 검증 실패")
    void validateToken_InvalidToken() {
        // given
        String invalidToken = "invalid.token.here";

        // when
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("만료된 토큰 검증 실패")
    void validateToken_ExpiredToken() throws InterruptedException {
        // given - 매우 짧은 유효기간으로 토큰 생성
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider(userDetailsService);
        ReflectionTestUtils.setField(shortLivedProvider, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(shortLivedProvider, "accessTokenValidityInSeconds", 0L);
        ReflectionTestUtils.setField(shortLivedProvider, "refreshTokenValidityInSeconds", 0L);
        shortLivedProvider.init();

        Member member = MemberTestBuilder.aMember().withEmail("test@test.com").build();
        CustomUserDetails userDetails = new CustomUserDetails(member);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        String token = shortLivedProvider.createAccessToken(authentication);
        Thread.sleep(100); // 토큰 만료 대기

        // when
        boolean isValid = shortLivedProvider.validateToken(token);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("토큰의 남은 유효 시간 확인")
    void getTokenExpirationTime_Success() {
        // given
        Member member = MemberTestBuilder.aMember()
                .withEmail("test@test.com")
                .withUserId(1L)
                .build();
        CustomUserDetails userDetails = new CustomUserDetails(member);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        String token = jwtTokenProvider.createAccessToken(authentication);

        // when
        long expirationTime = jwtTokenProvider.getTokenExpirationTime(token);

        // then
        assertThat(expirationTime).isGreaterThan(0);
        assertThat(expirationTime).isLessThanOrEqualTo(ACCESS_TOKEN_VALIDITY * 1000);
    }

    @Test
    @DisplayName("빈 토큰 검증 실패")
    void validateToken_EmptyToken() {
        // when
        boolean isValid = jwtTokenProvider.validateToken("");

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("null 토큰 검증 실패")
    void validateToken_NullToken() {
        // when
        boolean isValid = jwtTokenProvider.validateToken(null);

        // then
        assertThat(isValid).isFalse();
    }

}
