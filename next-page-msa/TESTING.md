# Next-Page-MSA 테스트 가이드

## 📋 목차
- [테스트 실행 방법](#테스트-실행-방법)
- [Jacoco 커버리지 리포트](#jacoco-커버리지-리포트)
- [작성된 테스트 목록](#작성된-테스트-목록)
- [테스트 구조](#테스트-구조)

---

## 🚀 테스트 실행 방법

### 전체 프로젝트 테스트 실행
```bash
./gradlew test
```

### 특정 서비스 테스트 실행
```bash
# Member Service
./gradlew :member-service:test

# Story Service
./gradlew :story-service:test

# Reaction Service
./gradlew :reaction-service:test
```

### 특정 테스트 클래스 실행
```bash
./gradlew :member-service:test --tests "AuthServiceTest"
./gradlew :story-service:test --tests "BookServiceTest"
./gradlew :reaction-service:test --tests "ReactionServiceTest"
```

### 특정 테스트 메서드 실행
```bash
./gradlew :member-service:test --tests "AuthServiceTest.loginSuccess"
```

---

## 📊 Jacoco 커버리지 리포트

### 개별 서비스 리포트 생성
```bash
# Member Service 리포트
./gradlew :member-service:test :member-service:jacocoTestReport

# Story Service 리포트
./gradlew :story-service:test :story-service:jacocoTestReport

# Reaction Service 리포트
./gradlew :reaction-service:test :reaction-service:jacocoTestReport
```

**리포트 위치:**
- Member Service: `member-service/build/reports/jacoco/test/html/index.html`
- Story Service: `story-service/build/reports/jacoco/test/html/index.html`
- Reaction Service: `reaction-service/build/reports/jacoco/test/html/index.html`

### 통합 리포트 생성 (전체 프로젝트)
```bash
./gradlew jacocoRootReport
```

**통합 리포트 위치:**
- HTML: `build/reports/jacoco/aggregate/index.html`
- XML: `build/reports/jacoco/aggregate/jacocoTestReport.xml`

### 커버리지 검증
```bash
# 70% 커버리지 요구사항 검증
./gradlew :member-service:jacocoTestCoverageVerification
./gradlew :story-service:jacocoTestCoverageVerification
./gradlew :reaction-service:jacocoTestCoverageVerification
```

### 리포트 보기 (Windows)
```bash
# 통합 리포트 브라우저에서 열기
start build/reports/jacoco/aggregate/index.html

# Member Service 리포트
start member-service/build/reports/jacoco/test/html/index.html
```

---

## 📝 작성된 테스트 목록

### Member Service

#### AuthServiceTest.java
- ✅ 로그인 성공 - Access Token과 Refresh Token을 반환한다
- ✅ 로그인 실패 - 존재하지 않는 이메일
- ✅ 로그인 실패 - 비밀번호 불일치
- ✅ 로그인 실패 - 관리자 승인 대기 상태
- ✅ 토큰 갱신 성공 - 새로운 Access Token과 Refresh Token을 반환한다
- ✅ 토큰 갱신 실패 - 유효하지 않은 Refresh Token
- ✅ 토큰 갱신 실패 - 저장된 Refresh Token 없음
- ✅ 토큰 갱신 실패 - Refresh Token 불일치
- ✅ 토큰 갱신 실패 - Refresh Token 만료
- ✅ 로그아웃 성공 - Refresh Token을 삭제한다
- ✅ 로그아웃 - 유효하지 않은 토큰이어도 예외를 던지지 않는다
- ✅ Refresh Token 저장 - 기존 토큰이 없으면 새로 저장한다
- ✅ Refresh Token 갱신 - 기존 토큰이 있으면 업데이트한다

#### MemberServiceTest.java
- ✅ 일반 사용자 등록 성공
- ✅ 일반 사용자 등록 실패 - 이메일 중복
- ✅ 일반 사용자 등록 실패 - 닉네임 중복
- ✅ 관리자 등록 성공 - PENDING 상태로 생성
- ✅ 관리자 등록 실패 - 이메일 중복
- ✅ 관리자 승인 성공
- ✅ 관리자 승인 실패 - 관리자 권한 없음
- ✅ 관리자 승인 실패 - 사용자를 찾을 수 없음
- ✅ 회원 탈퇴 성공 - Soft Delete
- ✅ 회원 탈퇴 실패 - 사용자를 찾을 수 없음
- ✅ 이메일 중복 검증 - 중복되지 않음
- ✅ 이메일 중복 검증 - 중복됨
- ✅ 닉네임 중복 검증 - 중복되지 않음
- ✅ 닉네임 중복 검증 - 중복됨
- ✅ 관리자에 의한 회원 탈퇴 성공
- ✅ 관리자에 의한 회원 탈퇴 실패 - 관리자 권한 없음
- ✅ 관리자에 의한 회원 탈퇴 실패 - 사용자를 찾을 수 없음

#### MemberQueryServiceTest.java
- ✅ 마이페이지 조회 - 정상 조회 (모든 통계 성공)
- ✅ 마이페이지 조회 - Story Service 호출 실패 (통계 0 유지)
- ✅ 마이페이지 조회 - Reaction Service 호출 실패 (통계 0 유지)
- ✅ 마이페이지 조회 - 모든 Feign 호출 실패 (기본값 유지)
- ✅ 마이페이지 조회 - 회원 찾을 수 없음 → BusinessException

#### JwtTokenProviderTest.java
- ✅ Access Token 생성 - 정상
- ✅ Access Token 생성 - ADMIN 권한
- ✅ Access Token 생성 - Principal이 CustomUserDetails가 아닌 경우
- ✅ Refresh Token 생성 - 정상
- ✅ Refresh Token 생성 - Principal이 CustomUserDetails가 아닌 경우
- ✅ 토큰 검증 - 유효한 토큰
- ✅ 토큰 검증 - ExpiredJwtException (만료된 토큰)
- ✅ 토큰 검증 - MalformedJwtException (잘못된 형식)
- ✅ 토큰 검증 - IllegalArgumentException (null 토큰, 빈 토큰)
- ✅ 토큰 검증 - 잘못된 형식의 토큰 (점이 부족)
- ✅ 토큰으로부터 Authentication 객체 생성 - 정상
- ✅ 토큰으로부터 Authentication 생성 - 권한 정보 없음
- ✅ 토큰으로부터 Authentication 생성 - 이메일 정보 없음
- ✅ Access Token 생성 후 Authentication 복원
- ✅ 토큰에서 이메일 추출 - 정상
- ✅ 토큰에서 이메일 추출 - 만료된 토큰도 Claims 반환
- ✅ Refresh Token 유효성 검증
- ✅ Refresh Token 만료 시간 확인
- ✅ 토큰의 남은 유효 시간 확인 - 정상/만료

#### MemberIntegrationTest.java
- ✅ 회원가입 -> 로그인 -> 내 정보 조회 시나리오
- ✅ 중복 이메일 가입 실패 테스트

### Story Service

#### BookServiceTest.java
- ✅ 소설 생성 성공 - 첫 문장과 함께 생성된다
- ✅ 문장 이어쓰기 성공 - 정상적으로 다음 문장이 추가된다
- ✅ 문장 이어쓰기 실패 - 연속 작성 불가 (일반 유저)
- ✅ 문장 이어쓰기 성공 - 관리자는 연속 작성 가능
- ✅ 문장 이어쓰기 실패 - 완결된 소설에는 작성 불가
- ✅ 문장 이어쓰기 실패 - 존재하지 않는 소설
- ✅ 소설 수동 완결 성공 - 작성자가 완결 처리
- ✅ 소설 수동 완결 실패 - 작성자가 아님
- ✅ 소설 수동 완결 실패 - 이미 완결된 소설
- ✅ 소설 제목 수정 성공 - 작성자가 수정
- ✅ 소설 제목 수정 성공 - 관리자가 수정
- ✅ 소설 제목 수정 실패 - 작성자도 관리자도 아님
- ✅ 소설 삭제 성공 - 작성자가 삭제
- ✅ 소설 삭제 성공 - 관리자가 삭제
- ✅ 소설 삭제 실패 - 작성자도 관리자도 아님
- ✅ 문장 수정 성공 - 마지막 문장만 수정 가능
- ✅ 문장 수정 실패 - 마지막 문장이 아님
- ✅ 문장 삭제 성공 - 마지막 문장만 삭제 가능

#### BookQueryServiceTest.java
- ✅ 소설 검색 - 데이터 있음
- ✅ 소설 검색 - 빈 목록 반환
- ✅ 소설 검색 - Feign 성공: 작가 정보 조회
- ✅ 소설 검색 - Feign 실패: 작가 정보 (닉네임 null)
- ✅ 소설 검색 - Feign 성공: 반응 정보 조회
- ✅ 소설 검색 - Feign 실패: 반응 정보 (기본값 0)
- ✅ 소설 상세 보기 - 정상 조회 + Feign 성공
- ✅ 소설 상세 보기 - 존재하지 않음 → BusinessException
- ✅ 소설 상세 보기 - Feign 실패: 닉네임 null
- ✅ 뷰어 모드 조회 - 로그인 사용자
- ✅ 뷰어 모드 조회 - 비로그인 사용자
- ✅ 뷰어 모드 조회 - 존재하지 않는 소설 → BusinessException
- ✅ 뷰어 모드 조회 - Feign 성공: 회원 정보 (작가 + 문장 작가들)
- ✅ 뷰어 모드 조회 - Feign 성공: 반응 정보
- ✅ 사용자 문장 조회 - 정상 조회
- ✅ 사용자 문장 조회 - 빈 목록
- ✅ 사용자 문장 조회 - Feign 실패

#### BookControllerTest / BookQueryControllerTest
- ✅ POST /api/books - 소설 생성 성공
- ✅ POST /api/books/{bookId}/sentences - 문장 이어쓰기 성공/실패
- ✅ PATCH /api/books/{bookId}/sentences/{sentenceId} - 문장 수정
- ✅ DELETE /api/books/{bookId}/sentences/{sentenceId} - 문장 삭제
- ✅ PATCH /api/books/{bookId}/title - 제목 수정
- ✅ GET /api/books - 소설 검색
- ✅ GET /api/books/{bookId} - 소설 상세
- ✅ GET /api/books/mysentences - 내 문장 조회
- ✅ GET /api/books/{bookId}/viewer - 뷰어 조회

### Reaction Service

#### ReactionServiceTest.java
- ✅ 댓글 작성 성공 - 일반 댓글
- ✅ 댓글 작성 성공 - 대댓글 (parentId 존재)
- ✅ 댓글 작성 실패 - 부모 댓글이 존재하지 않음
- ✅ 댓글 작성 실패 - 부모 댓글과 다른 소설
- ✅ 댓글 수정 성공 - 작성자가 수정
- ✅ 댓글 수정 실패 - 작성자가 아님
- ✅ 댓글 삭제 성공 - 작성자가 삭제
- ✅ 댓글 삭제 성공 - 관리자가 삭제
- ✅ 댓글 삭제 실패 - 작성자도 관리자도 아님
- ✅ 소설 투표 성공 - 새로운 좋아요 투표
- ✅ 소설 투표 성공 - 같은 투표 토글 (취소)
- ✅ 소설 투표 성공 - 다른 투표로 변경
- ✅ 문장 투표 성공 - 새로운 좋아요 투표
- ✅ 문장 투표 성공 - 같은 투표 토글 (취소)
- ✅ 문장 투표 성공 - 다른 투표로 변경

#### ReactionQueryServiceTest.java
- ✅ 댓글 목록 조회 - 부모-자식 관계 검증 (트리 구조)
- ✅ 댓글 목록 조회 - Orphan 처리 (부모 삭제된 댓글은 최상위로)
- ✅ 댓글 목록 조회 - 최상위 댓글만
- ✅ 댓글 목록 조회 - 빈 목록
- ✅ 댓글 목록 조회 - Feign 성공: 회원 정보
- ✅ 댓글 목록 조회 - Feign 실패: 닉네임 null
- ✅ 사용자 댓글 조회 - 정상 조회
- ✅ 사용자 댓글 조회 - 빈 목록
- ✅ 사용자 댓글 조회 - Feign 성공: 회원 + 소설 정보
- ✅ 사용자 댓글 조회 - Feign 실패: 모든 정보 null

#### ReactionIntegrationTest.java
- ✅ 댓글 작성 성공 테스트
- ✅ 댓글 작성 실패 - 내용 없음

---

## 🏗️ 테스트 구조

### 단위 테스트 (Unit Test)
```java
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 단위 테스트")
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private MemberRepository memberRepository;

    @Test
    @DisplayName("로그인 성공 - Access Token과 Refresh Token을 반환한다")
    void loginSuccess() {
        // Given
        given(memberRepository.findByUserEmail(anyString()))
            .willReturn(Optional.of(testMember));

        // When
        TokenResponse response = authService.login(loginRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isNotBlank();
    }
}
```

### 통합 테스트 (Integration Test)
```java
@WebMvcTest(AuthController.class)
@DisplayName("AuthController 통합 테스트")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Test
    @WithMockUser
    @DisplayName("POST /api/auth/login - 로그인 성공")
    void loginSuccess() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }
}
```

---

## 🎯 테스트 커버리지 목표

### 현재 설정
- **Line Coverage**: 70% 이상
- **Branch Coverage**: 70% 이상

### 제외 대상
- Configuration 클래스 (`**/config/**`)
- DTO 클래스 (`**/dto/**`)
- Entity 클래스 (`**/entity/**`)
- Application 메인 클래스
- WebSocket DTO (`**/websocket/dto/**`)
- Feign Client 인터페이스 (`**/feign/**`)

---

## 📚 테스트 Best Practices

### 1. Given-When-Then 패턴 사용
```java
@Test
void test() {
    // Given - 테스트 준비
    given(repository.findById(1L)).willReturn(Optional.of(entity));

    // When - 실행
    Result result = service.doSomething(1L);

    // Then - 검증
    assertThat(result).isNotNull();
}
```

### 2. 명확한 테스트 이름
```java
@DisplayName("로그인 실패 - 비밀번호 불일치")
void loginFail_PasswordMismatch() { ... }
```

### 3. BDD Mockito 사용
```java
// given() - 스텁 설정
given(service.method()).willReturn(value);

// then() - 검증
then(service).should(times(1)).method();
```

---
