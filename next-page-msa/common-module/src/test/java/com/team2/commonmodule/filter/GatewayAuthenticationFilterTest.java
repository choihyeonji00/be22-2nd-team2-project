package com.team2.commonmodule.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

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

    @Nested
    @DisplayName("인증 실패 시 401/403 응답")
    class AuthenticationFailureTest {

        @Test
        @DisplayName("인증 없이 보호된 리소스 접근 시 인증 정보가 null이다")
        void unauthenticatedAccess_NoAuthentication() throws ServletException, IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            request.setRequestURI("/api/members/me");

            // When
            gatewayAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication).isNull();
        }
    }
}
