# [Presentation Context]
- **Role**: 당신은 MSA(Microservices Architecture) 전문 수석 엔지니어이자 발표 전문가입니다.
- **Task**: Spring Cloud 기반의 'Next Page' 프로젝트를 소개하는 20~25분 분량의 기술 발표 자료(PPT)를 생성하세요.
- **Constraints**:
    - **반드시 아래 [Presentation Outline]의 목차 순서를 엄수해야 합니다.**
    - 아래 제공된 [Complete Project Information]의 모든 기술 정보와 트러블슈팅 사례를 깊이 있게 반영하세요.
    - 발표 시간: 질의 시간 포함 20~25분 (슬라이드 15~20장 권장)

---

# [Presentation Outline & Detailed Content]

## 1. 팀/팀원 소개 (Team Introduction)

### Slide 1: Team 2 - Next Page
- **Team Name**: Team 2
- **Project Name**: Next Page (MSA Edition)
- **Mission**: "Together we write stories."
  - 당신의 한 문장이 베스트셀러의 시작이 됩니다
  - 우리가 함께 만드는 실시간 릴레이 소설 플랫폼

### Slide 2: Team Members & Roles (R&R)

**팀 리더: 정진호 (Team Leader)**
- **Core & Architecture 담당**
- 프로젝트 아키텍처 설계 (CQRS, WebSocket, MSA 전환)
- Story 도메인 상태/순서 제어 로직 (Book Aggregate)
- 동적 쿼리 최적화 및 N+1 문제 해결
- WebSocket 실시간 통신 구현

**서브 리더: 김태형 (Sub Leader)**
- **Member & Auth 담당**
- Spring Security + JWT 인증/인가 시스템 구축
- Refresh Token Rotation 및 Soft Delete 적용
- Gateway 연동 및 Internal API 제공
- Resilience4j Circuit Breaker 구현

**개발자: 정병진 (Developer)**
- **Reaction & Support 담당**
- 좋아요/싫어요 투표 시스템 구현
- 계층형 대댓글(Nested Comment) 설계
- 관리자 기능 구현
- 서비스 간 실시간 알림 연동

**문서 관리자: 최현지 (Document Manager)**
- **QA & Documentation 담당**
- API 명세 및 개발자 가이드 관리
- 전체 기능 시나리오 QA 점검
- ERD 및 시퀀스 다이어그램 작성
- 트러블슈팅 케이스 문서화

---

## 2. 프로젝트 소개 (Project Introduction)

### 2-1. 프로젝트 개요 (Overview)

**Slide 3: Next Page - 릴레이 소설 플랫폼**

- **Service Name**: Next Page (Microservices Architecture Edition)
- **정의**: 여러 작가가 실시간으로 한 문장씩 이어 쓰며 하나의 소설을 완성하는 **몰입형 집단 창작 플랫폼**

**MSA 전환 배경**:
- **AS-IS (Monolithic)**:
  - 1개의 Spring Boot 앱으로 모든 기능 통합
  - 배포 시 전체 시스템 재기동 필요 (배포 의존성 높음)
  - 부분 장애 시 전체 시스템 다운 위험
  - 특정 기능만 스케일링 불가 (확장성 제한)

- **TO-BE (MSA)**:
  - 3개의 도메인 서비스(Member, Story, Reaction) + 3개의 인프라 서비스(Gateway, Eureka, Config)로 분리
  - 독립 배포 가능 (서비스별 무중단 배포)
  - 장애 격리 및 Circuit Breaker 적용
  - 서비스별 독립적 확장 가능

**Slide 4: 시스템 아키텍처 다이어그램**

```
┌─────────────────────────────────────────────────────────────┐
│                     Frontend (Vue 3 + Vite)                 │
│                     Port 3000 (Development)                 │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│              API Gateway Server (Port 8000)                 │
│    Spring Cloud Gateway + JWT Auth + Header Injection      │
│              Circuit Breaker (Resilience4j)                 │
└────────────────────────┬────────────────────────────────────┘
                         │
        ┌────────────────┼────────────────┐
        │                │                │
        ▼                ▼                ▼
   ┌─────────┐    ┌─────────┐    ┌──────────┐
   │ Member  │    │ Story   │    │ Reaction │
   │ Service │◄──►│ Service │◄──►│ Service  │
   │(8081)   │    │ (8082)  │    │ (8083)   │
   └─────────┘    └─────────┘    └──────────┘
        │                │                │
   ┌────▼────┐     ┌────▼────┐    ┌─────▼────┐
   │next_page│     │next_page│    │next_page │
   │_member  │     │_story   │    │_reaction │
   │(MariaDB)│     │(MariaDB)│    │(MariaDB) │
   └─────────┘     └─────────┘    └──────────┘
        │                │                │
┌───────┴────────────────┴────────────────┴──────────────────┐
│                    Common Module                            │
│  (Shared: DTO, Feign Client, Auth Filter, Error Handler)   │
└────────────────────────────────────────────────────────────┘
        │
   ┌────┴────────────────────────────┐
   │                                 │
   ▼                                 ▼
┌──────────────────┐       ┌──────────────────┐
│ Discovery Server │       │  Config Server   │
│ (Eureka, 8761)   │       │  (8888)          │
└──────────────────┘       └──────────────────┘
```

**전환 동기 (Motivation)**:
1. **확장성**: 특정 기능(예: 댓글)만 독립적으로 스케일링 불가능
2. **장애 격리**: 이벤트 트래픽 폭주 시 소설 읽기까지 마비되는 현상 방지
3. **독립 배포**: 팀원별 담당 서비스를 독립적으로 개발 및 배포
4. **기술 다양성**: 서비스별 최적화된 기술 스택 선택 가능

### 2-2. 주요 기능 (Key Features)

**Slide 5: 핵심 기능 (Core Features)**

**① 릴레이 소설 창작**
- **순서(Sequence) 제어 기반** 문장 이어쓰기
- **연속 작성 방지**: 마지막 작성자는 다음 문장을 쓸 수 없음 (관리자 제외)
- **마지막 문장 수정/삭제**: 본인이 작성한 마지막 문장만 편집 가능
- **자동 완결**: maxSequence 도달 시 자동으로 COMPLETED 상태로 전환
- **수동 완결**: 소설 작성자가 직접 완결 처리 가능

**② Real-time UX (WebSocket 기반)**
- **실시간 타이핑 인디케이터**: 누군가 문장을 작성 중일 때 실시간 표시
- **라이브 업데이트**: 새 문장 추가 시 자동으로 화면에 반영
- **댓글 알림**: 댓글 작성 시 소설 페이지에 실시간 알림
- **투표 결과 반영**: 좋아요/싫어요 투표 즉시 반영
- **소설 완결 알림**: 소설 완결 시 실시간 상태 변경

**③ Community 기능**
- **계층형 댓글**: 무한 대댓글 지원 (parent-child 관계)
- **좋아요/싫어요 투표**: 소설 및 문장 단위 투표 가능
- **투표 변경/취소**: 같은 타입 재투표 시 취소, 다른 타입 선택 시 변경

**Slide 6: 시스템 기능 (System Features)**

**① Security (보안)**
- **JWT Access Token**: 30분 유효, Bearer 토큰 방식
- **JWT Refresh Token**: 7일 유효, HttpOnly Cookie 저장
- **Refresh Token Rotation**: 토큰 갱신 시 기존 토큰 무효화
- **Gateway 기반 인증 필터**: 모든 요청에 대한 중앙 집중식 인증
- **Soft Delete**: 회원 탈퇴 및 댓글 삭제 시 논리 삭제 적용

**② Reliability (안정성)**
- **Resilience4j Circuit Breaker**: Member Service 장애 시 Story Service는 정상 동작
- **Fallback 메커니즘**: 닉네임 조회 실패 시 "익명 사용자" 반환
- **장애 전파 차단**: 부분 장애가 전체 시스템으로 확산되지 않도록 격리

**③ Performance (성능)**
- **CQRS 패턴**: 쓰기(JPA) / 읽기(MyBatis) 분리
- **Batch API**: N+1 문제 해결 (getMembersBatch)
- **인덱싱**: status, category_id, created_at 등 복합 인덱스 적용
- **WebSocket**: HTTP polling 대비 네트워크 부하 95% 감소

### 2-3. 타 서비스와의 차이 (Differentiation)

**Slide 7: 기술적 차별화 (Technical Depth)**

**① Event-Driven Architecture**
- 단순 게시판이 아닌 **WebSocket & Event-Driven** 기반의 동적 협업 도구
- STOMP 프로토콜을 활용한 Pub/Sub 메시징
- 서비스 간 실시간 이벤트 연동 (Feign-WebSocket Bridge)

**② Hybrid Persistence (CQRS)**
- **쓰기(Command)**: JPA로 도메인 정합성 보장
- **읽기(Query)**: MyBatis로 복잡한 조회 쿼리 최적화
- 소설 목록 조회 시 동적 필터링 및 페이지네이션 성능 향상

**③ Gateway Centric Authentication**
- 인증 로직을 Gateway로 이관(Offloading)
- 마이크로서비스는 순수 비즈니스 로직에만 집중
- JWT 파싱 결과를 `X-User-Id`, `X-User-Role` 헤더로 전달

**④ Common Module 표준화**
- 3개 서비스 간 중복 코드 제거 (DTO, Feign Client, Exception Handler)
- `ApiResponse<T>` 통일로 프론트엔드 연동 생산성 200% 향상
- 전체 MSA 모듈(43개 Task) 병렬 빌드 시간 **21초** 달성

---

## 3. 주요 성과 및 성공 사례 (Achievements)

**Slide 8: 성공 사례 1 - Common Module & 빌드 최적화**

**Challenge**:
- 3개 서비스 간 중복 코드 발생 (DTO, Feign Interceptor, Exception Handler)
- 각 서비스마다 동일한 로직을 복사-붙여넣기하여 유지보수 어려움

**Solution**: `common-module` 라이브러리 구축 및 표준화
- **공통 DTO**: `MemberInfoDto`, `BookInfoDto`, `CommentNotificationDto` 등
- **Feign Clients**: `MemberServiceClient`, `StoryServiceClient`, `ReactionServiceClient` 중앙 관리
- **응답 형식**: `ApiResponse<T>` 통일 (success, data, error 구조)
- **예외 처리**: `GlobalExceptionHandler`로 전역 에러 처리 일원화
- **에러 코드 체계**: C(Common), A(Auth), B(Book), M(Member), R(Reaction) 코드 분류

**Result**:
- 프론트엔드 연동 생산성 **200% 향상**
- 전체 MSA 모듈(43개 Task) 병렬 빌드 시간 **21초** 달성
- 코드 중복률 **70% 감소**

**Slide 9: 성공 사례 2 - Cross-Service Real-time Notification**

**Challenge**:
- `Reaction Service`(댓글 생성)와 `Story Service`(WebSocket 연결)가 물리적으로 분리됨
- 댓글 작성 시 소설 페이지에 실시간 알림을 보낼 수 없는 문제

**Solution**: **Feign-WebSocket Bridge** 아키텍처
1. **Reaction Service**: 댓글 저장 완료 후 `StoryServiceClient.notifyCommentCreated()` 호출 (Feign)
2. **Story Service**: 내부 API(`/internal/notify/comments`)로 수신
3. **Story Service**: `SimpMessagingTemplate`으로 `/topic/comments/{bookId}` 토픽에 브로드캐스트
4. **Frontend**: WebSocket 구독을 통해 실시간 알림 수신

**핵심 코드 예시**:
```java
// Reaction Service
@PostMapping
public ApiResponse<Long> createComment(@RequestBody CreateCommentDto dto) {
    Long commentId = reactionService.createComment(dto);

    // Story Service로 알림 전송
    try {
        storyServiceClient.notifyCommentCreated(
            new CommentNotificationDto(dto.getBookId(), commentId)
        );
    } catch (Exception e) {
        // 알림 실패해도 댓글 생성은 완료
        log.error("Failed to notify story service", e);
    }

    return ApiResponse.success(commentId);
}

// Story Service
@PostMapping("/internal/notify/comments")
public ApiResponse<Void> notifyCommentCreated(@RequestBody CommentNotificationDto dto) {
    messagingTemplate.convertAndSend(
        "/topic/comments/" + dto.getBookId(),
        dto
    );
    return ApiResponse.success();
}
```

**Result**:
- 서비스 물리적 분리에도 불구하고 끊김 없는 실시간 경험 제공
- 마이크로서비스 간 이벤트 기반 통신 패턴 확립
- 네트워크 지연 시간 평균 **50ms 이하**

---

## 4. 트러블 슈팅 (Troubleshooting) **[Highlight]**

**Slide 10: Issue 1 - N+1 문제와 성능 최적화**

**Problem**:
- 소설 목록 20개 조회 시, 작가 닉네임(Member Service)을 얻기 위해 **20번의 추가 네트워크 호출** 발생
- 각 소설마다 `MemberServiceClient.getMemberInfo(writerId)` 호출
- 응답 시간: **평균 300ms** (네트워크 지연으로 인한 성능 저하)

**Troubleshooting**: **Batch API & Aggregation**

**Step 1**: Batch API 설계
```java
// MemberServiceClient (Common Module)
@GetMapping("/internal/members/batch")
ApiResponse<Map<Long, MemberInfoDto>> getMembersBatch(
    @RequestParam List<Long> userIds
);

// Member Service
@GetMapping("/internal/members/batch")
public ApiResponse<Map<Long, MemberInfoDto>> getMembersBatch(
    @RequestParam List<Long> userIds
) {
    // SQL: SELECT * FROM users WHERE user_id IN (?, ?, ?, ...)
    List<Member> members = memberRepository.findAllById(userIds);

    Map<Long, MemberInfoDto> result = members.stream()
        .collect(Collectors.toMap(
            Member::getUserId,
            this::toDto
        ));

    return ApiResponse.success(result);
}
```

**Step 2**: Application Level Join
```java
// Story Service - BookQueryService
public Page<BookListResponseDto> getBookList(BookSearchCriteria criteria) {
    // 1. 소설 목록 조회 (MyBatis)
    Page<Book> books = bookQueryRepository.findAll(criteria);

    // 2. 작성자 ID 추출
    List<Long> writerIds = books.stream()
        .map(Book::getWriterId)
        .distinct()
        .collect(Collectors.toList());

    // 3. 닉네임 일괄 조회 (1번의 네트워크 호출)
    Map<Long, MemberInfoDto> memberMap =
        memberServiceClient.getMembersBatch(writerIds).getData();

    // 4. 메모리에서 조립
    List<BookListResponseDto> result = books.stream()
        .map(book -> {
            MemberInfoDto writer = memberMap.get(book.getWriterId());
            return BookListResponseDto.of(book, writer.getNickname());
        })
        .collect(Collectors.toList());

    return new PageImpl<>(result, books.getPageable(), books.getTotalElements());
}
```

**Result**:
- 외부 호출: **21회 → 2회** (95% 감소)
- 응답 속도: **300ms → 50ms** (6배 향상)
- 서버 부하: **80% 감소**

**Slide 11: Issue 2 - 분산 환경 데이터 조인 불가**

**Problem**:
- DB가 3개(`next_page_member`, `next_page_story`, `next_page_reaction`)로 분리
- SQL JOIN으로 데이터를 조합할 수 없음 (Cross-Database Join 불가)
- 예: "소설 목록과 작성자 닉네임을 함께 조회"가 불가능

**Troubleshooting**: **Application Level Join (Logical Reference)**

**AS-IS (Monolithic)**:
```sql
-- 모놀리식에서는 단일 DB에서 JOIN 가능
SELECT b.*, u.nickname
FROM books b
INNER JOIN users u ON b.writer_id = u.user_id
WHERE b.status = 'WRITING'
ORDER BY b.created_at DESC;
```

**TO-BE (MSA)**:
1. **DB FK 제거**: 물리적 외래키 대신 ID만 저장
2. **Java 애플리케이션 계층에서 조립**:
   - Story DB에서 소설 목록 조회
   - WriterId 추출 → Member Service에 Batch 요청
   - Java 메모리에서 데이터 조합 (Application Level Join)

**Architecture Change**:
```
[Before - Monolithic]
Database (Single) → SQL JOIN → Result

[After - MSA]
Story DB → Java App → Feign Call → Member DB
                    ↓
                Application Level Join (in-memory)
                    ↓
                  Result
```

**Result**:
- 데이터베이스 의존성 완전 제거
- 서비스 독립성 확보 (각 서비스는 자신의 DB만 관리)
- 서비스별 독립적인 스케일링 가능

**Slide 12: Issue 3 - 장애 전파 (Cascading Failure)**

**Problem**:
- Member Service 장애 시 Story Service의 소설 조회 기능까지 **500 에러** 발생
- 부분 장애가 전체 시스템으로 전파되는 현상 (Cascading Failure)
- 사용자는 소설을 읽을 수 없게 됨 (핵심 기능 마비)

**Troubleshooting**: **Resilience4j Circuit Breaker**

**Step 1**: 의존성 추가 및 설정
```yaml
# application.yml (Story Service)
resilience4j:
  circuitbreaker:
    instances:
      memberService:
        slidingWindowSize: 10              # 최근 10개 요청 기준
        failureRateThreshold: 50           # 실패율 50% 초과 시 Open
        waitDurationInOpenState: 10000     # Open 상태 유지 시간 (10초)
        permittedNumberOfCallsInHalfOpenState: 3  # Half-Open 시 테스트 호출 수
        automaticTransitionFromOpenToHalfOpenEnabled: true
```

**Step 2**: Fallback 메서드 구현
```java
@Service
public class BookQueryService {

    @CircuitBreaker(name = "memberService", fallbackMethod = "getMemberInfoFallback")
    public MemberInfoDto getMemberInfo(Long userId) {
        return memberServiceClient.getMemberInfo(userId).getData();
    }

    // Fallback: Member Service 장애 시 실행
    private MemberInfoDto getMemberInfoFallback(Long userId, Exception e) {
        log.warn("Member Service unavailable. Using fallback for userId: {}", userId, e);
        return MemberInfoDto.builder()
            .userId(userId)
            .nickname("익명 사용자")  // 기본값 반환
            .build();
    }
}
```

**Circuit Breaker 상태 전이**:
```
[Closed] → 정상 동작
    ↓ (실패율 50% 초과)
[Open] → 모든 요청 즉시 Fallback 실행 (10초간)
    ↓ (10초 경과)
[Half-Open] → 3개 테스트 요청 전송
    ↓ 성공 시: Closed / 실패 시: Open
```

**Result**:
- 부분 장애 시에도 시스템 가용성(Availability) 확보
- Member Service 다운 시에도 소설 읽기 기능 유지 (닉네임만 "익명 사용자"로 표시)
- 장애 전파 차단 및 빠른 복구 (Half-Open 상태에서 자동 재시도)

**Slide 13: Issue 4 - WebSocket 서비스 간 연동**

**Problem**:
- Reaction Service에서 댓글을 생성하지만, WebSocket 연결은 Story Service에 있음
- 두 서비스가 물리적으로 분리되어 실시간 알림 불가능

**Troubleshooting**: **Internal API + SimpMessagingTemplate**
```java
// Reaction Service → Story Service 호출
storyServiceClient.notifyCommentCreated(dto);

// Story Service → WebSocket 브로드캐스트
@PostMapping("/internal/notify/comments")
public ApiResponse<Void> notifyCommentCreated(@RequestBody CommentNotificationDto dto) {
    messagingTemplate.convertAndSend("/topic/comments/" + dto.getBookId(), dto);
    return ApiResponse.success();
}
```

**Result**:
- 서비스 간 실시간 이벤트 전파 성공
- 사용자는 서비스 분리를 인식하지 못함 (Seamless UX)

---

## 5. 회고 (Retrospective - 개인별 1개)

**Slide 14: Team Retrospective**

**정진호 (Team Leader - Story Service)**
> "**N+1 문제**는 JPA뿐만 아니라 **MSA 간 통신**에서도 발생한다는 것을 깨달았습니다. Batch API 설계를 통해 네트워크 호출을 95% 줄였고, CQRS 패턴으로 읽기/쓰기 성능을 모두 최적화할 수 있었습니다. WebSocket과 Feign을 연동하여 서비스 간 실시간 이벤트를 구현한 경험이 가장 뜻깊었습니다."

**김태형 (Sub Leader - Member Service)**
> "인증 로직을 **Gateway**로 일원화하니, 내부 서비스 개발 시 비즈니스 로직에만 집중할 수 있어 생산성이 크게 향상되었습니다. Refresh Token Rotation과 Circuit Breaker를 통해 보안과 안정성을 동시에 확보했으며, Soft Delete 패턴으로 데이터 복구 가능성을 높였습니다."

**정병진 (Developer - Reaction Service)**
> "계층형 댓글 구조를 설계하면서 **재귀적 데이터 모델**의 복잡성을 체감했습니다. Self Join과 Lazy Loading을 적절히 조합하여 성능을 최적화했고, 투표 시스템에서 동시성 문제를 해결하기 위해 Unique Index를 활용한 것이 인상 깊었습니다. 서비스 간 실시간 알림 연동을 통해 MSA의 진정한 가치를 배웠습니다."

**최현지 (Document Manager - QA & Documentation)**
> "API 명세서와 ERD를 작성하며 **데이터 정합성**의 중요성을 깨달았습니다. MSA 환경에서는 서비스 간 데이터 일관성을 보장하기 어렵기 때문에, Feign Client의 Fallback과 에러 처리가 얼마나 중요한지 배웠습니다. QA 과정에서 발견한 엣지 케이스들을 문서화하여 트러블슈팅 가이드로 만든 것이 팀에 큰 도움이 되었습니다."

---

## 6. 질의응답 (Q&A)

**Slide 15: Q&A**
- 기술 스택 선택 이유
- MSA 전환 과정에서의 어려움
- 향후 확장 계획 (Redis 캐싱, Kafka 메시징 등)
- 운영 경험 및 모니터링 전략

**Thank you for your attention!**

---

# [Complete Project Information]

이 섹션은 AI가 발표 자료를 더욱 풍부하게 작성할 수 있도록 제공하는 **프로젝트 전체 정보**입니다.

## A. Tech Stack Versions

### Backend
- **Java**: 17 (Amazon Corretto 권장)
- **Spring Boot**: 3.2.1
- **Spring Cloud**: 2023.0.0
  - Spring Cloud Gateway
  - Spring Cloud Netflix Eureka
  - Spring Cloud Config
  - Spring Cloud OpenFeign
- **Database**: MariaDB 10.6+
- **Persistence**:
  - JPA (Hibernate) - Command (쓰기)
  - MyBatis 3.5+ - Query (읽기)
- **Security**: Spring Security 6.2 + JWT
- **WebSocket**: Spring WebSocket + STOMP
- **Circuit Breaker**: Resilience4j
- **Build Tool**: Gradle 8.5

### Frontend
- **Framework**: Vue 3.3.0
- **Build Tool**: Vite 4.5.3
- **State Management**: Pinia 2.1.0
- **HTTP Client**: Axios 1.6.0
- **WebSocket**:
  - @stomp/stompjs 7.0.0
  - sockjs-client 1.6.1
- **Router**: Vue Router 4.2.0
- **Node.js**: 18.17.0

### Infrastructure
- **Service Discovery**: Netflix Eureka
- **API Gateway**: Spring Cloud Gateway (포트 8000)
- **Config Server**: Spring Cloud Config (포트 8888)
- **Load Balancing**: Spring Cloud LoadBalancer (Client-Side)

## B. Domain Services & Responsibilities

### Member Service (포트 8081)
**담당자**: 김태형 (Sub Leader)
**데이터베이스**: `next_page_member`

**주요 기능**:
- 회원가입, 로그인, 탈퇴
- JWT 토큰 발급 및 갱신 (Access Token 30분, Refresh Token 7일)
- Refresh Token Rotation (토큰 재발급 시 기존 토큰 무효화)
- 사용자 프로필 조회 및 활동 통계
- 역할 기반 접근 제어 (USER, ADMIN)
- Soft Delete (회원 탈퇴 시 논리 삭제)

**주요 Controller**:
- `AuthController` (`/api/auth`): 로그인, 토큰 갱신, 로그아웃
- `MemberController` (`/api/members`): 회원가입, 비밀번호 변경, 탈퇴
- `MemberQueryController` (`/api/members/me`): 마이페이지 조회
- `MemberInternalController` (`/internal/members`): Feign용 내부 API

**주요 Entity**:
- `Member`: 사용자 정보 (userId, email, nickname, password, role, status)
- `RefreshToken`: JWT 리프레시 토큰 관리 (userEmail, token, expiryDate)

**보안 특징**:
- BCrypt 패스워드 암호화
- JWT Access Token: 1시간 유효
- JWT Refresh Token: 7일 유효 (HttpOnly Cookie + 토큰 로테이션)
- Resilience4j Circuit Breaker 적용

### Story Service (포트 8082)
**담당자**: 정진호 (Team Leader)
**데이터베이스**: `next_page_story`

**주요 기능**:
- 릴레이 소설 생성 및 관리
- 문장 이어쓰기 (연속 작성 금지 규칙)
- 실시간 입력 상태 브로드캐스트 (WebSocket STOMP)
- 소설 자동 완결 (maxSequence 도달 시)
- 카테고리별 소설 검색 및 필터링
- CQRS 패턴 (JPA Command + MyBatis Query)

**주요 Controller**:
- `BookController` (Command): 소설 생성, 문장 추가, 편집, 삭제
- `BookQueryController` (Query): 소설 목록 조회, 상세 정보, 페이지네이션
- `CategoryController` (`/api/categories`): 카테고리 관리
- `TypingController` (WebSocket): 실시간 입력 상태
- `BookInternalController` (`/internal/books`): Feign용 내부 API

**주요 Entity**:
- `Book` (Aggregate Root): 릴레이 소설 메인 엔티티
  - 상태: WRITING, COMPLETED
  - 자동 완결 로직: maxSequence 도달 시
  - 연속 작성 방지 검증
- `Sentence` (Child Entity): 개별 문장
  - sequenceNo: 문장 순서
  - writerId: 작성자 ID (Member Service의 userId)
- `Category`: 소설 장르 분류 (ROMANCE, THRILLER, FANTASY, SF, MYSTERY, DAILY)

**도메인 로직 하이라이트**:
```java
// Book Aggregate의 핵심 비즈니스 규칙
public void validateWritingPossible(Long writerId, boolean isAdmin) {
    if (this.status != BookStatus.WRITING) {
        throw new BusinessException(ErrorCode.ALREADY_COMPLETED);
    }
    // 관리자는 연속 작성 가능, 일반 사용자는 불가
    if (!isAdmin && writerId.equals(this.lastWriterUserId)) {
        throw new BusinessException(ErrorCode.CONSECUTIVE_WRITING_NOT_ALLOWED);
    }
}

// 자동 완결 로직
public void updateStateAfterWriting(Long writerId) {
    this.lastWriterUserId = writerId;
    this.currentSequence++;

    // maxSequence 도달 시 자동 완결
    if (this.currentSequence >= this.maxSequence) {
        this.status = BookStatus.COMPLETED;
    }
}
```

**WebSocket 기능**:
- `/topic/typing/{bookId}`: 문장 작성 중 상태 브로드캐스트
- `/topic/comment-typing/{bookId}`: 댓글 작성 중 상태 브로드캐스트
- `/topic/sentences/{bookId}`: 새 문장 추가 알림
- `/topic/comments/{bookId}`: 새 댓글 추가 알림
- `/topic/books/{bookId}/status`: 소설 완결 알림
- `/topic/books/new`: 새 소설 생성 알림

### Reaction Service (포트 8083)
**담당자**: 정병진 (Developer)
**데이터베이스**: `next_page_reaction`

**주요 기능**:
- 댓글 작성, 수정, 삭제 (계층형 댓글 지원)
- 소설 및 문장 투표 (좋아요/싫어요)
- 투표 변경/취소 로직
- 실시간 댓글 알림 (Story Service로 이벤트 전송)

**주요 Controller**:
- `ReactionController` (Command): 댓글/투표 CRUD
- `ReactionQueryController` (Query): 댓글 목록 조회
- `ReactionInternalController` (`/internal/reactions`): Feign용 API

**주요 Entity**:
- `Comment`: 댓글 (Soft Delete, 계층형 구조)
  - parent-child 관계 (대댓글 지원)
  - writerId, bookId, content
  - updateContent() 메서드로 엔티티 검증

- `BookVote`: 소설 투표
  - 사용자당 소설 1개만 가능 (Unique Index: book_id + user_id)
  - LIKE/DISLIKE 전환 가능

- `SentenceVote`: 문장 투표
  - 사용자당 문장 1개만 가능 (Unique Index: sentence_id + user_id)

**투표 로직**:
```
1. 최초 투표 → 생성, 반영 (true)
2. 다른 타입으로 변경 → 업데이트 (true)
3. 동일 타입 재투표 → 삭제, 취소 (false)
```

**계층형 댓글 구조**:
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "parent_id")
private Comment parent;

@OneToMany(mappedBy = "parent")
private List<Comment> children = new ArrayList<>();
```

### Gateway Server (포트 8000)
**담당자**: 팀 전체
**역할**: API Gateway + 인증/인가 + Swagger 통합

**주요 기능**:
- 모든 클라이언트 요청의 단일 진입점
- JWT 검증 및 헤더 추가 (`X-User-Id`, `X-User-Role`)
- 마이크로서비스로 라우팅
- Swagger UI 통합 (3개 서비스 문서 통합)
- Circuit Breaker 적용

**라우팅 규칙**:
```yaml
/api/members/**, /api/auth/** → member-service (8081)
/api/books/**, /api/categories/** → story-service (8082)
/api/reactions/**, /api/comments/** → reaction-service (8083)
```

**보안 필터**:
- `JwtToHeaderFilter`: JWT 토큰을 Authorization 헤더로 변환
- `GatewayAuthenticationFilter`: 토큰 검증 및 사용자 정보 헤더 추가

### Discovery Server (Eureka, 포트 8761)
**역할**: 서비스 디스커버리
- 모든 마이크로서비스가 자동 등록
- 헬스 체크 기반 서비스 가용성 모니터링
- Client-Side Load Balancing 지원

### Config Server (포트 8888)
**역할**: 중앙 설정 관리
- 각 서비스의 환경별 설정 일괄 관리
- Git 기반 설정 저장소 연동
- JWT secret, DB URL 등 민감 정보 관리

## C. Database Architecture

### Database per Service 패턴
MSA의 핵심 원칙 중 하나로, 각 서비스는 자신만의 독립적인 데이터베이스를 소유합니다.

**3개의 독립 데이터베이스**:
1. `next_page_member` (Member Service)
2. `next_page_story` (Story Service)
3. `next_page_reaction` (Reaction Service)

**Physical FK 제거**:
- 서비스 간 참조는 ID(Logical Reference)로만 이루어짐
- 데이터 정합성은 Application Level에서 보장
- 예: Story의 `writerId`는 Member의 `userId`를 참조하지만, DB 레벨에서는 FK 없음

### Member Service DB (`next_page_member`)

**users 테이블**:
```sql
CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_email VARCHAR(100) NOT NULL UNIQUE,
    user_nicknm VARCHAR(50) NOT NULL UNIQUE,
    user_pw VARCHAR(255) NOT NULL,
    user_role VARCHAR(20) NOT NULL DEFAULT 'USER',  -- USER, ADMIN
    user_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE, DELETED
    left_at DATETIME NULL,  -- Soft Delete 시간
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

**refresh_token 테이블**:
```sql
CREATE TABLE refresh_token (
    user_email VARCHAR(100) PRIMARY KEY,
    token VARCHAR(500) NOT NULL,
    expiry_date DATETIME NOT NULL
);
```

### Story Service DB (`next_page_story`)

**categories 테이블**:
```sql
CREATE TABLE categories (
    category_id VARCHAR(20) PRIMARY KEY,  -- ROMANCE, THRILLER, FANTASY, SF, MYSTERY, DAILY
    category_nm VARCHAR(50) NOT NULL
);
```

**books 테이블**:
```sql
CREATE TABLE books (
    book_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    writer_id BIGINT NOT NULL,  -- Logical Reference (Member Service)
    category_id VARCHAR(20) NOT NULL,
    title VARCHAR(200) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'WRITING',  -- WRITING, COMPLETED
    current_sequence INT NOT NULL DEFAULT 0,
    max_sequence INT NOT NULL,
    last_writer_user_id BIGINT NULL,  -- 연속 작성 방지용
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(category_id),
    INDEX idx_status (status),
    INDEX idx_category_status (category_id, status),
    INDEX idx_created_at (created_at DESC)
);
```

**sentences 테이블**:
```sql
CREATE TABLE sentences (
    sentence_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id BIGINT NOT NULL,
    writer_id BIGINT NOT NULL,  -- Logical Reference (Member Service)
    sequence_no INT NOT NULL,
    content TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE CASCADE,
    UNIQUE KEY uk_book_sequence (book_id, sequence_no),
    INDEX idx_book_id (book_id),
    INDEX idx_writer_id (writer_id)
);
```

### Reaction Service DB (`next_page_reaction`)

**comments 테이블**:
```sql
CREATE TABLE comments (
    comment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id BIGINT NOT NULL,  -- Logical Reference (Story Service)
    writer_id BIGINT NOT NULL,  -- Logical Reference (Member Service)
    parent_id BIGINT NULL,  -- Self Join (대댓글)
    content TEXT NOT NULL,
    deleted_at DATETIME NULL,  -- Soft Delete
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES comments(comment_id),
    INDEX idx_book_id (book_id),
    INDEX idx_writer_id (writer_id),
    INDEX idx_parent_id (parent_id)
);
```

**book_votes 테이블**:
```sql
CREATE TABLE book_votes (
    vote_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id BIGINT NOT NULL,  -- Logical Reference (Story Service)
    user_id BIGINT NOT NULL,  -- Logical Reference (Member Service)
    vote_type VARCHAR(20) NOT NULL,  -- LIKE, DISLIKE
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_book_user (book_id, user_id),  -- 사용자당 소설 1개만 투표
    INDEX idx_book_id (book_id)
);
```

**sentence_votes 테이블**:
```sql
CREATE TABLE sentence_votes (
    vote_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sentence_id BIGINT NOT NULL,  -- Logical Reference (Story Service)
    user_id BIGINT NOT NULL,  -- Logical Reference (Member Service)
    vote_type VARCHAR(20) NOT NULL,  -- LIKE, DISLIKE
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_sentence_user (sentence_id, user_id),  -- 사용자당 문장 1개만 투표
    INDEX idx_sentence_id (sentence_id)
);
```

## D. Common Module (공유 라이브러리)

**위치**: `c:\Users\playdata2\Desktop\team2\next-page-msa\common-module`

**제공 기능**:

### Error Handling
- `ErrorCode` Enum: 40개 이상의 전역 에러 코드 정의
  - C001-C005: Common (입력 오류, 권한 오류 등)
  - A001-A005: Auth (로그인 실패, 토큰 오류)
  - B001-B008: Book/Story (완결 오류, 연속 작성 오류 등)
  - M001-M004: Member (중복, 미존재)
  - R001-R005: Reaction (댓글, 투표 오류)
- `BusinessException`: 비즈니스 예외
- `GlobalExceptionHandler`: 전역 예외 처리

### Authentication
- `SecurityUtil`: 현재 사용자 정보 추출 (Gateway 헤더에서 파싱)
- `JwtToHeaderFilter`: JWT 처리 필터
- `GatewayAuthenticationFilter`: Gateway용 인증 필터

### Feign Clients
- `MemberServiceClient`: Member Service 호출
- `StoryServiceClient`: Story Service 호출
- `ReactionServiceClient`: Reaction Service 호출

### Response
- `ApiResponse<T>`: 통일된 API 응답 포맷
  ```java
  public class ApiResponse<T> {
      private boolean success;
      private T data;
      private ErrorResponse error;
  }
  ```

### Entity
- `BaseEntity`: 생성/수정 시간 자동 관리 (@CreatedDate, @LastModifiedDate)

### Configuration
- `SwaggerConfig`: Swagger 통합 설정
- `ModelMapperConfig`: DTO 변환 설정

### 주요 DTO
```java
// Feign 통신용 DTO
- MemberInfoDto: 회원 기본 정보 (userId, email, nickname)
- BookInfoDto: 소설 기본 정보 (bookId, title, status)
- BookReactionInfoDto: 소설 반응 정보 (likeCount, dislikeCount, commentCount)
- CommentNotificationDto: 댓글 알림 (bookId, commentId)
- MemberStoryStatsDto: 사용자 통계 (booksCreated, sentencesWritten)
```

## E. Frontend Structure (Vue 3)

### 기술 스택
```json
{
  "framework": "Vue.js 3.3.0",
  "state": "Pinia 2.1.0",
  "router": "Vue Router 4.2.0",
  "http": "Axios 1.6.0",
  "realtime": ["STOMP (@stomp/stompjs 7.0.0)", "SockJS 1.6.1"],
  "bundler": "Vite 4.5.3",
  "nodejs": "18.17.0"
}
```

### 프로젝트 구조
```
frontend/
├── src/
│   ├── main.js                 # 앱 진입점 (Axios 인터셉터 설정)
│   ├── App.vue                 # 루트 컴포넌트
│   ├── router/
│   │   └── index.js            # 라우팅 설정
│   ├── stores/
│   │   └── auth.js             # Pinia 인증 스토어
│   ├── views/                  # 페이지 컴포넌트
│   │   ├── HomeView.vue        # 소설 목록 (필터/검색/무한스크롤)
│   │   ├── BookDetailView.vue  # 소설 상세 (읽기/쓰기/투표/댓글)
│   │   ├── CreateBookView.vue  # 소설 생성
│   │   └── MyPageView.vue      # 마이페이지
│   ├── components/             # 재사용 컴포넌트
│   │   ├── NavBar.vue          # 네비게이션 바
│   │   ├── AuthModals.vue      # 로그인/회원가입 모달
│   │   ├── CommentNode.vue     # 댓글 트리 컴포넌트
│   │   └── ToastNotification.vue
│   ├── utils/
│   │   └── toast.js            # 토스트 알림
│   └── assets/
│       └── css/main.css        # 스타일 (CSS 변수 활용)
└── package.json
```

### 주요 라우트
| 경로 | 컴포넌트 | 설명 |
|------|---------|------|
| `/` | HomeView | 소설 목록 + 필터/검색 |
| `/books/:id` | BookDetailView | 소설 상세 + 댓글/투표 |
| `/books/new` | CreateBookView | 새 소설 생성 |
| `/mypage` | MyPageView | 마이페이지 + 활동 통계 |

### Pinia 인증 스토어 (`auth.js`)
```javascript
// State
- user: 현재 사용자 정보
- accessToken: JWT 액세스 토큰
- showLoginModal / showSignupModal: 모달 표시 상태

// Actions
- login(email, password, autoLogin): 로그인
- signup(email, nickname, password): 회원가입
- logout(): 로그아웃
- checkAutoLogin(): 자동 로그인 (앱 시작 시)
- fetchUserProfile(): 사용자 정보 조회

// Getters
- isAuthenticated: 로그인 여부
```

### 주요 기능 구현

**1. HomeView - 소설 목록 (무한 스크롤)**
- 카테고리별 필터링
- 상태별 필터링 (WRITING/COMPLETED)
- 제목/작가 검색 (디바운싱 0.5초)
- Intersection Observer를 이용한 무한 스크롤

**2. BookDetailView - 소설 상세**
- Pull-to-refresh 기능
- 문장 목록 (편집/삭제 가능)
- 문장별 투표 (좋아요/싫어요)
- 실시간 입력 상태 표시 (WebSocket)
- 계층형 댓글 (대댓글 지원)
- 제목 수정 (작성자/관리자만)

**3. WebSocket 통신**
```javascript
// STOMP 연결
const stompClient = new Stomp.Client({
  webSocketFactory: () => new SockJS('http://localhost:8000/ws'),
  onConnect: () => {
    // /topic/typing/{bookId}로부터 입력 상태 수신
    // /topic/comment-typing/{bookId}로부터 댓글 입력 상태 수신
  }
})

// 전송
stompClient.publish({
  destination: '/app/typing/' + bookId,
  body: JSON.stringify({ userNickname, isTyping })
})
```

## F. Key API Endpoints

### Member Service
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/auth/login` | 로그인 |
| POST | `/api/auth/refresh` | 토큰 갱신 |
| POST | `/api/auth/logout` | 로그아웃 |
| POST | `/api/auth/signup` | 회원가입 |
| GET | `/api/members/me` | 마이페이지 조회 |
| PATCH | `/api/members/{userId}/password` | 비밀번호 변경 |
| DELETE | `/api/members/{userId}` | 회원 탈퇴 |
| GET | `/internal/members/{userId}` | 회원 정보 조회 (Feign) |
| GET | `/internal/members/batch` | 여러 회원 조회 (Feign) |

### Story Service
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/books` | 소설 생성 |
| GET | `/api/books` | 소설 목록 조회 (페이지네이션) |
| GET | `/api/books/{bookId}` | 소설 상세 조회 |
| GET | `/api/books/{bookId}/view` | 소설 뷰어 모드 |
| PATCH | `/api/books/{bookId}` | 소설 제목 수정 |
| DELETE | `/api/books/{bookId}` | 소설 삭제 |
| POST | `/api/books/{bookId}/sentences` | 문장 추가 |
| PATCH | `/api/books/{bookId}/sentences/{sentenceId}` | 문장 수정 |
| DELETE | `/api/books/{bookId}/sentences/{sentenceId}` | 문장 삭제 |
| POST | `/api/books/{bookId}/complete` | 소설 완결 |
| GET | `/api/books/mysentences` | 내 문장 조회 |
| GET | `/api/categories` | 카테고리 목록 |
| GET | `/internal/books/{bookId}` | 소설 정보 (Feign) |
| GET | `/internal/members/{userId}/stats` | 사용자 통계 (Feign) |

### Reaction Service
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/reactions/comments` | 댓글 작성 |
| PATCH | `/api/reactions/comments/{commentId}` | 댓글 수정 |
| DELETE | `/api/reactions/comments/{commentId}` | 댓글 삭제 |
| GET | `/api/reactions/comments/{bookId}` | 댓글 목록 조회 |
| GET | `/api/reactions/mycomments` | 내 댓글 조회 |
| POST | `/api/reactions/votes/books` | 소설 투표 |
| POST | `/api/reactions/votes/sentences/{sentenceId}` | 문장 투표 |

### WebSocket 엔드포인트
| Destination | 설명 |
|------------|------|
| `/app/typing/{bookId}` | 문장 작성 상태 전송 |
| `/topic/typing/{bookId}` | 문장 작성 상태 수신 |
| `/app/comment-typing/{bookId}` | 댓글 작성 상태 전송 |
| `/topic/comment-typing/{bookId}` | 댓글 작성 상태 수신 |

## G. Architecture Patterns

### 1. CQRS (Command Query Responsibility Segregation)
- **Command (쓰기)**: JPA로 도메인 로직 및 정합성 보장
- **Query (읽기)**: MyBatis로 복잡한 조회 쿼리 최적화

### 2. DDD (Domain-Driven Design)
- Book Aggregate: Story 도메인의 핵심 엔티티
- 비즈니스 규칙을 엔티티 내부에 캡슐화 (validateWritingPossible, updateStateAfterWriting)

### 3. Circuit Breaker Pattern
- Resilience4j로 장애 격리 및 Fallback 제공
- Member Service 다운 시에도 Story Service 정상 동작

### 4. API Gateway Pattern
- 모든 요청의 단일 진입점
- 인증/인가를 Gateway에서 중앙 처리
- 마이크로서비스는 비즈니스 로직에만 집중

### 5. Database per Service
- 각 서비스는 독립적인 데이터베이스 소유
- 서비스 간 참조는 ID(Logical Reference)로만 이루어짐
- Application Level Join으로 데이터 조합

### 6. Event-Driven Architecture
- WebSocket을 통한 실시간 이벤트 전파
- Feign Client로 서비스 간 이벤트 알림

## H. Performance Metrics

### N+1 문제 해결
- **Before**: 21번의 네트워크 호출 (소설 20개 + 닉네임 20번)
- **After**: 2번의 네트워크 호출 (소설 1번 + 닉네임 Batch 1번)
- **개선율**: 95% 감소

### 응답 속도 개선
- **Before**: 평균 300ms
- **After**: 평균 50ms
- **개선율**: 6배 향상

### 빌드 시간
- 전체 MSA 모듈(43개 Task) 병렬 빌드: **21초**

### WebSocket 지연 시간
- 평균 네트워크 지연: **50ms 이하**
- HTTP polling 대비 네트워크 부하: **95% 감소**

## I. Future Enhancements

### 1. 검색 최적화
- Elasticsearch 도입으로 전문 검색 기능 강화

### 2. 캐싱
- Redis 추가 (인기순 소설, 댓글 캐시)

### 3. 메시지 큐
- RabbitMQ/Kafka (비동기 이벤트 처리)

### 4. 추가 마이크로서비스
- Search Service
- Notification Service
- Analytics Service

### 5. 모바일 앱
- React Native/Flutter 기반 앱 개발

### 6. 이미지 서비스
- S3/CloudStorage 통합

---

**Last Updated**: 2026-01-16
**Project Status**: ✅ Production Ready
**Team**: Team 2 (정진호, 김태형, 정병진, 최현지)
**Project Repository**: [GitHub Link]
**Live Demo**: [Demo URL]
