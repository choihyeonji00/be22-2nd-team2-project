package com.team2.storyservice.command.book.controller;

import com.team2.commonmodule.error.BusinessException;
import com.team2.commonmodule.error.ErrorCode;
import com.team2.commonmodule.response.ApiResponse;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

/**
 * Story-Service 테스트용 예외 핸들러 설정
 */
@TestConfiguration
public class TestExceptionHandler {

    @ControllerAdvice
    public static class TestExceptionHandlerAdvice {

        @ExceptionHandler(BusinessException.class)
        @ResponseBody
        public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
            ErrorCode errorCode = e.getErrorCode();
            ApiResponse<Void> response = ApiResponse.error(errorCode);
            return new ResponseEntity<>(response, errorCode.getStatus());
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        @ResponseBody
        public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
                MethodArgumentNotValidException e) {
            BindingResult bindingResult = e.getBindingResult();
            StringBuilder sb = new StringBuilder();
            FieldError firstError = bindingResult.getFieldError();
            if (firstError != null) {
                sb.append(firstError.getDefaultMessage());
            }
            ApiResponse<Void> response = ApiResponse.error(ErrorCode.INVALID_INPUT_VALUE.getCode(), sb.toString());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @Bean
    public TestExceptionHandlerAdvice testExceptionHandlerAdvice() {
        return new TestExceptionHandlerAdvice();
    }
}
