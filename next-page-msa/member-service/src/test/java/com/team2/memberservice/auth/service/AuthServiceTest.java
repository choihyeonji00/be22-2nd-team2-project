package com.team2.memberservice.auth.service;

import com.team2.commonmodule.error.BusinessException;
import com.team2.commonmodule.error.ErrorCode;
import com.team2.memberservice.auth.dto.LoginRequest;
import com.team2.memberservice.auth.dto.TokenResponse;
import com.team2.memberservice.auth.entity.RefreshToken;
import com.team2.memberservice.auth.repository.AuthRepository;
import com.team2.memberservice.command.member.entity.Member;
import com.team2.memberservice.command.member.entity.UserRole;
import com.team2.memberservice.command.member.entity.UserStatus;
import com.team2.memberservice.command.member.repository.MemberRepository;
import com.team2.memberservice.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import org.assertj.core.api.Assertions;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;

/**
 * AuthService 단위 테스트
 * Mockito를 사용하여 의존성을 격리한 순수한 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 단위 테스트")
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthRepository authRepository;

    private Member testMember;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // Refresh Token 유효기간 설정 (7일 = 604800초)
        ReflectionTestUtils.setField(authService, "refreshTokenValidityInSeconds", 604800L);

        // 테스트용 Member 객체 생성
        testMember = Member.builder()
                .userId(1L)
                .userEmail("test@example.com")
                .userPw("$2a$10$encoded.password.hash")
                .userNicknm("테스터")
                .userRole(UserRole.USER)
                .userStatus(UserStatus.ACTIVE)
                .build();

        // 테스트용 로그인 요청 생성
        loginRequest = new LoginRequest("test@example.com", "rawPassword123");
    }

    @Test
    @DisplayName("로그인 성공 - Access Token과 Refresh Token을 반환한다")
    void loginSuccess() {
        // Given
        given(memberRepository.findByUserEmail(loginRequest.getUserEmail()))
                .willReturn(Optional.of(testMember));
        given(passwordEncoder.matches(loginRequest.getUserPw(), testMember.getUserPw()))
                .willReturn(true);
        given(jwtTokenProvider.createAccessToken(any()))
                .willReturn("access.token.value");
        given(jwtTokenProvider.createRefreshToken(any()))
                .willReturn("refresh.token.value");
        given(authRepository.findByUserEmail(testMember.getUserEmail()))
                .willReturn(Optional.empty());

        // When
        TokenResponse response = authService.login(loginRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access.token.value");
        assertThat(response.getRefreshToken()).isEqualTo("refresh.token.value");

        // Verify
        then(memberRepository).should(times(1)).findByUserEmail(loginRequest.getUserEmail());
        then(passwordEncoder).should(times(1)).matches(loginRequest.getUserPw(), testMember.getUserPw());
        then(jwtTokenProvider).should(times(1)).createAccessToken(any());
        then(jwtTokenProvider).should(times(1)).createRefreshToken(any());
        then(authRepository).should(times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 이메일")
    void loginFail_UserNotFound() {
        // Given
        given(memberRepository.findByUserEmail(loginRequest.getUserEmail()))
                .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("아이디 또는 비밀번호가 일치하지 않습니다");

        // Verify - 비밀번호 검증까지 가지 않음
        then(passwordEncoder).should(never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void loginFail_PasswordMismatch() {
        // Given
        given(memberRepository.findByUserEmail(loginRequest.getUserEmail()))
                .willReturn(Optional.of(testMember));
        given(passwordEncoder.matches(loginRequest.getUserPw(), testMember.getUserPw()))
                .willReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("아이디 또는 비밀번호가 일치하지 않습니다");

        // Verify - 토큰 생성까지 가지 않음
        then(jwtTokenProvider).should(never()).createAccessToken(any());
    }

    @Test
    @DisplayName("로그인 실패 - 관리자 승인 대기 상태")
    void loginFail_PendingStatus() {
        // Given
        Member pendingMember = Member.builder()
                .userId(2L)
                .userEmail("admin@example.com")
                .userPw("$2a$10$encoded.password.hash")
                .userNicknm("관리자후보")
                .userRole(UserRole.ADMIN)
                .userStatus(UserStatus.PENDING)
                .build();

        given(memberRepository.findByUserEmail(loginRequest.getUserEmail()))
                .willReturn(Optional.of(pendingMember));

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCOUNT_APPROVAL_PENDING);

        // Verify
        then(passwordEncoder).should(never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("토큰 갱신 성공 - 새로운 Access Token과 Refresh Token을 반환한다")
    void refreshTokenSuccess() {
        // Given
        String oldRefreshToken = "old.refresh.token";
        RefreshToken storedToken = RefreshToken.builder()
                .userEmail(testMember.getUserEmail())
                .token(oldRefreshToken)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();

        given(jwtTokenProvider.validateToken(oldRefreshToken)).willReturn(true);
        given(jwtTokenProvider.getUserEmailFromToken(oldRefreshToken))
                .willReturn(testMember.getUserEmail());
        given(authRepository.findByUserEmail(testMember.getUserEmail()))
                .willReturn(Optional.of(storedToken));
        given(memberRepository.findByUserEmail(testMember.getUserEmail()))
                .willReturn(Optional.of(testMember));
        given(jwtTokenProvider.createAccessToken(any()))
                .willReturn("new.access.token");
        given(jwtTokenProvider.createRefreshToken(any()))
                .willReturn("new.refresh.token");

        // When
        TokenResponse response = authService.refreshToken(oldRefreshToken);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new.access.token");
        assertThat(response.getRefreshToken()).isEqualTo("new.refresh.token");

        // Verify
        then(jwtTokenProvider).should(times(1)).validateToken(oldRefreshToken);
        then(authRepository).should(atLeast(1)).findByUserEmail(testMember.getUserEmail());
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 유효하지 않은 Refresh Token")
    void refreshTokenFail_InvalidToken() {
        // Given
        String invalidToken = "invalid.refresh.token";
        given(jwtTokenProvider.validateToken(invalidToken)).willReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.refreshToken(invalidToken))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("유효하지 않은 Refresh Token입니다");

        // Verify
        then(authRepository).should(never()).findByUserEmail(anyString());
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 저장된 Refresh Token 없음")
    void refreshTokenFail_NoStoredToken() {
        // Given
        String refreshToken = "valid.refresh.token";
        given(jwtTokenProvider.validateToken(refreshToken)).willReturn(true);
        given(jwtTokenProvider.getUserEmailFromToken(refreshToken))
                .willReturn(testMember.getUserEmail());
        given(authRepository.findByUserEmail(testMember.getUserEmail()))
                .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.refreshToken(refreshToken))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("저장된 Refresh Token이 없습니다");
    }

    @Test
    @DisplayName("토큰 갱신 실패 - Refresh Token 불일치")
    void refreshTokenFail_TokenMismatch() {
        // Given
        String providedToken = "provided.token";
        String storedTokenValue = "stored.token.different";

        RefreshToken storedToken = RefreshToken.builder()
                .userEmail(testMember.getUserEmail())
                .token(storedTokenValue)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();

        given(jwtTokenProvider.validateToken(providedToken)).willReturn(true);
        given(jwtTokenProvider.getUserEmailFromToken(providedToken))
                .willReturn(testMember.getUserEmail());
        given(authRepository.findByUserEmail(testMember.getUserEmail()))
                .willReturn(Optional.of(storedToken));

        // When & Then
        assertThatThrownBy(() -> authService.refreshToken(providedToken))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Refresh Token이 일치하지 않습니다");
    }

    @Test
    @DisplayName("토큰 갱신 실패 - Refresh Token 만료")
    void refreshTokenFail_TokenExpired() {
        // Given
        String expiredToken = "expired.refresh.token";
        RefreshToken storedToken = RefreshToken.builder()
                .userEmail(testMember.getUserEmail())
                .token(expiredToken)
                .expiryDate(LocalDateTime.now().minusDays(1)) // 만료된 토큰
                .build();

        given(jwtTokenProvider.validateToken(expiredToken)).willReturn(true);
        given(jwtTokenProvider.getUserEmailFromToken(expiredToken))
                .willReturn(testMember.getUserEmail());
        given(authRepository.findByUserEmail(testMember.getUserEmail()))
                .willReturn(Optional.of(storedToken));

        // When & Then
        assertThatThrownBy(() -> authService.refreshToken(expiredToken))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Refresh Token이 만료되었습니다");

        // Verify - 만료된 토큰은 삭제됨
        then(authRepository).should(times(1)).delete(storedToken);
    }

    @Test
    @DisplayName("로그아웃 성공 - Refresh Token을 삭제한다")
    void logoutSuccess() {
        // Given
        String refreshToken = "valid.refresh.token";
        given(jwtTokenProvider.validateToken(refreshToken)).willReturn(true);
        given(jwtTokenProvider.getUserEmailFromToken(refreshToken))
                .willReturn(testMember.getUserEmail());

        // When
        authService.logout(refreshToken);

        // Then
        then(jwtTokenProvider).should(times(1)).validateToken(refreshToken);
        then(jwtTokenProvider).should(times(1)).getUserEmailFromToken(refreshToken);
        then(authRepository).should(times(1)).deleteByUserEmail(testMember.getUserEmail());
    }

    @Test
    @DisplayName("로그아웃 - 유효하지 않은 토큰이어도 예외를 던지지 않는다")
    void logout_InvalidToken_NoException() {
        // Given
        String invalidToken = "invalid.refresh.token";
        given(jwtTokenProvider.validateToken(invalidToken)).willReturn(false);

        // When & Then
        assertThatCode(() -> authService.logout(invalidToken))
                .doesNotThrowAnyException();

        // Verify - 유효하지 않은 토큰이면 삭제 작업을 수행하지 않음
        then(authRepository).should(never()).deleteByUserEmail(anyString());
    }

    @Test
    @DisplayName("Refresh Token 저장 - 기존 토큰이 없으면 새로 저장한다")
    void saveRefreshToken_NewToken() {
        // Given
        String newToken = "new.refresh.token";
        given(memberRepository.findByUserEmail(testMember.getUserEmail()))
                .willReturn(Optional.of(testMember));
        given(passwordEncoder.matches(loginRequest.getUserPw(), testMember.getUserPw()))
                .willReturn(true);
        given(jwtTokenProvider.createAccessToken(any()))
                .willReturn("access.token");
        given(jwtTokenProvider.createRefreshToken(any()))
                .willReturn(newToken);
        given(authRepository.findByUserEmail(testMember.getUserEmail()))
                .willReturn(Optional.empty());

        // When
        authService.login(loginRequest);

        // Then
        then(authRepository).should(times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Refresh Token 갱신 - 기존 토큰이 있으면 업데이트한다")
    void updateRefreshToken_ExistingToken() {
        // Given
        String oldToken = "old.refresh.token";
        String newToken = "new.refresh.token";

        RefreshToken existingToken = RefreshToken.builder()
                .userEmail(testMember.getUserEmail())
                .token(oldToken)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();

        given(memberRepository.findByUserEmail(testMember.getUserEmail()))
                .willReturn(Optional.of(testMember));
        given(passwordEncoder.matches(loginRequest.getUserPw(), testMember.getUserPw()))
                .willReturn(true);
        given(jwtTokenProvider.createAccessToken(any()))
                .willReturn("access.token");
        given(jwtTokenProvider.createRefreshToken(any()))
                .willReturn(newToken);
        given(authRepository.findByUserEmail(testMember.getUserEmail()))
                .willReturn(Optional.of(existingToken));

        // When
        authService.login(loginRequest);

        // Then
        // 기존 토큰이 있으면 업데이트만 하고 save는 호출하지 않음
        then(authRepository).should(never()).save(any(RefreshToken.class));
    }
}
