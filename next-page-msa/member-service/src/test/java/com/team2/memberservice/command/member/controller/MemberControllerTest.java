package com.team2.memberservice.command.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team2.commonmodule.error.BusinessException;
import com.team2.commonmodule.error.ErrorCode;
import com.team2.memberservice.auth.controller.TestExceptionHandler;
import com.team2.memberservice.command.member.dto.request.SignUpRequest;
import com.team2.memberservice.command.member.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/**
 * MemberController 단위 테스트
 *
 * MockMvcBuilders.standaloneSetup을 사용하여 컨트롤러 계층만 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MemberController 단위 테스트")
class MemberControllerTest {

        private MockMvc mockMvc;
        private ObjectMapper objectMapper;

        @Mock
        private MemberService memberService;

        @InjectMocks
        private MemberController memberController;

        @BeforeEach
        void setUp() {
                objectMapper = new ObjectMapper();
                mockMvc = MockMvcBuilders.standaloneSetup(memberController)
                                .setControllerAdvice(new TestExceptionHandler.TestExceptionHandlerAdvice())
                                .build();
        }

        @Nested
        @DisplayName("POST /api/auth/signup - 회원가입")
        class SignUpTest {

                @Test
                @DisplayName("성공 - 유효한 정보로 회원가입하면 200 OK를 반환한다")
                void signupSuccess() throws Exception {
                        // Given
                        SignUpRequest request = new SignUpRequest("test@example.com", "password123", "테스터");
                        willDoNothing().given(memberService).registUser(any(SignUpRequest.class));

                        // When & Then
                        mockMvc.perform(post("/api/auth/signup")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andDo(print())
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true));
                }

                @Test
                @DisplayName("실패 - 이메일이 중복되면 409 Conflict를 반환한다")
                void signupFail_DuplicateEmail() throws Exception {
                        // Given
                        SignUpRequest request = new SignUpRequest("duplicate@example.com", "password123", "테스터");
                        willThrow(new BusinessException(ErrorCode.DUPLICATE_EMAIL))
                                        .given(memberService).registUser(any(SignUpRequest.class));

                        // When & Then
                        mockMvc.perform(post("/api/auth/signup")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andDo(print())
                                        .andExpect(status().isConflict())
                                        .andExpect(jsonPath("$.code").value("M001"));
                }

                @Test
                @DisplayName("실패 - 닉네임이 중복되면 409 Conflict를 반환한다")
                void signupFail_DuplicateNickname() throws Exception {
                        // Given
                        SignUpRequest request = new SignUpRequest("test@example.com", "password123", "중복닉네임");
                        willThrow(new BusinessException(ErrorCode.DUPLICATE_NICKNAME))
                                        .given(memberService).registUser(any(SignUpRequest.class));

                        // When & Then
                        mockMvc.perform(post("/api/auth/signup")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andDo(print())
                                        .andExpect(status().isConflict())
                                        .andExpect(jsonPath("$.code").value("M002"));
                }

                @Test
                @DisplayName("실패 - 이메일이 누락되면 400 Bad Request를 반환한다")
                void signupFail_MissingEmail() throws Exception {
                        // Given - 이메일 누락
                        String requestJson = "{\"userPw\": \"password123\", \"userNicknm\": \"테스터\"}";

                        // When & Then
                        mockMvc.perform(post("/api/auth/signup")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestJson))
                                        .andDo(print())
                                        .andExpect(status().isBadRequest());
                }

                @Test
                @DisplayName("실패 - 비밀번호가 누락되면 400 Bad Request를 반환한다")
                void signupFail_MissingPassword() throws Exception {
                        // Given - 비밀번호 누락
                        String requestJson = "{\"userEmail\": \"test@example.com\", \"userNicknm\": \"테스터\"}";

                        // When & Then
                        mockMvc.perform(post("/api/auth/signup")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestJson))
                                        .andDo(print())
                                        .andExpect(status().isBadRequest());
                }
        }

        @Nested
        @DisplayName("POST /api/auth/admin - 관리자 계정 생성")
        class CreateAdminTest {

                @Test
                @DisplayName("성공 - 유효한 정보로 관리자 계정을 생성하면 200 OK를 반환한다")
                void createAdminSuccess() throws Exception {
                        // Given
                        SignUpRequest request = new SignUpRequest("admin@example.com", "password123", "관리자");
                        willDoNothing().given(memberService).registAdmin(any(SignUpRequest.class));

                        // When & Then
                        mockMvc.perform(post("/api/auth/admin")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andDo(print())
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true));
                }

                @Test
                @DisplayName("실패 - 이메일이 중복되면 409 Conflict를 반환한다")
                void createAdminFail_DuplicateEmail() throws Exception {
                        // Given
                        SignUpRequest request = new SignUpRequest("duplicate@example.com", "password123", "관리자");
                        willThrow(new BusinessException(ErrorCode.DUPLICATE_EMAIL))
                                        .given(memberService).registAdmin(any(SignUpRequest.class));

                        // When & Then
                        mockMvc.perform(post("/api/auth/admin")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andDo(print())
                                        .andExpect(status().isConflict())
                                        .andExpect(jsonPath("$.code").value("M001"));
                }
        }

        @Nested
        @DisplayName("PATCH /api/auth/admin/approve/{userId} - 관리자 승인")
        class ApproveAdminTest {

                @Test
                @DisplayName("성공 - 관리자 승인에 성공하면 200 OK를 반환한다")
                void approveAdminSuccess() throws Exception {
                        // Given
                        willDoNothing().given(memberService).approveAdmin(anyLong());

                        // When & Then
                        mockMvc.perform(patch("/api/auth/admin/approve/1"))
                                        .andDo(print())
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true));
                }

                @Test
                @DisplayName("실패 - 존재하지 않는 사용자면 404 Not Found를 반환한다")
                void approveAdminFail_UserNotFound() throws Exception {
                        // Given
                        willThrow(new BusinessException(ErrorCode.MEMBER_NOT_FOUND))
                                        .given(memberService).approveAdmin(anyLong());

                        // When & Then
                        mockMvc.perform(patch("/api/auth/admin/approve/999"))
                                        .andDo(print())
                                        .andExpect(status().isNotFound())
                                        .andExpect(jsonPath("$.code").value("M003"));
                }

                @Test
                @DisplayName("실패 - 권한이 없으면 403 Forbidden을 반환한다")
                void approveAdminFail_AccessDenied() throws Exception {
                        // Given
                        willThrow(new BusinessException(ErrorCode.ACCESS_DENIED))
                                        .given(memberService).approveAdmin(anyLong());

                        // When & Then
                        mockMvc.perform(patch("/api/auth/admin/approve/1"))
                                        .andDo(print())
                                        .andExpect(status().isForbidden())
                                        .andExpect(jsonPath("$.code").value("C005"));
                }
        }

        @Nested
        @DisplayName("DELETE /api/auth/withdraw - 회원 탈퇴")
        class WithdrawTest {

                @Test
                @DisplayName("성공 - 회원 탈퇴에 성공하면 200 OK를 반환한다")
                void withdrawSuccess() throws Exception {
                        // Given
                        willDoNothing().given(memberService).withdraw();

                        // When & Then
                        mockMvc.perform(delete("/api/auth/withdraw"))
                                        .andDo(print())
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true));
                }
        }

        @Nested
        @DisplayName("DELETE /api/auth/admin/users/{userId} - 관리자 강제 탈퇴")
        class ForceWithdrawTest {

                @Test
                @DisplayName("성공 - 관리자가 회원을 강제 탈퇴시키면 200 OK를 반환한다")
                void forceWithdrawSuccess() throws Exception {
                        // Given
                        willDoNothing().given(memberService).withdrawByAdmin(anyLong());

                        // When & Then
                        mockMvc.perform(delete("/api/auth/admin/users/1"))
                                        .andDo(print())
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true));
                }

                @Test
                @DisplayName("실패 - 존재하지 않는 사용자면 404 Not Found를 반환한다")
                void forceWithdrawFail_UserNotFound() throws Exception {
                        // Given
                        willThrow(new BusinessException(ErrorCode.MEMBER_NOT_FOUND))
                                        .given(memberService).withdrawByAdmin(anyLong());

                        // When & Then
                        mockMvc.perform(delete("/api/auth/admin/users/999"))
                                        .andDo(print())
                                        .andExpect(status().isNotFound())
                                        .andExpect(jsonPath("$.code").value("M003"));
                }
        }

        @Nested
        @DisplayName("GET /api/auth/check-email - 이메일 중복 체크")
        class CheckEmailTest {

                @Test
                @DisplayName("성공 - 사용 가능한 이메일이면 200 OK를 반환한다")
                void checkEmailSuccess() throws Exception {
                        // Given - 중복 아님 (예외 없이 통과)
                        willDoNothing().given(memberService).validateDuplicateEmail(anyString());

                        // When & Then
                        mockMvc.perform(get("/api/auth/check-email")
                                        .param("email", "available@example.com"))
                                        .andDo(print())
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true));
                }

                @Test
                @DisplayName("실패 - 이메일이 중복되면 409 Conflict를 반환한다")
                void checkEmailFail_Duplicate() throws Exception {
                        // Given - 중복 시 예외 발생
                        willThrow(new BusinessException(ErrorCode.DUPLICATE_EMAIL))
                                        .given(memberService).validateDuplicateEmail(anyString());

                        // When & Then
                        mockMvc.perform(get("/api/auth/check-email")
                                        .param("email", "duplicate@example.com"))
                                        .andDo(print())
                                        .andExpect(status().isConflict());
                }
        }

        @Nested
        @DisplayName("GET /api/auth/check-nickname - 닉네임 중복 체크")
        class CheckNicknameTest {

                @Test
                @DisplayName("성공 - 사용 가능한 닉네임이면 200 OK를 반환한다")
                void checkNicknameSuccess() throws Exception {
                        // Given - 중복 아님 (예외 없이 통과)
                        willDoNothing().given(memberService).validateDuplicateNicknm(anyString());

                        // When & Then
                        mockMvc.perform(get("/api/auth/check-nickname")
                                        .param("nickname", "사용가능닉네임"))
                                        .andDo(print())
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true));
                }

                @Test
                @DisplayName("실패 - 닉네임이 중복되면 409 Conflict를 반환한다")
                void checkNicknameFail_Duplicate() throws Exception {
                        // Given - 중복 시 예외 발생
                        willThrow(new BusinessException(ErrorCode.DUPLICATE_NICKNAME))
                                        .given(memberService).validateDuplicateNicknm(anyString());

                        // When & Then
                        mockMvc.perform(get("/api/auth/check-nickname")
                                        .param("nickname", "중복닉네임"))
                                        .andDo(print())
                                        .andExpect(status().isConflict());
                }
        }
}
