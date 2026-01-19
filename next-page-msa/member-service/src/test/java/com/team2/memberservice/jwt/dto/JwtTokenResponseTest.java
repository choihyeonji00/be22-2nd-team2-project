package com.team2.memberservice.jwt.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtTokenResponse DTO 테스트")
class JwtTokenResponseTest {

    @Test
    @DisplayName("Builder를 사용하여 객체를 생성한다")
    void createUsingBuilder() {
        // Given
        JwtTokenResponse.UserInfo userInfo = JwtTokenResponse.UserInfo.builder()
                .userId(1L)
                .email("test@example.com")
                .nickname("tester")
                .role("ROLE_USER")
                .build();

        // When
        JwtTokenResponse response = JwtTokenResponse.builder()
                .grantType("Bearer")
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .accessTokenExpiresIn(3600L)
                .userInfo(userInfo)
                .build();

        // Then
        assertThat(response.getGrantType()).isEqualTo("Bearer");
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getAccessTokenExpiresIn()).isEqualTo(3600L);
        assertThat(response.getUserInfo()).isEqualTo(userInfo);
    }

    @Test
    @DisplayName("static factory method 'of'를 사용하여 객체를 생성한다")
    void createUsingStaticFactory() {
        // Given
        JwtTokenResponse.UserInfo userInfo = JwtTokenResponse.UserInfo.builder()
                .userId(1L)
                .email("test@example.com")
                .nickname("tester")
                .role("ROLE_USER")
                .build();

        // When
        JwtTokenResponse response = JwtTokenResponse.of(
                "access-token",
                "refresh-token",
                3600L,
                userInfo);

        // Then
        assertThat(response.getGrantType()).isEqualTo("Bearer");
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getAccessTokenExpiresIn()).isEqualTo(3600L);
        assertThat(response.getUserInfo()).isEqualTo(userInfo);
    }
}
