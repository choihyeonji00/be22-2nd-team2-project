package com.team2.memberservice.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team2.commonmodule.error.BusinessException;
import com.team2.commonmodule.error.ErrorCode;
import com.team2.memberservice.auth.dto.LoginRequest;
import com.team2.memberservice.auth.dto.TokenResponse;
import com.team2.memberservice.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController 단위 테스트
 *
 * MockMvcBuilders.standaloneSetup을 사용하여 컨트롤러 계층만 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController 단위 테스트")
class AuthControllerTest {

        private MockMvc mockMvc;
        private ObjectMapper objectMapper;

        @Mock
        private AuthService authService;

        @InjectMocks
        private AuthController authController;

        @BeforeEach
        void setUp() {
                objectMapper = new ObjectMapper();
                mockMvc = MockMvcBuilders.standaloneSetup(authController)
                                .setControllerAdvice(new TestExceptionHandler.TestExceptionHandlerAdvice())
                                .build();
        }

        @Nested
        @DisplayName("POST /api/auth/login - 로그인")
        class LoginTest {

                @Test
                @DisplayName("성공 - 올바른 자격 증명으로 로그인하면 200 OK와 토큰을 반환한다")
                void loginSuccess() throws Exception {
                        // Given
                        LoginRequest request = new LoginRequest("test@example.com", "password123");
                        TokenResponse tokenResponse = TokenResponse.builder()
                                        .accessToken("access-token")
                                        .refreshToken("refresh-token")
                                        .tokenType("Bearer")
                                        .build();

                        given(authService.login(any(LoginRequest.class))).willReturn(tokenResponse);

                        // When & Then
                        mockMvc.perform(post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andDo(print())
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                                        .andExpect(cookie().exists("refreshToken"));
                }

                @Test
                @DisplayName("실패 - 잘못된 자격 증명이면 401 Unauthorized를 반환한다")
                void loginFail_InvalidCredentials() throws Exception {
                        // Given
                        LoginRequest request = new LoginRequest("test@example.com", "wrongpassword");
                        given(authService.login(any(LoginRequest.class)))
                                        .willThrow(new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다."));

                        // When & Then
                        mockMvc.perform(post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andDo(print())
                                        .andExpect(status().isUnauthorized());
                }

                @Test
                @DisplayName("실패 - 승인 대기 상태면 403 Forbidden을 반환한다")
                void loginFail_PendingApproval() throws Exception {
                        // Given
                        LoginRequest request = new LoginRequest("admin@example.com", "password123");
                        given(authService.login(any(LoginRequest.class)))
                                        .willThrow(new BusinessException(ErrorCode.ACCOUNT_APPROVAL_PENDING));

                        // When & Then
                        mockMvc.perform(post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andDo(print())
                                        .andExpect(status().isForbidden())
                                        .andExpect(jsonPath("$.code").value("A005")); // ACCOUNT_APPROVAL_PENDING code
                }

                @Test
                @DisplayName("실패 - 이메일 형식이 잘못되면 400 Bad Request를 반환한다")
                void loginFail_InvalidEmailFormat() throws Exception {
                        // Given - 잘못된 이메일 형식
                        String requestJson = "{\"userEmail\": \"invalid-email\", \"userPw\": \"password123\"}";

                        // When & Then
                        mockMvc.perform(post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestJson))
                                        .andDo(print())
                                        .andExpect(status().isBadRequest());
                }

                @Test
                @DisplayName("실패 - 비밀번호가 누락되면 400 Bad Request를 반환한다")
                void loginFail_MissingPassword() throws Exception {
                        // Given - 비밀번호 누락
                        String requestJson = "{\"userEmail\": \"test@example.com\"}";

                        // When & Then
                        mockMvc.perform(post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestJson))
                                        .andDo(print())
                                        .andExpect(status().isBadRequest());
                }
        }

        @Nested
        @DisplayName("POST /api/auth/refresh - 토큰 갱신")
        class RefreshTest {

                @Test
                @DisplayName("성공 - 유효한 Refresh Token으로 새 토큰을 발급받는다")
                void refreshSuccess() throws Exception {
                        // Given
                        TokenResponse tokenResponse = TokenResponse.builder()
                                        .accessToken("new-access-token")
                                        .refreshToken("new-refresh-token")
                                        .tokenType("Bearer")
                                        .build();

                        given(authService.refreshToken(anyString())).willReturn(tokenResponse);

                        // When & Then
                        mockMvc.perform(post("/api/auth/refresh")
                                        .cookie(new jakarta.servlet.http.Cookie("refreshToken", "valid-refresh-token")))
                                        .andDo(print())
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));
                }

                @Test
                @DisplayName("실패 - Refresh Token이 없으면 401 Unauthorized를 반환한다")
                void refreshFail_NoToken() throws Exception {
                        // When & Then
                        mockMvc.perform(post("/api/auth/refresh"))
                                        .andDo(print())
                                        .andExpect(status().isUnauthorized());
                }

                @Test
                @DisplayName("실패 - 유효하지 않은 Refresh Token이면 401 Unauthorized를 반환한다")
                void refreshFail_InvalidToken() throws Exception {
                        // Given
                        given(authService.refreshToken(anyString()))
                                        .willThrow(new BusinessException(ErrorCode.INVALID_TOKEN));

                        // When & Then
                        mockMvc.perform(post("/api/auth/refresh")
                                        .cookie(new jakarta.servlet.http.Cookie("refreshToken", "invalid-token")))
                                        .andDo(print())
                                        .andExpect(status().isUnauthorized())
                                        .andExpect(jsonPath("$.code").value("A004"));
                }
        }

        @Nested
        @DisplayName("POST /api/auth/logout - 로그아웃")
        class LogoutTest {

                @Test
                @DisplayName("성공 - 로그아웃하면 200 OK와 쿠키 삭제 응답을 반환한다")
                void logoutSuccess() throws Exception {
                        // Given
                        willDoNothing().given(authService).logout(anyString());

                        // When & Then
                        mockMvc.perform(post("/api/auth/logout")
                                        .cookie(new jakarta.servlet.http.Cookie("refreshToken", "valid-refresh-token")))
                                        .andDo(print())
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(cookie().maxAge("refreshToken", 0));
                }

                @Test
                @DisplayName("성공 - Refresh Token 없이도 로그아웃할 수 있다")
                void logoutWithoutToken() throws Exception {
                        // When & Then
                        mockMvc.perform(post("/api/auth/logout"))
                                        .andDo(print())
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true));
                }
        }
}
