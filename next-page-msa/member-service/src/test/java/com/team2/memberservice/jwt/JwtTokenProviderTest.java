package com.team2.memberservice.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.team2.memberservice.command.member.entity.*;
import com.team2.memberservice.config.security.CustomUserDetails;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;
import org.assertj.core.api.Assertions;
import org.mockito.BDDMockito;

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

    // ===== createAccessToken 테스트 =====

    @Test
    @DisplayName("Access Token 생성 - 정상")
    void createAccessToken_Success() {
        // Given
        Member member = createTestMember(1L, "test@test.com", "TestUser");
        CustomUserDetails userDetails = new CustomUserDetails(member);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        // When
        String token = jwtTokenProvider.createAccessToken(authentication);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("Access Token 생성 - Principal이 CustomUserDetails가 아닌 경우")
    void createAccessToken_PrincipalNotCustomUserDetails() {
        // Given
        User userDetails = new User("test@test.com", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        // When
        String token = jwtTokenProvider.createAccessToken(authentication);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        // userId가 null이어도 토큰은 생성됨 (경고 로그만 출력)
    }

    @Test
    @DisplayName("Access Token 생성 - ADMIN 권한")
    void createAccessToken_AdminRole() {
        // Given
        Member adminMember = Member.builder()
                .userId(100L)
                .userEmail("admin@test.com")
                .userPw("password")
                .userNicknm("Admin")
                .userRole(UserRole.ADMIN)
                .userStatus(UserStatus.ACTIVE)
                .build();
        CustomUserDetails userDetails = new CustomUserDetails(adminMember);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        // When
        String token = jwtTokenProvider.createAccessToken(authentication);

        // Then
        assertThat(token).isNotNull();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        String email = jwtTokenProvider.getUserEmailFromToken(token);
        assertThat(email).isEqualTo("admin@test.com");
    }

    @Test
    @DisplayName("Access Token 생성 후 Authentication 복원")
    void createAndRestoreAuthentication() {
        // Given
        String email = "restore@test.com";
        Member member = createTestMember(5L, email, "RestoreUser");
        CustomUserDetails originalUserDetails = new CustomUserDetails(member);
        Authentication originalAuth = new UsernamePasswordAuthenticationToken(
                originalUserDetails, null, originalUserDetails.getAuthorities());
        String token = jwtTokenProvider.createAccessToken(originalAuth);

        given(userDetailsService.loadUserByUsername(email)).willReturn(originalUserDetails);

        // When
        Authentication restoredAuth = jwtTokenProvider.getAuthentication(token);

        // Then
        assertThat(restoredAuth).isNotNull();
        assertThat(restoredAuth.getName()).isEqualTo(email);
        assertThat(restoredAuth.getAuthorities()).hasSize(1);
    }

    // ===== createRefreshToken 테스트 =====

    @Test
    @DisplayName("Refresh Token 생성 - 정상")
    void createRefreshToken_Success() {
        // Given
        Member member = createTestMember(1L, "test@test.com", "TestUser");
        CustomUserDetails userDetails = new CustomUserDetails(member);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        // When
        String token = jwtTokenProvider.createRefreshToken(authentication);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("Refresh Token 생성 - Principal이 CustomUserDetails가 아닌 경우")
    void createRefreshToken_PrincipalNotCustomUserDetails() {
        // Given
        User userDetails = new User("test@test.com", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        // When
        String token = jwtTokenProvider.createRefreshToken(authentication);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    @DisplayName("Refresh Token에서 이메일 추출")
    void getEmailFromRefreshToken() {
        // Given
        String email = "refresh@test.com";
        Member member = createTestMember(2L, email, "RefreshUser");
        CustomUserDetails userDetails = new CustomUserDetails(member);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

        // When
        String extractedEmail = jwtTokenProvider.getUserEmailFromToken(refreshToken);

        // Then
        assertThat(extractedEmail).isEqualTo(email);
    }

    @Test
    @DisplayName("Refresh Token 유효성 검증")
    void validateRefreshToken() {
        // Given
        Member member = createTestMember(3L, "test@test.com", "TestUser");
        CustomUserDetails userDetails = new CustomUserDetails(member);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

        // When
        boolean isValid = jwtTokenProvider.validateToken(refreshToken);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Refresh Token 만료 시간 확인")
    void getRefreshTokenExpirationTime() {
        // Given
        Member member = createTestMember(4L, "test@test.com", "TestUser");
        CustomUserDetails userDetails = new CustomUserDetails(member);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

        // When
        long expirationTime = jwtTokenProvider.getTokenExpirationTime(refreshToken);

        // Then
        assertThat(expirationTime).isGreaterThan(0);
        assertThat(expirationTime).isLessThanOrEqualTo(REFRESH_TOKEN_VALIDITY * 1000);
    }

    // ===== getAuthentication 테스트 =====

    @Test
    @DisplayName("토큰으로부터 Authentication 객체 생성 - 정상")
    void getAuthentication_Success() {
        // Given
        String email = "test@test.com";
        Member member = createTestMember(1L, email, "TestUser");
        CustomUserDetails userDetails = new CustomUserDetails(member);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        String token = jwtTokenProvider.createAccessToken(authentication);

        given(userDetailsService.loadUserByUsername(email)).willReturn(userDetails);

        // When
        Authentication resultAuth = jwtTokenProvider.getAuthentication(token);

        // Then
        assertThat(resultAuth).isNotNull();
        assertThat(resultAuth.getName()).isEqualTo(email);
    }

    @Test
    @DisplayName("토큰으로부터 Authentication 생성 - 권한 정보 없음")
    void getAuthentication_NoAuthorities() {
        // Given
        String tokenWithoutAuth = createTokenWithoutAuthorities();

        // When & Then
        assertThatThrownBy(() -> jwtTokenProvider.getAuthentication(tokenWithoutAuth))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("권한 정보가 없는 토큰입니다.");
    }

    @Test
    @DisplayName("토큰으로부터 Authentication 생성 - 이메일 정보 없음")
    void getAuthentication_NoEmail() {
        // Given
        String tokenWithoutEmail = createTokenWithoutEmail();

        // When & Then
        assertThatThrownBy(() -> jwtTokenProvider.getAuthentication(tokenWithoutEmail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일 정보가 없는 토큰입니다.");
    }

    // ===== validateToken 테스트 (Branch Coverage) =====

    @Test
    @DisplayName("토큰 검증 - 유효한 토큰")
    void validateToken_Valid() {
        // Given
        Member member = createTestMember(1L, "test@test.com", "TestUser");
        CustomUserDetails userDetails = new CustomUserDetails(member);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        String token = jwtTokenProvider.createAccessToken(authentication);

        // When
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("토큰 검증 - MalformedJwtException (잘못된 형식)")
    void validateToken_MalformedJwt() {
        // Given
        String malformedToken = "invalid.token.here";

        // When
        boolean isValid = jwtTokenProvider.validateToken(malformedToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("토큰 검증 - ExpiredJwtException (만료된 토큰)")
    void validateToken_ExpiredToken() throws InterruptedException {
        // Given - 매우 짧은 유효기간으로 토큰 생성
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider(userDetailsService);
        ReflectionTestUtils.setField(shortLivedProvider, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(shortLivedProvider, "accessTokenValidityInSeconds", 0L);
        ReflectionTestUtils.setField(shortLivedProvider, "refreshTokenValidityInSeconds", 0L);
        shortLivedProvider.init();

        Member member = createTestMember(1L, "test@test.com", "TestUser");
        CustomUserDetails userDetails = new CustomUserDetails(member);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        String token = shortLivedProvider.createAccessToken(authentication);
        Thread.sleep(100); // 토큰 만료 대기

        // When
        boolean isValid = shortLivedProvider.validateToken(token);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("토큰 검증 - IllegalArgumentException (빈 토큰)")
    void validateToken_EmptyToken() {
        // When
        boolean isValid = jwtTokenProvider.validateToken("");

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("토큰 검증 - IllegalArgumentException (null 토큰)")
    void validateToken_NullToken() {
        // When
        boolean isValid = jwtTokenProvider.validateToken(null);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("토큰 검증 - 잘못된 형식의 토큰 (점이 부족)")
    void validateToken_InvalidFormat() {
        // Given - 점(.)이 부족한 토큰
        String invalidFormatToken = "invalidtoken";

        // When
        boolean isValid = jwtTokenProvider.validateToken(invalidFormatToken);

        // Then
        assertThat(isValid).isFalse();
    }

    // ===== getUserEmailFromToken 테스트 =====

    @Test
    @DisplayName("토큰에서 이메일 추출 - 정상")
    void getUserEmailFromToken_Success() {
        // Given
        String email = "test@test.com";
        Member member = createTestMember(1L, email, "TestUser");
        CustomUserDetails userDetails = new CustomUserDetails(member);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        String token = jwtTokenProvider.createAccessToken(authentication);

        // When
        String extractedEmail = jwtTokenProvider.getUserEmailFromToken(token);

        // Then
        assertThat(extractedEmail).isEqualTo(email);
    }

    @Test
    @DisplayName("토큰에서 이메일 추출 - 만료된 토큰도 Claims 반환")
    void getUserEmailFromToken_ExpiredToken() throws InterruptedException {
        // Given - 만료된 토큰
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider(userDetailsService);
        ReflectionTestUtils.setField(shortLivedProvider, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(shortLivedProvider, "accessTokenValidityInSeconds", 0L);
        shortLivedProvider.init();

        Member member = createTestMember(1L, "test@test.com", "TestUser");
        CustomUserDetails userDetails = new CustomUserDetails(member);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        String token = shortLivedProvider.createAccessToken(authentication);
        Thread.sleep(100); // 토큰 만료 대기

        // When - parseClaims는 만료된 토큰도 Claims를 반환함
        String email = shortLivedProvider.getUserEmailFromToken(token);

        // Then
        assertThat(email).isEqualTo("test@test.com");
    }

    // ===== getTokenExpirationTime 테스트 =====

    @Test
    @DisplayName("토큰의 남은 유효 시간 확인 - 정상")
    void getTokenExpirationTime_Success() {
        // Given
        Member member = createTestMember(1L, "test@test.com", "TestUser");
        CustomUserDetails userDetails = new CustomUserDetails(member);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        String token = jwtTokenProvider.createAccessToken(authentication);

        // When
        long expirationTime = jwtTokenProvider.getTokenExpirationTime(token);

        // Then
        assertThat(expirationTime).isGreaterThan(0);
        assertThat(expirationTime).isLessThanOrEqualTo(ACCESS_TOKEN_VALIDITY * 1000);
    }

    @Test
    @DisplayName("토큰의 남은 유효 시간 확인 - 만료된 토큰은 음수")
    void getTokenExpirationTime_ExpiredToken() throws InterruptedException {
        // Given - 만료된 토큰
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider(userDetailsService);
        ReflectionTestUtils.setField(shortLivedProvider, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(shortLivedProvider, "accessTokenValidityInSeconds", 0L);
        shortLivedProvider.init();

        Member member = createTestMember(1L, "test@test.com", "TestUser");
        CustomUserDetails userDetails = new CustomUserDetails(member);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        String token = shortLivedProvider.createAccessToken(authentication);
        Thread.sleep(100); // 토큰 만료 대기

        // When
        long expirationTime = shortLivedProvider.getTokenExpirationTime(token);

        // Then
        assertThat(expirationTime).isLessThan(0); // 만료되었으므로 음수
    }

    // ===== Helper Methods =====

    private Member createTestMember(Long userId, String email, String nickname) {
        return Member.builder()
                .userId(userId)
                .userEmail(email)
                .userPw("encodedPassword")
                .userNicknm(nickname)
                .userRole(UserRole.USER)
                .userStatus(UserStatus.ACTIVE)
                .build();
    }

    private String createTokenWithoutAuthorities() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject("1")
                .claim("email", "test@test.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    private String createTokenWithoutEmail() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject("1")
                .claim("auth", "ROLE_USER")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }
}
