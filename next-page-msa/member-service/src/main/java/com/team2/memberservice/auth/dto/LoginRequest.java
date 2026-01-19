package com.team2.memberservice.auth.dto;

import jakarta.validation.constraints.*;

import lombok.*;

/**
 * 로그인 요청 DTO
 *
 * @author 김태형
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class LoginRequest {

  @NotBlank(message = "이메일은 필수 입력값입니다.")
  @Email(message = "이메일 형식이 올바르지 않습니다.")
  private String userEmail;

  @NotBlank(message = "비밀번호는 필수 입력값입니다.")
  @Size(min = 8, max = 20, message = "비밀번호는 8~20자 이내여야 합니다.")
  private String userPw;
}