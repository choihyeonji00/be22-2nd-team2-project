package com.team2.nextpage.jwt;

import com.team2.nextpage.command.member.entity.Member;
import com.team2.nextpage.config.security.CustomUserDetails;
import com.team2.nextpage.fixtures.MemberTestBuilder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private FilterChain filterChain;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("유효한 JWT 토큰으로 인증 성공")
    void doFilterInternal_ValidToken_AuthenticationSuccess() throws ServletException, IOException {
        // given
        String token = "validToken";
        request.addHeader("Authorization", "Bearer " + token);

        Member member = MemberTestBuilder.aMember()
                .withEmail("test@test.com")
                .withUserId(1L)
                .build();
        CustomUserDetails userDetails = new CustomUserDetails(member);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        given(jwtTokenProvider.validateToken(token)).willReturn(true);
        given(jwtTokenProvider.getAuthentication(token)).willReturn(authentication);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("test@test.com");
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Authorization 헤더 없이 요청 시 인증 없이 진행")
    void doFilterInternal_NoAuthorizationHeader_NoAuthentication() throws ServletException, IOException {
        // given - no authorization header

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("유효하지 않은 JWT 토큰으로 요청 시 인증 없이 진행")
    void doFilterInternal_InvalidToken_NoAuthentication() throws ServletException, IOException {
        // given
        String token = "invalidToken";
        request.addHeader("Authorization", "Bearer " + token);

        given(jwtTokenProvider.validateToken(token)).willReturn(false);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Bearer 형식이 아닌 Authorization 헤더로 요청 시 인증 없이 진행")
    void doFilterInternal_NonBearerToken_NoAuthentication() throws ServletException, IOException {
        // given
        request.addHeader("Authorization", "Basic someCredentials");

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("토큰 인증 중 예외 발생 시 SecurityContext 초기화")
    void doFilterInternal_ExceptionDuringAuthentication_ClearContext() throws ServletException, IOException {
        // given
        String token = "validToken";
        request.addHeader("Authorization", "Bearer " + token);

        given(jwtTokenProvider.validateToken(token)).willReturn(true);
        given(jwtTokenProvider.getAuthentication(token)).willThrow(new RuntimeException("Auth error"));

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Swagger UI 경로는 필터 적용 제외")
    void shouldNotFilter_SwaggerPath_ReturnsTrue() throws ServletException {
        // given
        request.setRequestURI("/swagger-ui/index.html");

        // when
        boolean shouldNotFilter = jwtAuthenticationFilter.shouldNotFilter(request);

        // then
        assertThat(shouldNotFilter).isTrue();
    }

    @Test
    @DisplayName("API docs 경로는 필터 적용 제외")
    void shouldNotFilter_ApiDocsPath_ReturnsTrue() throws ServletException {
        // given
        request.setRequestURI("/v3/api-docs");

        // when
        boolean shouldNotFilter = jwtAuthenticationFilter.shouldNotFilter(request);

        // then
        assertThat(shouldNotFilter).isTrue();
    }

    @Test
    @DisplayName("Actuator 경로는 필터 적용 제외")
    void shouldNotFilter_ActuatorPath_ReturnsTrue() throws ServletException {
        // given
        request.setRequestURI("/actuator/health");

        // when
        boolean shouldNotFilter = jwtAuthenticationFilter.shouldNotFilter(request);

        // then
        assertThat(shouldNotFilter).isTrue();
    }

    @Test
    @DisplayName("H2 console 경로는 필터 적용 제외")
    void shouldNotFilter_H2ConsolePath_ReturnsTrue() throws ServletException {
        // given
        request.setRequestURI("/h2-console");

        // when
        boolean shouldNotFilter = jwtAuthenticationFilter.shouldNotFilter(request);

        // then
        assertThat(shouldNotFilter).isTrue();
    }

    @Test
    @DisplayName("일반 API 경로는 필터 적용")
    void shouldNotFilter_ApiPath_ReturnsFalse() throws ServletException {
        // given
        request.setRequestURI("/api/books");

        // when
        boolean shouldNotFilter = jwtAuthenticationFilter.shouldNotFilter(request);

        // then
        assertThat(shouldNotFilter).isFalse();
    }
}
