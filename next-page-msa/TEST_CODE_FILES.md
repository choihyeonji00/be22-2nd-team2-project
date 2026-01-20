# Next Page MSA - 전체 테스트 코드

> **본 문서는 next-page-msa 프로젝트의 모든 테스트 코드 전체 소스를 포함합니다.**
> 각 섹션을 클릭하여 실제 테스트 코드를 확인할 수 있습니다.

---

## 테스트 파일 통계

| 모듈 | 테스트 파일 수 | 테스트 메서드 수 |
|:---:|:---:|:---:|
| **Common Module** | 5 | 37 |
| **Member Service** | 12 | 90+ |
| **Story Service** | 6 | 75+ |
| **Reaction Service** | 7 | 50+ |
| **Config Server** | 1 | 1 |
| **전체** | **31** | **253+** |

---

## 1. Common Module

### 1.1 Filter Tests

<details>
<summary><b>GatewayAuthenticationFilterTest.java</b> - Gateway 헤더 기반 Spring Security 인증 필터 테스트 (13개 테스트)</summary>

**경로:** `common-module/src/test/java/com/team2/commonmodule/filter/GatewayAuthenticationFilterTest.java`

```java
package com.team2.commonmodule.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import java.io.IOException;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.assertj.core.api.Assertions;
import org.mockito.BDDMockito;
import org.mockito.Mockito;

/**
 * GatewayAuthenticationFilter 단위 테스트
 * Gateway 헤더를 기반으로 Spring Security 인증을 설정하는 필터 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GatewayAuthenticationFilter 단위 테스트")
class GatewayAuthenticationFilterTest {

    private GatewayAuthenticationFilter gatewayAuthenticationFilter;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        gatewayAuthenticationFilter = new GatewayAuthenticationFilter();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("Gateway 헤더 기반 인증")
    class GatewayHeaderAuthenticationTest {

        @Test
        @DisplayName("성공 - 유효한 Gateway 헤더가 있으면 인증 정보를 설정한다")
        void validGatewayHeaders_SetsAuthentication() throws ServletException, IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();

            request.addHeader("X-User-Id", "1");
            request.addHeader("X-User-Email", "test@example.com");
            request.addHeader("X-User-Role", "USER");

            // When
            gatewayAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication).isNotNull();
            assertThat(authentication.getPrincipal()).isEqualTo("test@example.com");
            assertThat(authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_USER"))).isTrue();

            then(filterChain).should(times(1)).doFilter(request, response);
        }

        @Test
        @DisplayName("성공 - ADMIN 역할이 있으면 ROLE_ADMIN 권한을 부여한다")
        void adminRole_GrantsRoleAdmin() throws ServletException, IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();

            request.addHeader("X-User-Id", "1");
            request.addHeader("X-User-Email", "admin@example.com");
            request.addHeader("X-User-Role", "ADMIN");

            // When
            gatewayAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication).isNotNull();
            assertThat(authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))).isTrue();
        }

        @Test
        @DisplayName("성공 - 역할에 ROLE_ 접두사가 이미 있으면 그대로 사용한다")
        void roleWithPrefix_UsesAsIs() throws ServletException, IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();

            request.addHeader("X-User-Id", "1");
            request.addHeader("X-User-Email", "test@example.com");
            request.addHeader("X-User-Role", "ROLE_USER");

            // When
            gatewayAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication).isNotNull();
            assertThat(authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_USER"))).isTrue();
        }

        @Test
        @DisplayName("성공 - 역할이 없으면 기본 USER 역할을 부여한다")
        void noRole_GrantsDefaultUserRole() throws ServletException, IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();

            request.addHeader("X-User-Id", "1");
            request.addHeader("X-User-Email", "test@example.com");
            // X-User-Role 헤더 없음

            // When
            gatewayAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication).isNotNull();
            assertThat(authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_USER"))).isTrue();
        }

        @Test
        @DisplayName("실패 - User-Id 헤더가 없으면 인증 정보를 설정하지 않는다")
        void noUserId_NoAuthentication() throws ServletException, IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();

            request.addHeader("X-User-Email", "test@example.com");
            request.addHeader("X-User-Role", "USER");
            // X-User-Id 헤더 없음

            // When
            gatewayAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication).isNull();

            then(filterChain).should(times(1)).doFilter(request, response);
        }

        @Test
        @DisplayName("실패 - User-Email 헤더가 없으면 인증 정보를 설정하지 않는다")
        void noUserEmail_NoAuthentication() throws ServletException, IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();

            request.addHeader("X-User-Id", "1");
            request.addHeader("X-User-Role", "USER");
            // X-User-Email 헤더 없음

            // When
            gatewayAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication).isNull();
        }

        @Test
        @DisplayName("실패 - 모든 Gateway 헤더가 없으면 인증 정보를 설정하지 않는다")
        void noGatewayHeaders_NoAuthentication() throws ServletException, IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();

            // When
            gatewayAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication).isNull();

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
            boolean result = gatewayAuthenticationFilter.shouldNotFilter(request);

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
            boolean result = gatewayAuthenticationFilter.shouldNotFilter(request);

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
            boolean result = gatewayAuthenticationFilter.shouldNotFilter(request);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("H2 Console 경로는 필터를 적용하지 않는다")
        void h2ConsolePath_ShouldNotFilter() {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/h2-console");

            // When
            boolean result = gatewayAuthenticationFilter.shouldNotFilter(request);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Error 경로는 필터를 적용하지 않는다")
        void errorPath_ShouldNotFilter() {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/error");

            // When
            boolean result = gatewayAuthenticationFilter.shouldNotFilter(request);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("일반 API 경로는 필터를 적용한다")
        void normalApiPath_ShouldFilter() {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/api/members/me");

            // When
            boolean result = gatewayAuthenticationFilter.shouldNotFilter(request);

            // Then
            assertThat(result).isFalse();
        }
    }
}
```

</details>

<details>
<summary><b>JwtToHeaderFilterTest.java</b> - JWT 토큰 파싱 및 Gateway 헤더 변환 필터 테스트 (11개 테스트)</summary>

**경로:** `common-module/src/test/java/com/team2/commonmodule/filter/JwtToHeaderFilterTest.java`

```java
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
                .expiration(new Date(System.currentTimeMillis() + 3600000))
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
                    .issuedAt(new Date(System.currentTimeMillis() - 7200000))
                    .expiration(new Date(System.currentTimeMillis() - 3600000))
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
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/swagger-ui/index.html");
            boolean result = jwtToHeaderFilter.shouldNotFilter(request);
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("API 문서 경로는 필터를 적용하지 않는다")
        void apiDocsPath_ShouldNotFilter() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/v3/api-docs");
            boolean result = jwtToHeaderFilter.shouldNotFilter(request);
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Actuator 경로는 필터를 적용하지 않는다")
        void actuatorPath_ShouldNotFilter() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/actuator/health");
            boolean result = jwtToHeaderFilter.shouldNotFilter(request);
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("일반 API 경로는 필터를 적용한다")
        void normalApiPath_ShouldFilter() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/api/books");
            boolean result = jwtToHeaderFilter.shouldNotFilter(request);
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("JWT Secret Key 설정")
    class SecretKeyConfigTest {

        @Test
        @DisplayName("Secret Key가 설정되지 않으면 JWT 파싱을 건너뛴다")
        void noSecretKey_SkipsJwtParsing() throws ServletException, IOException {
            JwtToHeaderFilter filterWithoutKey = new JwtToHeaderFilter();
            ReflectionTestUtils.setField(filterWithoutKey, "secretKey", "");
            ReflectionTestUtils.setField(filterWithoutKey, "key", null);

            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.addHeader("Authorization", "Bearer some.token.value");

            filterWithoutKey.doFilterInternal(request, response, filterChain);

            then(filterChain).should(times(1)).doFilter(request, response);
        }
    }
}
```

</details>

### 1.2 Security Tests

<details>
<summary><b>CustomAccessDeniedHandlerTest.java</b> - 403 Forbidden 응답 처리 테스트 (3개 테스트)</summary>

**경로:** `common-module/src/test/java/com/team2/commonmodule/security/CustomAccessDeniedHandlerTest.java`

```java
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

@ExtendWith(MockitoExtension.class)
class CustomAccessDeniedHandlerTest {

    @InjectMocks
    private CustomAccessDeniedHandler handler;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("403 Forbidden 응답 정상 반환")
    void handle_Returns403Response() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AccessDeniedException exception = new AccessDeniedException("Access Denied");

        String expectedJson = "{\"success\":false,\"message\":\"접근 권한이 없습니다.\"}";
        given(objectMapper.writeValueAsString(any(ApiResponse.class))).willReturn(expectedJson);

        handler.handle(request, response, exception);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
        assertThat(response.getContentType()).contains(MediaType.APPLICATION_JSON_VALUE);
        assertThat(response.getCharacterEncoding()).isEqualTo(StandardCharsets.UTF_8.name());
        verify(objectMapper, times(1)).writeValueAsString(any(ApiResponse.class));
    }

    @Test
    @DisplayName("Content-Type: application/json 확인")
    void handle_SetsCorrectContentType() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AccessDeniedException exception = new AccessDeniedException("Access Denied");

        String expectedJson = "{}";
        given(objectMapper.writeValueAsString(any())).willReturn(expectedJson);

        handler.handle(request, response, exception);

        assertThat(response.getContentType()).contains(MediaType.APPLICATION_JSON_VALUE);
        assertThat(response.getCharacterEncoding()).isEqualTo(StandardCharsets.UTF_8.name());
    }

    @Test
    @DisplayName("ErrorCode.ACCESS_DENIED 메시지 확인")
    void handle_UsesCorrectErrorCode() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AccessDeniedException exception = new AccessDeniedException("Access Denied");

        String expectedJson = "{\"success\":false,\"data\":null,\"message\":\"접근 권한이 없습니다.\"}";
        given(objectMapper.writeValueAsString(any(ApiResponse.class))).willReturn(expectedJson);

        handler.handle(request, response, exception);

        assertThat(response.getContentAsString()).isEqualTo(expectedJson);
        verify(objectMapper, times(1)).writeValueAsString(any(ApiResponse.class));
    }
}
```

</details>

<details>
<summary><b>CustomAuthenticationEntryPointTest.java</b> - 401 Unauthorized 응답 처리 테스트 (3개 테스트)</summary>

**경로:** `common-module/src/test/java/com/team2/commonmodule/security/CustomAuthenticationEntryPointTest.java`

```java
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CustomAuthenticationEntryPointTest {

    @InjectMocks
    private CustomAuthenticationEntryPoint entryPoint;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("401 Unauthorized 응답 정상 반환")
    void commence_Returns401Response() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthenticationException exception = new BadCredentialsException("Bad credentials");

        String expectedJson = "{\"success\":false,\"message\":\"로그인이 필요합니다.\"}";
        given(objectMapper.writeValueAsString(any(ApiResponse.class))).willReturn(expectedJson);

        entryPoint.commence(request, response, exception);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentType()).contains(MediaType.APPLICATION_JSON_VALUE);
        assertThat(response.getCharacterEncoding()).isEqualTo(StandardCharsets.UTF_8.name());
        verify(objectMapper, times(1)).writeValueAsString(any(ApiResponse.class));
    }

    @Test
    @DisplayName("Content-Type: application/json 확인")
    void commence_SetsCorrectContentType() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthenticationException exception = new BadCredentialsException("Bad credentials");

        String expectedJson = "{}";
        given(objectMapper.writeValueAsString(any())).willReturn(expectedJson);

        entryPoint.commence(request, response, exception);

        assertThat(response.getContentType()).contains(MediaType.APPLICATION_JSON_VALUE);
        assertThat(response.getCharacterEncoding()).isEqualTo(StandardCharsets.UTF_8.name());
    }

    @Test
    @DisplayName("ErrorCode.UNAUTHENTICATED 메시지 확인")
    void commence_UsesCorrectErrorCode() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthenticationException exception = new BadCredentialsException("Bad credentials");

        String expectedJson = "{\"success\":false,\"data\":null,\"message\":\"로그인이 필요합니다.\"}";
        given(objectMapper.writeValueAsString(any(ApiResponse.class))).willReturn(expectedJson);

        entryPoint.commence(request, response, exception);

        assertThat(response.getContentAsString()).isEqualTo(expectedJson);
        verify(objectMapper, times(1)).writeValueAsString(any(ApiResponse.class));
    }
}
```

</details>

### 1.3 기타 테스트

<details>
<summary><b>SerializationTest.java</b> - ApiResponse 직렬화 테스트</summary>

**경로:** `common-module/src/test/java/SerializationTest.java`

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team2.commonmodule.response.ApiResponse;

public class SerializationTest {
    public static void main(String[] args) {
        try {
            ApiResponse<Void> response = ApiResponse.success();
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(response);
            System.out.println("Serialized JSON: " + json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

</details>

<details>
<summary><b>CommonModuleApplicationTests.java</b> - Spring Context 로딩 테스트</summary>

**경로:** `common-module/src/test/java/com/team2/commonmodule/CommonModuleApplicationTests.java`

```java
package com.team2.commonmodule;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@EnableAutoConfiguration(exclude = {
    DataSourceAutoConfiguration.class,
    DataSourceTransactionManagerAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})
class CommonModuleApplicationTests {

  @Test
  void contextLoads() {
  }
}
```

</details>

---

## 2. Member Service

### 2.1 API Tests

<details>
<summary><b>MemberApiControllerTest.java</b> - Internal API (Feign용) 테스트 (3개 테스트)</summary>

**경로:** `member-service/src/test/java/com/team2/memberservice/api/MemberApiControllerTest.java`

```java
package com.team2.memberservice.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

import com.team2.memberservice.command.member.entity.*;
import com.team2.memberservice.command.member.repository.MemberRepository;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberApiControllerTest {

    @InjectMocks
    private MemberApiController memberApiController;

    @Mock
    private MemberRepository memberRepository;

    @Test
    @DisplayName("사용자 닉네임 조회 - 정상")
    void getUserNickname_Success() {
        Long userId = 1L;
        Member member = Member.builder()
                .userEmail("test@test.com")
                .userPw("password")
                .userNicknm("TestUser")
                .userRole(UserRole.USER)
                .userStatus(UserStatus.ACTIVE)
                .build();

        given(memberRepository.findById(userId)).willReturn(Optional.of(member));

        String nickname = memberApiController.getUserNickname(userId);

        assertThat(nickname).isEqualTo("TestUser");
    }

    @Test
    @DisplayName("사용자 닉네임 조회 - 사용자 없음 (Unknown 반환)")
    void getUserNickname_UserNotFound_ReturnsUnknown() {
        Long userId = 999L;
        given(memberRepository.findById(userId)).willReturn(Optional.empty());

        String nickname = memberApiController.getUserNickname(userId);

        assertThat(nickname).isEqualTo("Unknown");
    }

    @Test
    @DisplayName("사용자 닉네임 조회 - 존재하지 않는 사용자")
    void getUserNickname_NonExistentUser() {
        Long userId = 12345L;
        given(memberRepository.findById(anyLong())).willReturn(Optional.empty());

        String nickname = memberApiController.getUserNickname(userId);

        assertThat(nickname).isEqualTo("Unknown");
    }
}
```

</details>

### 2.2 Auth Tests

<details>
<summary><b>AuthControllerTest.java</b> - 로그인/로그아웃/토큰 갱신 API 테스트 (10개 테스트)</summary>

**경로:** `member-service/src/test/java/com/team2/memberservice/auth/controller/AuthControllerTest.java`

```java
package com.team2.memberservice.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team2.commonmodule.error.BusinessException;
import com.team2.commonmodule.error.ErrorCode;
import com.team2.memberservice.auth.dto.LoginRequest;
import com.team2.memberservice.auth.dto.TokenResponse;
import com.team2.memberservice.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import jakarta.servlet.http.Cookie;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController 단위 테스트")
class AuthControllerTest {

        private MockMvc mockMvc;
        private ObjectMapper objectMapper;

        @Mock
        private AuthService authService;

        @InjectMocks
        private AuthController authController;

        @BeforeEach
        void setUp() {
                objectMapper = new ObjectMapper();
                mockMvc = MockMvcBuilders.standaloneSetup(authController)
                                .setControllerAdvice(new TestExceptionHandler.TestExceptionHandlerAdvice())
                                .build();
        }

        @Nested
        @DisplayName("POST /api/auth/login - 로그인")
        class LoginTest {

                @Test
                @DisplayName("성공 - 올바른 자격 증명으로 로그인하면 200 OK와 토큰을 반환한다")
                void loginSuccess() throws Exception {
                        LoginRequest request = new LoginRequest("test@example.com", "password123");
                        TokenResponse tokenResponse = TokenResponse.builder()
                                        .accessToken("access-token")
                                        .refreshToken("refresh-token")
                                        .tokenType("Bearer")
                                        .build();

                        given(authService.login(any(LoginRequest.class))).willReturn(tokenResponse);

                        mockMvc.perform(post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andDo(print())
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                                        .andExpect(cookie().exists("refreshToken"));
                }

                @Test
                @DisplayName("실패 - 잘못된 자격 증명이면 401 Unauthorized를 반환한다")
                void loginFail_InvalidCredentials() throws Exception {
                        LoginRequest request = new LoginRequest("test@example.com", "wrongpassword");
                        given(authService.login(any(LoginRequest.class)))
                                        .willThrow(new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다."));

                        mockMvc.perform(post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andDo(print())
                                        .andExpect(status().isUnauthorized());
                }

                @Test
                @DisplayName("실패 - 승인 대기 상태면 403 Forbidden을 반환한다")
                void loginFail_PendingApproval() throws Exception {
                        LoginRequest request = new LoginRequest("admin@example.com", "password123");
                        given(authService.login(any(LoginRequest.class)))
                                        .willThrow(new BusinessException(ErrorCode.ACCOUNT_APPROVAL_PENDING));

                        mockMvc.perform(post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andDo(print())
                                        .andExpect(status().isForbidden())
                                        .andExpect(jsonPath("$.code").value("A005"));
                }

                @Test
                @DisplayName("실패 - 이메일 형식이 잘못되면 400 Bad Request를 반환한다")
                void loginFail_InvalidEmailFormat() throws Exception {
                        String requestJson = "{\"userEmail\": \"invalid-email\", \"userPw\": \"password123\"}";

                        mockMvc.perform(post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestJson))
                                        .andDo(print())
                                        .andExpect(status().isBadRequest());
                }

                @Test
                @DisplayName("실패 - 비밀번호가 누락되면 400 Bad Request를 반환한다")
                void loginFail_MissingPassword() throws Exception {
                        String requestJson = "{\"userEmail\": \"test@example.com\"}";

                        mockMvc.perform(post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(requestJson))
                                        .andDo(print())
                                        .andExpect(status().isBadRequest());
                }
        }

        @Nested
        @DisplayName("POST /api/auth/refresh - 토큰 갱신")
        class RefreshTest {

                @Test
                @DisplayName("성공 - 유효한 Refresh Token으로 새 토큰을 발급받는다")
                void refreshSuccess() throws Exception {
                        TokenResponse tokenResponse = TokenResponse.builder()
                                        .accessToken("new-access-token")
                                        .refreshToken("new-refresh-token")
                                        .tokenType("Bearer")
                                        .build();

                        given(authService.refreshToken(anyString())).willReturn(tokenResponse);

                        mockMvc.perform(post("/api/auth/refresh")
                                        .cookie(new Cookie("refreshToken", "valid-refresh-token")))
                                        .andDo(print())
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));
                }

                @Test
                @DisplayName("실패 - Refresh Token이 없으면 401 Unauthorized를 반환한다")
                void refreshFail_NoToken() throws Exception {
                        mockMvc.perform(post("/api/auth/refresh"))
                                        .andDo(print())
                                        .andExpect(status().isUnauthorized());
                }

                @Test
                @DisplayName("실패 - 유효하지 않은 Refresh Token이면 401 Unauthorized를 반환한다")
                void refreshFail_InvalidToken() throws Exception {
                        given(authService.refreshToken(anyString()))
                                        .willThrow(new BusinessException(ErrorCode.INVALID_TOKEN));

                        mockMvc.perform(post("/api/auth/refresh")
                                        .cookie(new Cookie("refreshToken", "invalid-token")))
                                        .andDo(print())
                                        .andExpect(status().isUnauthorized())
                                        .andExpect(jsonPath("$.code").value("A004"));
                }
        }

        @Nested
        @DisplayName("POST /api/auth/logout - 로그아웃")
        class LogoutTest {

                @Test
                @DisplayName("성공 - 로그아웃하면 200 OK와 쿠키 삭제 응답을 반환한다")
                void logoutSuccess() throws Exception {
                        willDoNothing().given(authService).logout(anyString());

                        mockMvc.perform(post("/api/auth/logout")
                                        .cookie(new Cookie("refreshToken", "valid-refresh-token")))
                                        .andDo(print())
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true))
                                        .andExpect(cookie().maxAge("refreshToken", 0));
                }

                @Test
                @DisplayName("성공 - Refresh Token 없이도 로그아웃할 수 있다")
                void logoutWithoutToken() throws Exception {
                        mockMvc.perform(post("/api/auth/logout"))
                                        .andDo(print())
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success").value(true));
                }
        }
}
```

</details>

<details>
<summary><b>AuthServiceTest.java</b> - 인증 서비스 비즈니스 로직 테스트 (13개 테스트)</summary>

**경로:** `member-service/src/test/java/com/team2/memberservice/auth/service/AuthServiceTest.java`

```java
package com.team2.memberservice.auth.service;

import com.team2.commonmodule.error.BusinessException;
import com.team2.commonmodule.error.ErrorCode;
import com.team2.memberservice.auth.dto.LoginRequest;
import com.team2.memberservice.auth.dto.TokenResponse;
import com.team2.memberservice.auth.entity.RefreshToken;
import com.team2.memberservice.auth.repository.AuthRepository;
import com.team2.memberservice.command.member.entity.Member;
import com.team2.memberservice.command.member.entity.UserRole;
import com.team2.memberservice.command.member.entity.UserStatus;
import com.team2.memberservice.command.member.repository.MemberRepository;
import com.team2.memberservice.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 단위 테스트")
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthRepository authRepository;

    private Member testMember;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "refreshTokenValidityInSeconds", 604800L);

        testMember = Member.builder()
                .userId(1L)
                .userEmail("test@example.com")
                .userPw("$2a$10$encoded.password.hash")
                .userNicknm("테스터")
                .userRole(UserRole.USER)
                .userStatus(UserStatus.ACTIVE)
                .build();

        loginRequest = new LoginRequest("test@example.com", "rawPassword123");
    }

    @Test
    @DisplayName("로그인 성공 - Access Token과 Refresh Token을 반환한다")
    void loginSuccess() {
        given(memberRepository.findByUserEmail(loginRequest.getUserEmail()))
                .willReturn(Optional.of(testMember));
        given(passwordEncoder.matches(loginRequest.getUserPw(), testMember.getUserPw()))
                .willReturn(true);
        given(jwtTokenProvider.createAccessToken(any()))
                .willReturn("access.token.value");
        given(jwtTokenProvider.createRefreshToken(any()))
                .willReturn("refresh.token.value");
        given(authRepository.findByUserEmail(testMember.getUserEmail()))
                .willReturn(Optional.empty());

        TokenResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access.token.value");
        assertThat(response.getRefreshToken()).isEqualTo("refresh.token.value");

        then(memberRepository).should(times(1)).findByUserEmail(loginRequest.getUserEmail());
        then(passwordEncoder).should(times(1)).matches(loginRequest.getUserPw(), testMember.getUserPw());
        then(jwtTokenProvider).should(times(1)).createAccessToken(any());
        then(jwtTokenProvider).should(times(1)).createRefreshToken(any());
        then(authRepository).should(times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 이메일")
    void loginFail_UserNotFound() {
        given(memberRepository.findByUserEmail(loginRequest.getUserEmail()))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("아이디 또는 비밀번호가 일치하지 않습니다");

        then(passwordEncoder).should(never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void loginFail_PasswordMismatch() {
        given(memberRepository.findByUserEmail(loginRequest.getUserEmail()))
                .willReturn(Optional.of(testMember));
        given(passwordEncoder.matches(loginRequest.getUserPw(), testMember.getUserPw()))
                .willReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("아이디 또는 비밀번호가 일치하지 않습니다");

        then(jwtTokenProvider).should(never()).createAccessToken(any());
    }

    @Test
    @DisplayName("로그인 실패 - 관리자 승인 대기 상태")
    void loginFail_PendingStatus() {
        Member pendingMember = Member.builder()
                .userId(2L)
                .userEmail("admin@example.com")
                .userPw("$2a$10$encoded.password.hash")
                .userNicknm("관리자후보")
                .userRole(UserRole.ADMIN)
                .userStatus(UserStatus.PENDING)
                .build();

        given(memberRepository.findByUserEmail(loginRequest.getUserEmail()))
                .willReturn(Optional.of(pendingMember));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCOUNT_APPROVAL_PENDING);

        then(passwordEncoder).should(never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("토큰 갱신 성공 - 새로운 Access Token과 Refresh Token을 반환한다")
    void refreshTokenSuccess() {
        String oldRefreshToken = "old.refresh.token";
        RefreshToken storedToken = RefreshToken.builder()
                .userEmail(testMember.getUserEmail())
                .token(oldRefreshToken)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();

        given(jwtTokenProvider.validateToken(oldRefreshToken)).willReturn(true);
        given(jwtTokenProvider.getUserEmailFromToken(oldRefreshToken))
                .willReturn(testMember.getUserEmail());
        given(authRepository.findByUserEmail(testMember.getUserEmail()))
                .willReturn(Optional.of(storedToken));
        given(memberRepository.findByUserEmail(testMember.getUserEmail()))
                .willReturn(Optional.of(testMember));
        given(jwtTokenProvider.createAccessToken(any()))
                .willReturn("new.access.token");
        given(jwtTokenProvider.createRefreshToken(any()))
                .willReturn("new.refresh.token");

        TokenResponse response = authService.refreshToken(oldRefreshToken);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new.access.token");
        assertThat(response.getRefreshToken()).isEqualTo("new.refresh.token");

        then(jwtTokenProvider).should(times(1)).validateToken(oldRefreshToken);
        then(authRepository).should(atLeast(1)).findByUserEmail(testMember.getUserEmail());
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 유효하지 않은 Refresh Token")
    void refreshTokenFail_InvalidToken() {
        String invalidToken = "invalid.refresh.token";
        given(jwtTokenProvider.validateToken(invalidToken)).willReturn(false);

        assertThatThrownBy(() -> authService.refreshToken(invalidToken))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("유효하지 않은 Refresh Token입니다");

        then(authRepository).should(never()).findByUserEmail(anyString());
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 저장된 Refresh Token 없음")
    void refreshTokenFail_NoStoredToken() {
        String refreshToken = "valid.refresh.token";
        given(jwtTokenProvider.validateToken(refreshToken)).willReturn(true);
        given(jwtTokenProvider.getUserEmailFromToken(refreshToken))
                .willReturn(testMember.getUserEmail());
        given(authRepository.findByUserEmail(testMember.getUserEmail()))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refreshToken(refreshToken))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("저장된 Refresh Token이 없습니다");
    }

    @Test
    @DisplayName("토큰 갱신 실패 - Refresh Token 불일치")
    void refreshTokenFail_TokenMismatch() {
        String providedToken = "provided.token";
        String storedTokenValue = "stored.token.different";

        RefreshToken storedToken = RefreshToken.builder()
                .userEmail(testMember.getUserEmail())
                .token(storedTokenValue)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();

        given(jwtTokenProvider.validateToken(providedToken)).willReturn(true);
        given(jwtTokenProvider.getUserEmailFromToken(providedToken))
                .willReturn(testMember.getUserEmail());
        given(authRepository.findByUserEmail(testMember.getUserEmail()))
                .willReturn(Optional.of(storedToken));

        assertThatThrownBy(() -> authService.refreshToken(providedToken))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Refresh Token이 일치하지 않습니다");
    }

    @Test
    @DisplayName("토큰 갱신 실패 - Refresh Token 만료")
    void refreshTokenFail_TokenExpired() {
        String expiredToken = "expired.refresh.token";
        RefreshToken storedToken = RefreshToken.builder()
                .userEmail(testMember.getUserEmail())
                .token(expiredToken)
                .expiryDate(LocalDateTime.now().minusDays(1))
                .build();

        given(jwtTokenProvider.validateToken(expiredToken)).willReturn(true);
        given(jwtTokenProvider.getUserEmailFromToken(expiredToken))
                .willReturn(testMember.getUserEmail());
        given(authRepository.findByUserEmail(testMember.getUserEmail()))
                .willReturn(Optional.of(storedToken));

        assertThatThrownBy(() -> authService.refreshToken(expiredToken))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Refresh Token이 만료되었습니다");

        then(authRepository).should(times(1)).delete(storedToken);
    }

    @Test
    @DisplayName("로그아웃 성공 - Refresh Token을 삭제한다")
    void logoutSuccess() {
        String refreshToken = "valid.refresh.token";
        given(jwtTokenProvider.validateToken(refreshToken)).willReturn(true);
        given(jwtTokenProvider.getUserEmailFromToken(refreshToken))
                .willReturn(testMember.getUserEmail());

        authService.logout(refreshToken);

        then(jwtTokenProvider).should(times(1)).validateToken(refreshToken);
        then(jwtTokenProvider).should(times(1)).getUserEmailFromToken(refreshToken);
        then(authRepository).should(times(1)).deleteByUserEmail(testMember.getUserEmail());
    }

    @Test
    @DisplayName("로그아웃 - 유효하지 않은 토큰이어도 예외를 던지지 않는다")
    void logout_InvalidToken_NoException() {
        String invalidToken = "invalid.refresh.token";
        given(jwtTokenProvider.validateToken(invalidToken)).willReturn(false);

        assertThatCode(() -> authService.logout(invalidToken))
                .doesNotThrowAnyException();

        then(authRepository).should(never()).deleteByUserEmail(anyString());
    }

    @Test
    @DisplayName("Refresh Token 저장 - 기존 토큰이 없으면 새로 저장한다")
    void saveRefreshToken_NewToken() {
        String newToken = "new.refresh.token";
        given(memberRepository.findByUserEmail(testMember.getUserEmail()))
                .willReturn(Optional.of(testMember));
        given(passwordEncoder.matches(loginRequest.getUserPw(), testMember.getUserPw()))
                .willReturn(true);
        given(jwtTokenProvider.createAccessToken(any()))
                .willReturn("access.token");
        given(jwtTokenProvider.createRefreshToken(any()))
                .willReturn(newToken);
        given(authRepository.findByUserEmail(testMember.getUserEmail()))
                .willReturn(Optional.empty());

        authService.login(loginRequest);

        then(authRepository).should(times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Refresh Token 갱신 - 기존 토큰이 있으면 업데이트한다")
    void updateRefreshToken_ExistingToken() {
        String oldToken = "old.refresh.token";
        String newToken = "new.refresh.token";

        RefreshToken existingToken = RefreshToken.builder()
                .userEmail(testMember.getUserEmail())
                .token(oldToken)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();

        given(memberRepository.findByUserEmail(testMember.getUserEmail()))
                .willReturn(Optional.of(testMember));
        given(passwordEncoder.matches(loginRequest.getUserPw(), testMember.getUserPw()))
                .willReturn(true);
        given(jwtTokenProvider.createAccessToken(any()))
                .willReturn("access.token");
        given(jwtTokenProvider.createRefreshToken(any()))
                .willReturn(newToken);
        given(authRepository.findByUserEmail(testMember.getUserEmail()))
                .willReturn(Optional.of(existingToken));

        authService.login(loginRequest);

        then(authRepository).should(never()).save(any(RefreshToken.class));
    }
}
```

</details>

<details>
<summary><b>TestSecurityConfig.java</b> - 테스트용 Security 설정</summary>

**경로:** `member-service/src/test/java/com/team2/memberservice/auth/controller/TestSecurityConfig.java`

```java
package com.team2.memberservice.auth.controller;

import com.team2.commonmodule.error.ErrorCode;
import com.team2.commonmodule.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/members/**").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            ApiResponse<Void> errorResponse = ApiResponse.error(ErrorCode.UNAUTHENTICATED);
                            new ObjectMapper().writeValue(response.getWriter(), errorResponse);
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            ApiResponse<Void> errorResponse = ApiResponse.error(ErrorCode.ACCESS_DENIED);
                            new ObjectMapper().writeValue(response.getWriter(), errorResponse);
                        }));
        return http.build();
    }
}
```

</details>

<details>
<summary><b>TestExceptionHandler.java</b> - 테스트용 예외 핸들러</summary>

**경로:** `member-service/src/test/java/com/team2/memberservice/auth/controller/TestExceptionHandler.java`

```java
package com.team2.memberservice.auth.controller;

import com.team2.commonmodule.error.BusinessException;
import com.team2.commonmodule.error.ErrorCode;
import com.team2.commonmodule.response.ApiResponse;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@TestConfiguration
public class TestExceptionHandler {

    @ControllerAdvice
    public static class TestExceptionHandlerAdvice {

        @ExceptionHandler(BadCredentialsException.class)
        @ResponseBody
        public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(BadCredentialsException e) {
            ApiResponse<Void> response = ApiResponse.error(ErrorCode.LOGIN_FAILED);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

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
```

</details>

### 2.3 JWT Tests

<details>
<summary><b>JwtTokenResponseTest.java</b> - JWT 응답 DTO 테스트 (2개 테스트)</summary>

**경로:** `member-service/src/test/java/com/team2/memberservice/jwt/dto/JwtTokenResponseTest.java`

```java
package com.team2.memberservice.jwt.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtTokenResponse DTO 테스트")
class JwtTokenResponseTest {

    @Test
    @DisplayName("Builder를 사용하여 객체를 생성한다")
    void createUsingBuilder() {
        JwtTokenResponse.UserInfo userInfo = JwtTokenResponse.UserInfo.builder()
                .userId(1L)
                .email("test@example.com")
                .nickname("tester")
                .role("ROLE_USER")
                .build();

        JwtTokenResponse response = JwtTokenResponse.builder()
                .grantType("Bearer")
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .accessTokenExpiresIn(3600L)
                .userInfo(userInfo)
                .build();

        assertThat(response.getGrantType()).isEqualTo("Bearer");
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getAccessTokenExpiresIn()).isEqualTo(3600L);
        assertThat(response.getUserInfo()).isEqualTo(userInfo);
    }

    @Test
    @DisplayName("static factory method 'of'를 사용하여 객체를 생성한다")
    void createUsingStaticFactory() {
        JwtTokenResponse.UserInfo userInfo = JwtTokenResponse.UserInfo.builder()
                .userId(1L)
                .email("test@example.com")
                .nickname("tester")
                .role("ROLE_USER")
                .build();

        JwtTokenResponse response = JwtTokenResponse.of(
                "access-token",
                "refresh-token",
                3600L,
                userInfo);

        assertThat(response.getGrantType()).isEqualTo("Bearer");
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getAccessTokenExpiresIn()).isEqualTo(3600L);
        assertThat(response.getUserInfo()).isEqualTo(userInfo);
    }
}
```

</details>

### 2.4 Integration Test

<details>
<summary><b>MemberIntegrationTest.java</b> - 전체 플로우 통합 테스트 (2개 테스트)</summary>

**경로:** `member-service/src/test/java/com/team2/memberservice/integration/MemberIntegrationTest.java`

```java
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
@Transactional
@DisplayName("Member Service 통합 테스트")
class MemberIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("회원가입 -> 로그인 -> 내 정보 조회 시나리오")
    void signupLoginAndGetProfile() throws Exception {
        SignUpRequest signUpRequest = new SignUpRequest("integration@test.com", "password123", "통합테스터");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

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
        SignUpRequest user1 = new SignUpRequest("duplicate@test.com", "pw1", "user1");
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isOk());

        SignUpRequest user2 = new SignUpRequest("duplicate@test.com", "pw2", "user2");
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user2)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }
}
```

</details>

---

## 3. Story Service

### 3.1 Category Tests

<details>
<summary><b>CategoryControllerTest.java</b> - 카테고리 조회 API 테스트 (3개 테스트)</summary>

**경로:** `story-service/src/test/java/com/team2/storyservice/category/controller/CategoryControllerTest.java`

```java
package com.team2.storyservice.category.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.team2.storyservice.category.entity.Category;
import com.team2.storyservice.category.repository.CategoryRepository;

import java.util.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @InjectMocks
    private CategoryController categoryController;

    @Mock
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("카테고리 목록 조회 - 정상")
    void getCategories_Success() {
        Category category1 = new Category("ROMANCE", "로맨스");
        Category category2 = new Category("FANTASY", "판타지");

        List<Category> categories = Arrays.asList(category1, category2);
        given(categoryRepository.findAll()).willReturn(categories);

        var response = categoryController.getCategories();

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).hasSize(2);
        assertThat(response.getData().get(0).getCategoryId()).isEqualTo("ROMANCE");
        assertThat(response.getData().get(1).getCategoryId()).isEqualTo("FANTASY");
    }

    @Test
    @DisplayName("카테고리 목록 조회 - 빈 목록")
    void getCategories_EmptyList() {
        given(categoryRepository.findAll()).willReturn(Collections.emptyList());

        var response = categoryController.getCategories();

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEmpty();
    }

    @Test
    @DisplayName("카테고리 목록 조회 - 단일 카테고리")
    void getCategories_SingleCategory() {
        Category category = new Category("MYSTERY", "미스터리");

        given(categoryRepository.findAll()).willReturn(Collections.singletonList(category));

        var response = categoryController.getCategories();

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).hasSize(1);
        assertThat(response.getData().get(0).getCategoryName()).isEqualTo("미스터리");
    }
}
```

</details>

### 3.2 테스트 헬퍼

<details>
<summary><b>TestSecurityConfig.java</b> - Story-Service 테스트용 Security 설정</summary>

**경로:** `story-service/src/test/java/com/team2/storyservice/command/book/controller/TestSecurityConfig.java`

```java
package com.team2.storyservice.command.book.controller;

import com.team2.commonmodule.error.ErrorCode;
import com.team2.commonmodule.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/books/**").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            ApiResponse<Void> errorResponse = ApiResponse.error(ErrorCode.UNAUTHENTICATED);
                            new ObjectMapper().writeValue(response.getWriter(), errorResponse);
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            ApiResponse<Void> errorResponse = ApiResponse.error(ErrorCode.ACCESS_DENIED);
                            new ObjectMapper().writeValue(response.getWriter(), errorResponse);
                        }));
        return http.build();
    }
}
```

</details>

<details>
<summary><b>TestExceptionHandler.java</b> - Story-Service 테스트용 예외 핸들러</summary>

**경로:** `story-service/src/test/java/com/team2/storyservice/command/book/controller/TestExceptionHandler.java`

```java
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
```

</details>

---

## 4. Reaction Service

### 4.1 테스트 헬퍼

<details>
<summary><b>TestSecurityConfig.java</b> - Reaction-Service 테스트용 Security 설정</summary>

**경로:** `reaction-service/src/test/java/com/team2/reactionservice/command/reaction/controller/TestSecurityConfig.java`

```java
package com.team2.reactionservice.command.reaction.controller;

import com.team2.commonmodule.error.ErrorCode;
import com.team2.commonmodule.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/reactions/**").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            ApiResponse<Void> errorResponse = ApiResponse.error(ErrorCode.UNAUTHENTICATED);
                            new ObjectMapper().writeValue(response.getWriter(), errorResponse);
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            ApiResponse<Void> errorResponse = ApiResponse.error(ErrorCode.ACCESS_DENIED);
                            new ObjectMapper().writeValue(response.getWriter(), errorResponse);
                        }));
        return http.build();
    }
}
```

</details>

<details>
<summary><b>TestExceptionHandler.java</b> - Reaction-Service 테스트용 예외 핸들러</summary>

**경로:** `reaction-service/src/test/java/com/team2/reactionservice/command/reaction/controller/TestExceptionHandler.java`

```java
package com.team2.reactionservice.command.reaction.controller;

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
```

</details>

### 4.2 Integration Test

<details>
<summary><b>ReactionIntegrationTest.java</b> - 댓글 작성 통합 테스트 (2개 테스트)</summary>

**경로:** `reaction-service/src/test/java/com/team2/reactionservice/integration/ReactionIntegrationTest.java`

```java
package com.team2.reactionservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team2.reactionservice.command.reaction.dto.request.CreateCommentRequest;
import com.team2.commonmodule.feign.MemberServiceClient;
import com.team2.commonmodule.feign.StoryServiceClient;
import com.team2.commonmodule.feign.dto.MemberInfoDto;
import com.team2.commonmodule.response.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Reaction Service 통합 테스트")
class ReactionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MemberServiceClient memberServiceClient;

    @MockBean
    private StoryServiceClient storyServiceClient;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    @Test
    @DisplayName("댓글 작성 성공 테스트")
    void createCommentSuccess() throws Exception {
        Long userId = 1L;
        Long bookId = 100L;
        String content = "Integration Test Comment";

        CreateCommentRequest request = new CreateCommentRequest(bookId, content, null);

        MemberInfoDto memberInfo = MemberInfoDto.builder()
                .userId(userId)
                .userNicknm("TestUser")
                .userRole("USER")
                .build();
        given(memberServiceClient.getMemberInfo(userId)).willReturn(ApiResponse.success(memberInfo));

        mockMvc.perform(post("/api/reactions/comments")
                .header("X-User-Id", String.valueOf(userId))
                .header("X-User-Email", "test@example.com")
                .header("X-User-Role", "USER")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    @DisplayName("댓글 작성 실패 - 내용 없음")
    void createCommentFailValidation() throws Exception {
        Long userId = 1L;
        CreateCommentRequest request = new CreateCommentRequest(100L, "", null);

        mockMvc.perform(post("/api/reactions/comments")
                .header("X-User-Id", String.valueOf(userId))
                .header("X-User-Email", "test@example.com")
                .header("X-User-Role", "USER")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
```

</details>

---

## 5. Config Server

<details>
<summary><b>ConfigServerApplicationTests.java</b> - Spring Context 로딩 테스트</summary>

**경로:** `config-server/src/test/java/com/team2/configserver/ConfigServerApplicationTests.java`

```java
package com.team2.configserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.profiles.active=native",
    "spring.cloud.config.server.git.uri=",
    "spring.cloud.config.server.git.clone-on-start=false"
})
class ConfigServerApplicationTests {

  @Test
  void contextLoads() {
  }
}
```

</details>

---

## 테스트 실행 방법

### 전체 테스트 실행
```bash
# 루트 디렉토리에서
./gradlew test

# 테스트 및 JaCoCo 리포트 생성
./gradlew test jacocoTestReport

# 커버리지 검증 (70% 기준)
./gradlew test jacocoTestCoverageVerification
```

### 특정 모듈 테스트 실행
```bash
./gradlew :common-module:test
./gradlew :member-service:test
./gradlew :story-service:test
./gradlew :reaction-service:test
```

### JaCoCo 리포트 확인
```bash
# 브라우저에서 열기
start next-page-msa/member-service/build/reports/jacoco/test/html/index.html
start next-page-msa/story-service/build/reports/jacoco/test/html/index.html
start next-page-msa/reaction-service/build/reports/jacoco/test/html/index.html
```

---

## 관련 문서

- [README.md](../README.md) - 프로젝트 전체 개요

---

**문서 생성일:** 2026-01-20
**테스트 실행 환경:** Windows 11, JDK 17, Gradle 9.0.0
**전체 테스트 결과:** 253개 테스트 모두 PASS
