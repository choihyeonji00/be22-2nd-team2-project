package com.team2.commonmodule.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.assertj.core.api.Assertions;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.Mockito;

/**
 * JwtToHeaderFilter 단위 테스트
 * JWT 토큰을 파싱하여 Gateway 헤더로 변환하는 필터 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtToHeaderFilter 단위 테스트")
class JwtToHeaderFilterTest {

    private JwtToHeaderFilter jwtToHeaderFilter;

    @Mock
    private FilterChain filterChain;

    private static final String SECRET_KEY = "test-secret-key-for-jwt-token-testing-must-be-256-bits-long";
    private SecretKey key;

    @BeforeEach
    void setUp() {
        jwtToHeaderFilter = new JwtToHeaderFilter();
        ReflectionTestUtils.setField(jwtToHeaderFilter, "secretKey", SECRET_KEY);
        key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        ReflectionTestUtils.setField(jwtToHeaderFilter, "key", key);
    }

    private String createValidToken(Long userId, String email, String nickname, String role) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("nickname", nickname)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000)) // 1시간
                .signWith(key)
                .compact();
    }

    @Nested
    @DisplayName("JWT 토큰 파싱 및 헤더 변환")
    class JwtParsingTest {

        @Test
        @DisplayName("성공 - 유효한 JWT 토큰이 있으면 Gateway 헤더로 변환한다")
        void validJwtToken_ConvertsToHeaders() throws ServletException, IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();

            String token = createValidToken(1L, "test@example.com", "테스터", "USER");
            request.addHeader("Authorization", "Bearer " + token);

            // When
            jwtToHeaderFilter.doFilterInternal(request, response, filterChain);

            // Then
            then(filterChain).should(times(1)).doFilter(any(), any());
        }

        @Test
        @DisplayName("성공 - Gateway 헤더가 이미 있으면 JWT 파싱을 건너뛴다")
        void existingGatewayHeader_SkipsJwtParsing() throws ServletException, IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();

            request.addHeader("X-User-Id", "1");
            request.addHeader("Authorization", "Bearer some.token.value");

            // When
            jwtToHeaderFilter.doFilterInternal(request, response, filterChain);

            // Then
            then(filterChain).should(times(1)).doFilter(request, response);
        }

        @Test
        @DisplayName("성공 - Authorization 헤더가 없으면 그대로 진행한다")
        void noAuthorizationHeader_ContinuesWithoutParsing() throws ServletException, IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();

            // When
            jwtToHeaderFilter.doFilterInternal(request, response, filterChain);

            // Then
            then(filterChain).should(times(1)).doFilter(request, response);
        }

        @Test
        @DisplayName("성공 - Bearer 접두사가 없는 토큰은 무시하고 진행한다")
        void noBearerPrefix_ContinuesWithoutParsing() throws ServletException, IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();

            request.addHeader("Authorization", "some.token.value");

            // When
            jwtToHeaderFilter.doFilterInternal(request, response, filterChain);

            // Then
            then(filterChain).should(times(1)).doFilter(request, response);
        }

        @Test
        @DisplayName("실패 - 유효하지 않은 JWT 토큰은 파싱 실패하고 진행한다")
        void invalidJwtToken_ContinuesWithWarning() throws ServletException, IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();

            request.addHeader("Authorization", "Bearer invalid.jwt.token");

            // When
            jwtToHeaderFilter.doFilterInternal(request, response, filterChain);

            // Then
            then(filterChain).should(times(1)).doFilter(request, response);
        }

        @Test
        @DisplayName("실패 - 만료된 JWT 토큰은 파싱 실패하고 진행한다")
        void expiredJwtToken_ContinuesWithWarning() throws ServletException, IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();

            String expiredToken = Jwts.builder()
                    .subject("1")
                    .claim("email", "test@example.com")
                    .issuedAt(new Date(System.currentTimeMillis() - 7200000)) // 2시간 전
                    .expiration(new Date(System.currentTimeMillis() - 3600000)) // 1시간 전 만료
                    .signWith(key)
                    .compact();

            request.addHeader("Authorization", "Bearer " + expiredToken);

            // When
            jwtToHeaderFilter.doFilterInternal(request, response, filterChain);

            // Then
            then(filterChain).should(times(1)).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("필터 제외 경로")
    class ShouldNotFilterTest {

        @Test
        @DisplayName("Swagger UI 경로는 필터를 적용하지 않는다")
        void swaggerUiPath_ShouldNotFilter() {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/swagger-ui/index.html");

            // When
            boolean result = jwtToHeaderFilter.shouldNotFilter(request);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("API 문서 경로는 필터를 적용하지 않는다")
        void apiDocsPath_ShouldNotFilter() {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/v3/api-docs");

            // When
            boolean result = jwtToHeaderFilter.shouldNotFilter(request);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Actuator 경로는 필터를 적용하지 않는다")
        void actuatorPath_ShouldNotFilter() {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/actuator/health");

            // When
            boolean result = jwtToHeaderFilter.shouldNotFilter(request);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("일반 API 경로는 필터를 적용한다")
        void normalApiPath_ShouldFilter() {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/api/books");

            // When
            boolean result = jwtToHeaderFilter.shouldNotFilter(request);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("JWT Secret Key 설정")
    class SecretKeyConfigTest {

        @Test
        @DisplayName("Secret Key가 설정되지 않으면 JWT 파싱을 건너뛴다")
        void noSecretKey_SkipsJwtParsing() throws ServletException, IOException {
            // Given
            JwtToHeaderFilter filterWithoutKey = new JwtToHeaderFilter();
            ReflectionTestUtils.setField(filterWithoutKey, "secretKey", "");
            ReflectionTestUtils.setField(filterWithoutKey, "key", null);

            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.addHeader("Authorization", "Bearer some.token.value");

            // When
            filterWithoutKey.doFilterInternal(request, response, filterChain);

            // Then
            then(filterChain).should(times(1)).doFilter(request, response);
        }
    }
}
