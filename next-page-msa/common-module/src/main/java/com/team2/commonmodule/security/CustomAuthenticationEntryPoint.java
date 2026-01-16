package com.team2.commonmodule.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team2.commonmodule.error.ErrorCode;
import com.team2.commonmodule.response.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 인증되지 않은 사용자(401)가 보호된 리소스에 접근할 때 호출됩니다.
 * Spring Security의 기본 401 응답 대신 커스텀 ApiResponse JSON을 내려줍니다.
 *
 * @author 정진호
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        log.error("Unauthorized Access: {}", authException.getMessage());

        // 로깅 및 응답 설정
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        // ErrorCode.UNAUTHENTICATED ("로그인이 필요합니다.") 등을 사용하거나 에러 상황에 맞춰 로직 분기 가능
        // 여기서는 기본적으로 UNAUTHENTICATED 사용
        ApiResponse<Void> apiResponse = ApiResponse.error(ErrorCode.UNAUTHENTICATED);

        // JSON 변환 및 쓰기
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
