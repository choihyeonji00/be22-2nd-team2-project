package com.team2.nextpage.common.exception;

import com.team2.nextpage.common.error.BusinessException;
import com.team2.nextpage.common.error.ErrorCode;
import com.team2.nextpage.common.response.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("MethodArgumentNotValidException 처리")
    void handleMethodArgumentNotValidException() {
        // given
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "유효하지 않은 값입니다");

        given(ex.getBindingResult()).willReturn(bindingResult);
        given(bindingResult.getFieldError()).willReturn(fieldError);

        // when
        ResponseEntity<ApiResponse<Void>> response = globalExceptionHandler
                .handleMethodArgumentNotValidException(ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("유효하지 않은 값입니다");
    }

    @Test
    @DisplayName("MethodArgumentNotValidException 처리 - FieldError 없는 경우")
    void handleMethodArgumentNotValidException_NoFieldError() {
        // given
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        given(ex.getBindingResult()).willReturn(bindingResult);
        given(bindingResult.getFieldError()).willReturn(null);

        // when
        ResponseEntity<ApiResponse<Void>> response = globalExceptionHandler
                .handleMethodArgumentNotValidException(ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("HttpRequestMethodNotSupportedException 처리")
    void handleHttpRequestMethodNotSupportedException() {
        // given
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("POST");

        // when
        ResponseEntity<ApiResponse<Void>> response = globalExceptionHandler
                .handleHttpRequestMethodNotSupportedException(ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("AccessDeniedException 처리")
    void handleAccessDeniedException() {
        // given
        AccessDeniedException ex = new AccessDeniedException("접근 권한이 없습니다");

        // when
        ResponseEntity<ApiResponse<Void>> response = globalExceptionHandler
                .handleAccessDeniedException(ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("BadCredentialsException 처리")
    void handleBadCredentialsException() {
        // given
        BadCredentialsException ex = new BadCredentialsException("비밀번호가 틀렸습니다");

        // when
        ResponseEntity<ApiResponse<Void>> response = globalExceptionHandler
                .handleBadCredentialsException(ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("이메일 또는 비밀번호가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("BusinessException 처리 - NOT_FOUND")
    void handleBusinessException_NotFound() {
        // given
        BusinessException ex = new BusinessException(ErrorCode.MEMBER_NOT_FOUND);

        // when
        ResponseEntity<ApiResponse<Void>> response = globalExceptionHandler.handleBusinessException(ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("BusinessException 처리 - CONFLICT")
    void handleBusinessException_Conflict() {
        // given
        BusinessException ex = new BusinessException(ErrorCode.DUPLICATE_EMAIL);

        // when
        ResponseEntity<ApiResponse<Void>> response = globalExceptionHandler.handleBusinessException(ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("BusinessException 처리 - BAD_REQUEST")
    void handleBusinessException_BadRequest() {
        // given
        BusinessException ex = new BusinessException(ErrorCode.ALREADY_COMPLETED);

        // when
        ResponseEntity<ApiResponse<Void>> response = globalExceptionHandler.handleBusinessException(ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("BusinessException 처리 - FORBIDDEN")
    void handleBusinessException_Forbidden() {
        // given
        BusinessException ex = new BusinessException(ErrorCode.ACCESS_DENIED);

        // when
        ResponseEntity<ApiResponse<Void>> response = globalExceptionHandler.handleBusinessException(ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @DisplayName("일반 Exception 처리")
    void handleException() {
        // given
        Exception ex = new RuntimeException("알 수 없는 오류");

        // when
        ResponseEntity<ApiResponse<Void>> response = globalExceptionHandler.handleException(ex);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
    }
}
