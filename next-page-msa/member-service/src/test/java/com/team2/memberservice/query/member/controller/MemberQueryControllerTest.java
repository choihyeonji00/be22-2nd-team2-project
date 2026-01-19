package com.team2.memberservice.query.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team2.commonmodule.util.SecurityUtil;
import com.team2.memberservice.auth.controller.TestExceptionHandler;
import com.team2.memberservice.query.member.dto.response.MemberDto;
import com.team2.memberservice.query.member.service.MemberQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/**
 * MemberQueryController 단위 테스트
 *
 * MockMvcBuilders.standaloneSetup을 사용하여 컨트롤러 계층만 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MemberQueryController 단위 테스트")
class MemberQueryControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private MemberQueryService memberQueryService;

    @InjectMocks
    private MemberQueryController memberQueryController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(memberQueryController)
                .setControllerAdvice(new TestExceptionHandler.TestExceptionHandlerAdvice())
                .build();
    }

    @Nested
    @DisplayName("GET /api/members/me - 내 정보 조회")
    class GetMyInfoTest {

        @Test
        @DisplayName("성공 - 로그인한 사용자의 정보를 조회하면 200 OK를 반환한다")
        void getMyInfoSuccess() throws Exception {
            // Given
            MemberDto memberDto = MemberDto.builder()
                    .userEmail("test@example.com")
                    .userNicknm("테스터")
                    .userRole("USER")
                    .build();

            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserEmail).thenReturn("test@example.com");
                given(memberQueryService.getMyPage(anyString())).willReturn(memberDto);

                // When & Then
                mockMvc.perform(get("/api/members/me"))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.userEmail").value("test@example.com"))
                        .andExpect(jsonPath("$.data.userNicknm").value("테스터"));
            }
        }

        @Test
        @DisplayName("실패 - 로그인하지 않은 경우 401 Unauthorized를 반환한다 (SecurityUtil이 null 반환)")
        void getMyInfoFail_Unauthenticated() throws Exception {
            try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
                securityUtil.when(SecurityUtil::getCurrentUserEmail).thenReturn(null);

                // When & Then
                mockMvc.perform(get("/api/members/me"))
                        .andDo(print())
                        .andExpect(status().isUnauthorized())
                        .andExpect(jsonPath("$.code").value("A003"));
            }
        }
    }
}
