package com.team2.memberservice.auth.dto;

import lombok.*;

/**
 * 토큰 갱신 요청 DTO
 * 프론트엔드에서 Request Body로 refreshToken을 전송할 때 사용합니다.
 *
 * @author 김태형
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RefreshRequest {

    private String refreshToken;
}
