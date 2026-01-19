package com.team2.nextpage.command.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team2.nextpage.command.member.dto.request.SignUpRequest;
import com.team2.nextpage.command.member.service.MemberService;
import com.team2.nextpage.common.error.BusinessException;
import com.team2.nextpage.common.error.ErrorCode;
import com.team2.nextpage.fixtures.RequestDtoTestBuilder;
import com.team2.nextpage.jwt.JwtAuthenticationFilter;
import com.team2.nextpage.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.doNothing;
import static org.mockito.BDDMockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberController.class)
@AutoConfigureMockMvc(addFilters = false)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("회원가입 성공 테스트")
    void signUp_Success() throws Exception {
        // given
        SignUpRequest request = RequestDtoTestBuilder.createSignUpRequest(
            "test@test.com",
            "password123",
            "nickname"
        );
        doNothing().when(memberService).registUser(any(SignUpRequest.class));

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("회원가입 성공"));

        verify(memberService).registUser(any(SignUpRequest.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void signUp_DuplicateEmail() throws Exception {
        // given
        SignUpRequest request = RequestDtoTestBuilder.createSignUpRequest(
            "duplicate@test.com",
            "password123",
            "nickname"
        );
        doThrow(new BusinessException(ErrorCode.DUPLICATE_EMAIL))
                .when(memberService).registUser(any(SignUpRequest.class));

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("관리자 계정 생성 성공")
    void signUpAdmin_Success() throws Exception {
        // given
        SignUpRequest request = RequestDtoTestBuilder.createSignUpRequest(
            "admin@test.com",
            "adminpass123",
            "adminNick"
        );
        doNothing().when(memberService).registAdmin(any(SignUpRequest.class));

        // when & then
        mockMvc.perform(post("/api/auth/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("관리자 가입 성공"));

        verify(memberService).registAdmin(any(SignUpRequest.class));
    }

    @Test
    @DisplayName("관리자 계정 생성 실패 - 닉네임 중복")
    void signUpAdmin_DuplicateNickname() throws Exception {
        // given
        SignUpRequest request = RequestDtoTestBuilder.createSignUpRequest(
            "admin@test.com",
            "adminpass123",
            "duplicateNick"
        );
        doThrow(new BusinessException(ErrorCode.DUPLICATE_NICKNAME))
                .when(memberService).registAdmin(any(SignUpRequest.class));

        // when & then
        mockMvc.perform(post("/api/auth/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("회원 탈퇴 성공")
    void withdraw_Success() throws Exception {
        // given
        doNothing().when(memberService).withdraw();

        // when & then
        mockMvc.perform(delete("/api/auth/withdraw"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(memberService).withdraw();
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 인증되지 않은 사용자")
    void withdraw_Unauthenticated() throws Exception {
        // given
        doThrow(new BusinessException(ErrorCode.UNAUTHENTICATED))
                .when(memberService).withdraw();

        // when & then
        mockMvc.perform(delete("/api/auth/withdraw"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("회원 강제 탈퇴 성공 - 관리자")
    void withdrawByAdmin_Success() throws Exception {
        // given
        Long userId = 1L;
        doNothing().when(memberService).withdrawByAdmin(userId);

        // when & then
        mockMvc.perform(delete("/api/auth/admin/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(memberService).withdrawByAdmin(userId);
    }

    @Test
    @DisplayName("회원 강제 탈퇴 실패 - 권한 없음")
    void withdrawByAdmin_Unauthorized() throws Exception {
        // given
        Long userId = 1L;
        doThrow(new BusinessException(ErrorCode.UNAUTHORIZED))
                .when(memberService).withdrawByAdmin(userId);

        // when & then
        mockMvc.perform(delete("/api/auth/admin/users/{userId}", userId))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("회원 강제 탈퇴 실패 - 사용자를 찾을 수 없음")
    void withdrawByAdmin_MemberNotFound() throws Exception {
        // given
        Long userId = 999L;
        doThrow(new BusinessException(ErrorCode.MEMBER_NOT_FOUND))
                .when(memberService).withdrawByAdmin(userId);

        // when & then
        mockMvc.perform(delete("/api/auth/admin/users/{userId}", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("이메일 중복 체크 - 사용 가능")
    void checkEmail_Available() throws Exception {
        // given
        String email = "available@test.com";
        doNothing().when(memberService).validateDuplicateEmail(email);

        // when & then
        mockMvc.perform(get("/api/auth/check-email")
                .param("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(memberService).validateDuplicateEmail(email);
    }

    @Test
    @DisplayName("이메일 중복 체크 - 중복")
    void checkEmail_Duplicate() throws Exception {
        // given
        String email = "duplicate@test.com";
        doThrow(new BusinessException(ErrorCode.DUPLICATE_EMAIL))
                .when(memberService).validateDuplicateEmail(email);

        // when & then
        mockMvc.perform(get("/api/auth/check-email")
                .param("email", email))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("닉네임 중복 체크 - 사용 가능")
    void checkNickname_Available() throws Exception {
        // given
        String nickname = "availableNick";
        doNothing().when(memberService).validateDuplicateNicknm(nickname);

        // when & then
        mockMvc.perform(get("/api/auth/check-nickname")
                .param("nickname", nickname))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(memberService).validateDuplicateNicknm(nickname);
    }

    @Test
    @DisplayName("닉네임 중복 체크 - 중복")
    void checkNickname_Duplicate() throws Exception {
        // given
        String nickname = "duplicateNick";
        doThrow(new BusinessException(ErrorCode.DUPLICATE_NICKNAME))
                .when(memberService).validateDuplicateNicknm(nickname);

        // when & then
        mockMvc.perform(get("/api/auth/check-nickname")
                .param("nickname", nickname))
                .andExpect(status().isConflict());
    }
}
