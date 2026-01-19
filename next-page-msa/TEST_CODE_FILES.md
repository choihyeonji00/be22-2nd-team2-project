# Next Page MSA - 전체 테스트 코드 파일 목록

> **본 문서는 next-page-msa 프로젝트의 모든 테스트 코드 파일 경로와 GitHub 링크를 제공합니다.**
> 각 파일을 클릭하여 실제 테스트 코드를 확인할 수 있습니다.

---

## 📊 테스트 파일 통계

| 모듈 | 테스트 파일 수 | 테스트 메서드 수 |
|:---:|:---:|:---:|
| **Common Module** | 5 | 37 |
| **Member Service** | 10 | 90+ |
| **Story Service** | 5 | 75+ |
| **Reaction Service** | 5 | 50+ |
| **Config Server** | 1 | 1 |
| **전체** | **26** | **253+** |

---

## 1️⃣ Common Module (5개 파일)

### Filter Tests

#### 1. GatewayAuthenticationFilterTest.java
**경로:** `common-module/src/test/java/com/team2/commonmodule/filter/GatewayAuthenticationFilterTest.java`

**테스트 내용:**
- Gateway 헤더 기반 Spring Security 인증 설정 (13개 테스트)
- X-User-Id, X-User-Email, X-User-Role 헤더 파싱
- ROLE_USER, ROLE_ADMIN 권한 부여 검증
- 필터 제외 경로 테스트 (Swagger, API Docs, Actuator, H2 Console, Error)

**주요 테스트 시나리오:**
```java
@Test validGatewayHeaders_SetsAuthentication()  // 유효한 헤더로 인증 설정
@Test adminRole_GrantsRoleAdmin()               // ADMIN 역할 권한 부여
@Test noUserId_NoAuthentication()               // User-Id 누락 시 인증 미설정
@Test swaggerUiPath_ShouldNotFilter()           // Swagger 경로 필터 제외
```

---

#### 2. JwtToHeaderFilterTest.java
**경로:** `common-module/src/test/java/com/team2/commonmodule/filter/JwtToHeaderFilterTest.java`

**테스트 내용:**
- JWT 토큰 파싱 및 Gateway 헤더 변환 (11개 테스트)
- Bearer 토큰 추출 및 Claims 파싱
- 만료 토큰, 잘못된 토큰 처리
- Secret Key 설정 검증

**주요 테스트 시나리오:**
```java
@Test validJwtToken_ConvertsToHeaders()         // 유효한 JWT → 헤더 변환
@Test existingGatewayHeader_SkipsJwtParsing()   // 기존 헤더 존재 시 스킵
@Test expiredJwtToken_ContinuesWithWarning()    // 만료 토큰 처리
@Test noSecretKey_SkipsJwtParsing()             // Secret Key 없을 시 스킵
```

---

### Security Tests

#### 3. CustomAccessDeniedHandlerTest.java
**경로:** `common-module/src/test/java/com/team2/commonmodule/security/CustomAccessDeniedHandlerTest.java`

**테스트 내용:**
- 403 Forbidden 응답 처리 (3개 테스트)
- ErrorCode.ACCESS_DENIED 메시지 검증
- Content-Type: application/json 설정 확인

**주요 테스트 시나리오:**
```java
@Test handle_Returns403Response()               // 403 응답 반환
@Test handle_SetsCorrectContentType()           // Content-Type 설정
@Test handle_UsesCorrectErrorCode()             // 에러 코드 사용
```

---

#### 4. CustomAuthenticationEntryPointTest.java
**경로:** `common-module/src/test/java/com/team2/commonmodule/security/CustomAuthenticationEntryPointTest.java`

**테스트 내용:**
- 401 Unauthorized 응답 처리 (3개 테스트)
- ErrorCode.UNAUTHENTICATED 메시지 검증
- Content-Type: application/json 설정 확인

**주요 테스트 시나리오:**
```java
@Test commence_Returns401Response()             // 401 응답 반환
@Test commence_SetsCorrectContentType()         // Content-Type 설정
@Test commence_UsesCorrectErrorCode()           // 에러 코드 사용
```

---

#### 5. SerializationTest.java
**경로:** `common-module/src/test/java/SerializationTest.java`

**테스트 내용:**
- ApiResponse 직렬화 테스트 (수동 테스트)
- JSON 변환 검증

---

## 2️⃣ Member Service (10개 파일)

### API Tests

#### 6. MemberApiControllerTest.java
**경로:** `member-service/src/test/java/com/team2/memberservice/api/MemberApiControllerTest.java`

**테스트 내용:**
- Internal API (Feign용) 테스트 (3개 테스트)
- 사용자 닉네임 조회
- 사용자 없을 시 "Unknown" 반환

**주요 테스트 시나리오:**
```java
@Test getUserNickname_Success()                      // 닉네임 조회 성공
@Test getUserNickname_UserNotFound_ReturnsUnknown() // 사용자 없음 시 Unknown
```

---

### Auth Tests

#### 7. AuthControllerTest.java
**경로:** `member-service/src/test/java/com/team2/memberservice/auth/controller/AuthControllerTest.java`

**테스트 내용:**
- 로그인/로그아웃/토큰 갱신 API 테스트 (10개 테스트)
- MockMvc 사용한 컨트롤러 단위 테스트
- JWT Access Token 및 Refresh Token 검증

**주요 테스트 시나리오:**
```java
@Test loginSuccess()                                 // 로그인 성공 - AT/RT 반환
@Test loginFail_InvalidCredentials()                 // 로그인 실패 - 비밀번호 불일치
@Test loginFail_PendingApproval()                    // 로그인 실패 - 승인 대기
@Test refreshSuccess()                               // 토큰 갱신 성공
@Test refreshFail_InvalidToken()                     // 토큰 갱신 실패
@Test logoutSuccess()                                // 로그아웃 성공
```

---

#### 8. AuthServiceTest.java
**경로:** `member-service/src/test/java/com/team2/memberservice/auth/service/AuthServiceTest.java`

**테스트 내용:**
- 인증 서비스 비즈니스 로직 테스트 (13개 테스트)
- 로그인, 토큰 갱신, 로그아웃 로직
- Refresh Token 저장/갱신 로직

**주요 테스트 시나리오:**
```java
@Test loginSuccess()                                 // 로그인 성공
@Test loginFail_UserNotFound()                       // 사용자 없음
@Test loginFail_PasswordMismatch()                   // 비밀번호 불일치
@Test loginFail_PendingStatus()                      // PENDING 상태
@Test refreshTokenSuccess()                          // 토큰 갱신 성공
@Test refreshTokenFail_TokenExpired()                // 토큰 만료
@Test saveRefreshToken_NewToken()                    // RT 저장
@Test updateRefreshToken_ExistingToken()             // RT 업데이트
```

---

### Member Command Tests

#### 9. MemberControllerTest.java
**경로:** `member-service/src/test/java/com/team2/memberservice/command/member/controller/MemberControllerTest.java`

**테스트 내용:**
- 회원 관리 API 테스트 (17개 테스트)
- 회원가입, 탈퇴, 중복 검증, 관리자 승인

**주요 테스트 시나리오:**
```java
@Test signupSuccess()                                // 회원가입 성공
@Test signupFail_DuplicateEmail()                    // 이메일 중복
@Test signupFail_DuplicateNickname()                 // 닉네임 중복
@Test createAdminSuccess()                           // 관리자 생성 성공
@Test approveAdminSuccess()                          // 관리자 승인 성공
@Test withdrawSuccess()                              // 회원 탈퇴 성공
@Test checkEmailSuccess()                            // 이메일 중복 검증
```

---

#### 10. MemberServiceTest.java
**경로:** `member-service/src/test/java/com/team2/memberservice/command/member/service/MemberServiceTest.java`

**테스트 내용:**
- 회원 서비스 비즈니스 로직 테스트 (17개 테스트)
- 회원가입, 탈퇴, 승인, 중복 검증

**주요 테스트 시나리오:**
```java
@Test registUserSuccess()                            // 일반 회원 등록 성공
@Test registUserFail_DuplicateEmail()                // 이메일 중복
@Test registAdminSuccess()                           // 관리자 등록 성공
@Test approveAdminSuccess()                          // 관리자 승인 성공
@Test withdrawSuccess()                              // 회원 탈퇴 성공
@Test validateDuplicateEmail_Duplicated()            // 이메일 중복 검증
```

---

### JWT Tests

#### 11. JwtTokenProviderTest.java
**경로:** `member-service/src/test/java/com/team2/memberservice/jwt/JwtTokenProviderTest.java`

**테스트 내용:**
- JWT 토큰 생성/검증/파싱 테스트 (22개 테스트)
- Access Token, Refresh Token 생성
- 토큰 유효성 검증
- Claims 추출 및 Authentication 복원

**주요 테스트 시나리오:**
```java
@Test createAccessToken_Success()                    // AT 생성 성공
@Test createAccessToken_AdminRole()                  // ADMIN 역할 AT 생성
@Test createRefreshToken_Success()                   // RT 생성 성공
@Test validateToken_Valid()                          // 유효한 토큰 검증
@Test validateToken_ExpiredToken()                   // 만료된 토큰 검증
@Test getAuthentication_Success()                    // Authentication 생성
@Test getUserEmailFromToken_Success()                // 토큰에서 이메일 추출
```

---

#### 12. JwtTokenResponseTest.java
**경로:** `member-service/src/test/java/com/team2/memberservice/jwt/dto/JwtTokenResponseTest.java`

**테스트 내용:**
- JWT 응답 DTO 테스트 (2개 테스트)
- Builder 패턴 및 Static Factory 메서드 검증

**주요 테스트 시나리오:**
```java
@Test createUsingBuilder()                           // Builder로 객체 생성
@Test createUsingStaticFactory()                     // Static Factory 메서드
```

---

### Member Query Tests

#### 13. MemberQueryControllerTest.java
**경로:** `member-service/src/test/java/com/team2/memberservice/query/member/controller/MemberQueryControllerTest.java`

**테스트 내용:**
- 회원 조회 API 테스트 (2개 테스트)
- 마이페이지 조회

**주요 테스트 시나리오:**
```java
@Test getMyInfoSuccess()                             // 내 정보 조회 성공
@Test getMyInfoFail_Unauthenticated()                // 미인증 조회 실패
```

---

#### 14. MemberQueryServiceTest.java
**경로:** `member-service/src/test/java/com/team2/memberservice/query/member/service/MemberQueryServiceTest.java`

**테스트 내용:**
- 회원 조회 서비스 테스트 (5개 테스트)
- Feign Client 통합 테스트 (Story, Reaction 서비스 호출)
- Fallback 처리 검증

**주요 테스트 시나리오:**
```java
@Test getMyPage_Success_AllStatsSucceed()            // 모든 통계 조회 성공
@Test getMyPage_MemberNotFound_ThrowsException()     // 회원 없음 예외
@Test getMyPage_StoryServiceFailure_KeepsDefaultZero() // Story 서비스 실패 시 기본값
@Test getMyPage_AllFeignCallsFailure_KeepsDefaultValues() // 모든 Feign 실패 시 기본값
```

---

### Integration Test

#### 15. MemberIntegrationTest.java
**경로:** `member-service/src/test/java/com/team2/memberservice/integration/MemberIntegrationTest.java`

**테스트 내용:**
- 전체 플로우 통합 테스트 (2개 테스트)
- 회원가입 → 로그인 → 내 정보 조회 시나리오
- Spring Context 전체 로딩

**주요 테스트 시나리오:**
```java
@Test signupLoginAndGetProfile()                     // 회원가입 → 로그인 플로우
@Test signupFailDuplicateEmail()                     // 중복 이메일 가입 실패
```

---

## 3️⃣ Story Service (5개 파일)

### Category Tests

#### 16. CategoryControllerTest.java
**경로:** `story-service/src/test/java/com/team2/storyservice/category/controller/CategoryControllerTest.java`

**테스트 내용:**
- 카테고리 조회 API 테스트 (3개 테스트)
- 전체 카테고리 목록 조회

**주요 테스트 시나리오:**
```java
@Test getCategories_Success()                        // 전체 카테고리 조회 성공
@Test getCategories_EmptyList()                      // 빈 리스트 반환
@Test getCategories_SingleCategory()                 // 단일 카테고리
```

---

### Book Command Tests

#### 17. BookControllerTest.java
**경로:** `story-service/src/test/java/com/team2/storyservice/command/book/controller/BookControllerTest.java`

**테스트 내용:**
- 소설 작성 API 테스트 (16개 테스트)
- 소설 생성, 문장 이어쓰기, 수정, 삭제, 완결

**주요 테스트 시나리오:**
```java
@Test createBookSuccess()                            // 소설 생성 성공
@Test appendSentenceSuccess()                        // 문장 이어쓰기 성공
@Test appendSentenceFail_ConsecutiveWriting()        // 연속 작성 실패
@Test appendSentenceFail_AlreadyCompleted()          // 완결된 소설 작성 실패
@Test completeBookSuccess()                          // 소설 완결 성공
@Test updateBookTitleSuccess()                       // 제목 수정 성공
@Test deleteBookSuccess()                            // 소설 삭제 성공
```

---

#### 18. BookServiceTest.java
**경로:** `story-service/src/test/java/com/team2/storyservice/command/book/service/BookServiceTest.java`

**테스트 내용:**
- 소설 서비스 비즈니스 로직 테스트 (18개 테스트)
- 소설 생성, 문장 이어쓰기, 수정, 삭제, 완결 로직
- 연속 작성 방지, 권한 검증

**주요 테스트 시나리오:**
```java
@Test createBookSuccess()                            // 소설 생성 성공
@Test appendSentenceSuccess()                        // 문장 이어쓰기 성공
@Test appendSentenceFail_ConsecutiveWriting()        // 연속 작성 방지
@Test appendSentenceSuccess_AdminConsecutiveWriting() // 관리자는 연속 작성 가능
@Test completeBookSuccess()                          // 완결 처리 성공
@Test updateSentenceSuccess()                        // 마지막 문장 수정 성공
@Test deleteSentenceSuccess()                        // 마지막 문장 삭제 성공
```

---

### Book Query Tests

#### 19. BookQueryControllerTest.java
**경로:** `story-service/src/test/java/com/team2/storyservice/query/book/controller/BookQueryControllerTest.java`

**테스트 내용:**
- 소설 조회 API 테스트 (6개 테스트)
- 소설 검색, 상세 조회, 뷰어 모드, 내 문장 조회

**주요 테스트 시나리오:**
```java
@Test searchBooksSuccess()                           // 소설 검색 성공
@Test getBookSuccess()                               // 소설 상세 조회
@Test getBookForViewerSuccess_InProgress()           // 진행 중 소설 뷰어
@Test getBookForViewerSuccess_Completed()            // 완결 소설 뷰어
@Test getMySentencesSuccess()                        // 내 문장 조회 성공
```

---

#### 20. BookQueryServiceTest.java
**경로:** `story-service/src/test/java/com/team2/storyservice/query/book/service/BookQueryServiceTest.java`

**테스트 내용:**
- 소설 조회 서비스 테스트 (18개 테스트)
- Feign Client 통합 (Member, Reaction 서비스)
- Fallback 처리 검증

**주요 테스트 시나리오:**
```java
@Test searchBooks_WithData()                         // 소설 검색 - 데이터 있음
@Test searchBooks_FeignSuccess_MemberInfo()          // Feign 성공 - 작가 정보
@Test searchBooks_FeignFailure_MemberInfo()          // Feign 실패 - 닉네임 null
@Test getBook_Success_WithFeignSuccess()             // 소설 상세 - Feign 성공
@Test getBookForViewer_AuthenticatedUser()           // 뷰어 - 인증 사용자
@Test getSentencesByUser_Success()                   // 사용자 문장 조회
```

---

## 4️⃣ Reaction Service (5개 파일)

### Reaction Command Tests

#### 21. ReactionControllerTest.java
**경로:** `reaction-service/src/test/java/com/team2/reactionservice/command/reaction/controller/ReactionControllerTest.java`

**테스트 내용:**
- 댓글/투표 API 테스트 (17개 테스트)
- 댓글 작성, 수정, 삭제
- 소설/문장 투표

**주요 테스트 시나리오:**
```java
@Test createCommentSuccess()                         // 댓글 작성 성공
@Test createCommentSuccess_FeignFail()               // Feign 실패해도 댓글 작성 성공
@Test createReplyCommentSuccess()                    // 대댓글 작성 성공
@Test modifyCommentSuccess()                         // 댓글 수정 성공
@Test removeCommentSuccess()                         // 댓글 삭제 성공
@Test voteBookSuccess_NewVote()                      // 소설 투표 - 새 투표
@Test voteBookSuccess_ToggleCancel()                 // 소설 투표 - 토글 취소
@Test voteSentenceSuccess_ChangeVoteType()           // 문장 투표 - 타입 변경
```

---

#### 22. ReactionServiceTest.java
**경로:** `reaction-service/src/test/java/com/team2/reactionservice/command/reaction/service/ReactionServiceTest.java`

**테스트 내용:**
- 댓글/투표 서비스 로직 테스트 (15개 테스트)
- 댓글 작성, 수정, 삭제 로직
- 투표 추가, 취소, 변경 로직

**주요 테스트 시나리오:**
```java
@Test addCommentSuccess()                            // 댓글 추가 성공
@Test addReplyCommentSuccess()                       // 대댓글 추가 성공
@Test modifyCommentSuccess()                         // 댓글 수정 성공
@Test removeCommentSuccess_Owner()                   // 소유자 댓글 삭제
@Test removeCommentSuccess_Admin()                   // 관리자 댓글 삭제
@Test voteBookSuccess_NewLike()                      // 소설 LIKE 투표
@Test voteBookSuccess_ToggleSameVote()               // 동일 투표 토글
@Test voteSentenceSuccess_ChangeVote()               // 투표 타입 변경
```

---

### Reaction Query Tests

#### 23. ReactionQueryControllerTest.java
**경로:** `reaction-service/src/test/java/com/team2/reactionservice/query/reaction/controller/ReactionQueryControllerTest.java`

**테스트 내용:**
- 댓글 조회 API 테스트 (3개 테스트)
- 소설 댓글 목록, 내 댓글 조회

**주요 테스트 시나리오:**
```java
@Test getCommentsSuccess()                           // 소설 댓글 목록 조회
@Test getMyCommentsSuccess()                         // 내 댓글 조회 성공
@Test getMyCommentsFail_Unauthenticated()            // 미인증 조회 실패
```

---

#### 24. ReactionQueryServiceTest.java
**경로:** `reaction-service/src/test/java/com/team2/reactionservice/query/reaction/service/ReactionQueryServiceTest.java`

**테스트 내용:**
- 댓글 조회 서비스 테스트 (10개 테스트)
- 트리 구조 댓글 변환
- Orphan 댓글 처리
- Feign Client 통합

**주요 테스트 시나리오:**
```java
@Test getComments_TreeStructure()                    // 트리 구조 반환
@Test getComments_OrphanHandling()                   // 고아 댓글 처리
@Test getComments_FeignSuccess_MemberInfo()          // Feign 성공 - 작성자 정보
@Test getCommentsByUser_Success()                    // 사용자별 댓글 조회
```

---

### Integration Test

#### 25. ReactionIntegrationTest.java
**경로:** `reaction-service/src/test/java/com/team2/reactionservice/integration/ReactionIntegrationTest.java`

**테스트 내용:**
- 댓글 작성 통합 테스트 (2개 테스트)
- 전체 Spring Context 로딩

**주요 테스트 시나리오:**
```java
@Test createCommentSuccess()                         // 댓글 작성 통합 테스트
@Test createCommentFailValidation()                  // 빈 내용 유효성 검증
```

---

## 5️⃣ Config Server (1개 파일)

#### 26. ConfigServerApplicationTests.java
**경로:** `config-server/src/test/java/com/team2/configserver/ConfigServerApplicationTests.java`

**테스트 내용:**
- Spring Context 로딩 테스트 (1개 테스트)
- Config Server 애플리케이션 구동 검증

---

## 🧪 테스트 실행 방법

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

## 📚 관련 문서

- [TEST_DOCUMENTATION.md](TEST_DOCUMENTATION.md) - 252개 이상 테스트 케이스 상세 명세
- [README.md](../README.md) - 프로젝트 전체 개요
- [API_SPECIFICATION.md](API_SPECIFICATION.md) - REST API 상세 명세

---

**문서 생성일:** 2026-01-20
**테스트 실행 환경:** Windows 11, JDK 17, Gradle 9.0.0
**마지막 테스트 실행:** 2026-01-19 23:58
**전체 테스트 결과:** ✅ 253개 테스트 모두 PASS
