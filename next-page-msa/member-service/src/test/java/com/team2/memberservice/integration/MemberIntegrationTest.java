package com.team2.memberservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team2.memberservice.command.member.dto.request.SignUpRequest;
import com.team2.memberservice.auth.dto.LoginRequest;
import com.team2.memberservice.command.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // 테스트 후 DB 롤백
@DisplayName("Member Service 통합 테스트")
class MemberIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        // 테스트 격리를 위해 매 테스트 실행 전 DB 초기화 (Transactional로 자동 롤백되지만 안전장치)
        // memberRepository.deleteAll(); // @Transactional 사용시 불필요
    }

    @Test
    @DisplayName("회원가입 -> 로그인 -> 내 정보 조회 시나리오")
    void signupLoginAndGetProfile() throws Exception {
        // 1. 회원가입
        SignUpRequest signUpRequest = new SignUpRequest("integration@test.com", "password123", "통합테스터");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 2. 로그인
        LoginRequest loginRequest = new LoginRequest("integration@test.com", "password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(cookie().exists("refreshToken"));
    }

    @Test
    @DisplayName("중복 이메일 가입 실패 테스트")
    void signupFailDuplicateEmail() throws Exception {
        // 1. 첫 번째 유저 가입
        SignUpRequest user1 = new SignUpRequest("duplicate@test.com", "pw1", "user1");
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isOk());

        // 2. 같은 이메일로 두 번째 유저 가입 시도
        SignUpRequest user2 = new SignUpRequest("duplicate@test.com", "pw2", "user2");
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user2)))
                .andDo(print())
                .andExpect(status().isConflict()) // 409 Conflict 기대
                .andExpect(jsonPath("$.success").value(false));
    }
}
