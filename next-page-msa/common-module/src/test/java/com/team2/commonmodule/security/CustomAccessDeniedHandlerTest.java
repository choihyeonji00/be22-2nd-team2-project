package com.team2.commonmodule.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team2.commonmodule.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.assertj.core.api.Assertions;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.Mockito;

@ExtendWith(MockitoExtension.class)
class CustomAccessDeniedHandlerTest {

    @InjectMocks
    private CustomAccessDeniedHandler handler;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("403 Forbidden 응답 정상 반환")
    void handle_Returns403Response() throws Exception {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AccessDeniedException exception = new AccessDeniedException("Access Denied");

        String expectedJson = "{\"success\":false,\"message\":\"접근 권한이 없습니다.\"}";
        given(objectMapper.writeValueAsString(any(ApiResponse.class))).willReturn(expectedJson);

        // When
        handler.handle(request, response, exception);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
        assertThat(response.getContentType()).contains(MediaType.APPLICATION_JSON_VALUE);
        assertThat(response.getCharacterEncoding()).isEqualTo(StandardCharsets.UTF_8.name());
        verify(objectMapper, times(1)).writeValueAsString(any(ApiResponse.class));
    }

    @Test
    @DisplayName("Content-Type: application/json 확인")
    void handle_SetsCorrectContentType() throws Exception {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AccessDeniedException exception = new AccessDeniedException("Access Denied");

        String expectedJson = "{}";
        given(objectMapper.writeValueAsString(any())).willReturn(expectedJson);

        // When
        handler.handle(request, response, exception);

        // Then
        assertThat(response.getContentType()).contains(MediaType.APPLICATION_JSON_VALUE);
        assertThat(response.getCharacterEncoding()).isEqualTo(StandardCharsets.UTF_8.name());
    }

    @Test
    @DisplayName("ErrorCode.ACCESS_DENIED 메시지 확인")
    void handle_UsesCorrectErrorCode() throws Exception {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AccessDeniedException exception = new AccessDeniedException("Access Denied");

        String expectedJson = "{\"success\":false,\"data\":null,\"message\":\"접근 권한이 없습니다.\"}";
        given(objectMapper.writeValueAsString(any(ApiResponse.class))).willReturn(expectedJson);

        // When
        handler.handle(request, response, exception);

        // Then
        assertThat(response.getContentAsString()).isEqualTo(expectedJson);
        verify(objectMapper, times(1)).writeValueAsString(any(ApiResponse.class));
    }
}
