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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 인증은 되었으나, 권한이 부족한 사용자(403)가 리소스에 접근할 때 호출됩니다.
 * Spring Security의 기본 403 응답 대신 커스텀 ApiResponse JSON을 내려줍니다.
 *
 * @author 정진호
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {

        log.error("Access Denied: {}", accessDeniedException.getMessage());

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        // ErrorCode.ACCESS_DENIED ("접근 권한이 없습니다.")
        ApiResponse<Void> apiResponse = ApiResponse.error(ErrorCode.ACCESS_DENIED);

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
