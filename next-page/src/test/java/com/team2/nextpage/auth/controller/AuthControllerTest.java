package com.team2.nextpage.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team2.nextpage.auth.dto.LoginRequest;
import com.team2.nextpage.auth.dto.TokenResponse;
import com.team2.nextpage.auth.service.AuthService;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("로그인 성공")
    void login_Success() throws Exception {
        // given
        LoginRequest request = RequestDtoTestBuilder.createLoginRequest(
            "test@test.com",
            "password123"
        );
        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken("access-token-value")
                .refreshToken("refresh-token-value")
                .build();

        given(authService.login(any(LoginRequest.class))).willReturn(tokenResponse);

        // when & then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token-value"));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 자격 증명")
    void login_InvalidCredentials() throws Exception {
        // given
        LoginRequest request = RequestDtoTestBuilder.createLoginRequest(
            "test@test.com",
            "wrongpassword"
        );

        given(authService.login(any(LoginRequest.class)))
                .willThrow(new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        // when & then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("로그인 실패 - 사용자를 찾을 수 없음")
    void login_MemberNotFound() throws Exception {
        // given
        LoginRequest request = RequestDtoTestBuilder.createLoginRequest(
            "nonexistent@test.com",
            "password123"
        );

        given(authService.login(any(LoginRequest.class)))
                .willThrow(new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // when & then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("액세스 토큰 재발급 성공")
    void reissue_Success() throws Exception {
        // given
        String refreshToken = "valid-refresh-token";
        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .build();

        given(authService.reissueAccessToken(anyString())).willReturn(tokenResponse);

        // when & then
        mockMvc.perform(post("/api/auth/reissue")
                .cookie(new jakarta.servlet.http.Cookie("refreshToken", refreshToken)))
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));

        verify(authService).reissueAccessToken(refreshToken);
    }

    @Test
    @DisplayName("액세스 토큰 재발급 실패 - 유효하지 않은 리프레시 토큰")
    void reissue_InvalidRefreshToken() throws Exception {
        // given
        String invalidRefreshToken = "invalid-refresh-token";

        given(authService.reissueAccessToken(anyString()))
                .willThrow(new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        // when & then
        mockMvc.perform(post("/api/auth/reissue")
                .cookie(new jakarta.servlet.http.Cookie("refreshToken", invalidRefreshToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_Success() throws Exception {
        // given
        Long userId = 1L;

        try (MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentUserId).thenReturn(userId);
            doNothing().when(authService).logout(userId);

            // when & then
            mockMvc.perform(post("/api/auth/logout"))
                    .andExpect(status().isOk())
                    .andExpect(header().exists("Set-Cookie"))
                    .andExpect(jsonPath("$.success").value(true));

            verify(authService).logout(userId);
        }
    }

    @Test
    @DisplayName("로그아웃 실패 - 인증되지 않은 사용자")
    void logout_Unauthenticated() throws Exception {
        // given
        try (MockedStatic<SecurityUtil> mockedSecurity = mockStatic(SecurityUtil.class)) {
            mockedSecurity.when(SecurityUtil::getCurrentUserId).thenReturn(null);

            // when & then
            mockMvc.perform(post("/api/auth/logout"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
