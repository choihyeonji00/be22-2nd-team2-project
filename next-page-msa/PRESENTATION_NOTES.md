# Next Page MSA 프로젝트 발표 노트

## 📋 발표 전 체크리스트
- [ ] 노트북 충전 확인
- [ ] 프로젝트 데모 환경 실행 (모든 서비스 8761 Eureka 확인)
- [ ] 백업 자료 준비 (PDF, 동영상)
- [ ] 발표 시간 리허설 (20분 목표)
- [ ] 질문 예상 답변 숙지

---

## Slide 1: 타이틀 - Next Page MSA

### 발표 멘트
"안녕하세요, Team 2입니다. 오늘 저희가 소개할 프로젝트는 **Next Page**, 실시간 릴레이 소설 플랫폼입니다."

"여러분은 혼자 소설을 쓰면서 막막함을 느낀 적이 있으신가요? Next Page는 여러 작가가 **한 문장씩 이어가며** 함께 소설을 완성하는 협업 창작 플랫폼입니다."

"저희 팀은 이 플랫폼을 **Spring Cloud 기반 마이크로서비스 아키텍처로 전환**하여, 더 안정적이고 확장 가능한 시스템을 구축했습니다."

"**한 문장으로도 베스트셀러가 될 수 있다**는 메시지를 전달하고자 합니다."

### 핵심 키워드
- 실시간 릴레이 소설
- Spring Cloud MSA
- 한 문장의 가치

### 시간 배분: 30초

---

## Slide 2: 팀원 소개 (R&R)

### 발표 멘트
"먼저 저희 Team 2의 멤버들과 각자의 역할을 소개하겠습니다."

**정진호 (팀장)**
"팀장 정진호 님은 **MSA 아키텍처 설계**의 총괄을 맡으셨습니다. Story Service의 도메인 로직 구현, CQRS 패턴 적용, 그리고 N+1 문제 해결과 같은 **성능 최적화**를 주도하셨습니다. 특히 WebSocket 실시간 통신 구현에서 핵심 역할을 하셨습니다."

**김태형 (부팀장)**
"부팀장 김태형 님은 **인증/인가 시스템의 설계자**입니다. JWT 기반 인증, Refresh Token Rotation, Gateway 연동을 담당하셨고, Resilience4j Circuit Breaker를 활용한 **장애 대응 메커니즘**을 구축하셨습니다."

**정병진 (개발자)**
"정병진 님은 **사용자 경험**에 집중하셨습니다. 좋아요/싫어요 투표 시스템, 계층형 대댓글 구조 설계, 그리고 서비스 간 실시간 알림 연동을 구현하셨습니다. 특히 재귀적 댓글 구조의 성능 최적화에 많은 노력을 기울이셨습니다."

**최현지 (문서 관리자)**
"최현지 님은 **프로젝트 품질의 수호자**입니다. API 명세서, ERD, 시퀀스 다이어그램 등 모든 기술 문서를 관리하시고, 전체 기능에 대한 QA를 진행하셨습니다. 특히 엣지 케이스를 찾아내어 트러블슈팅 가이드로 만드신 것이 팀에 큰 도움이 되었습니다."

### 핵심 키워드
- 정진호: 아키텍처, 성능 최적화, WebSocket
- 김태형: 인증/인가, Gateway, Circuit Breaker
- 정병진: UX, 댓글/투표, 실시간 알림
- 최현지: 문서화, QA, 품질 관리

### 시간 배분: 1분 30초

---

## Slide 3: 프로젝트 개요

### 발표 멘트
"Next Page는 **여러 작가가 실시간으로 한 문장씩 이어 쓰며 하나의 소설을 완성하는 몰입형 집단 창작 플랫폼**입니다."

"저희 플랫폼의 핵심 가치는 네 가지입니다."

**① 실시간 협업**
"WebSocket을 활용하여 누군가 문장을 쓰고 있을 때 **타이핑 인디케이터**가 실시간으로 표시됩니다. 마치 구글 독스처럼, 누가 지금 참여하고 있는지 바로 알 수 있습니다."

**② 창의적 제약**
"**마지막 작성자는 연속으로 쓸 수 없다**는 규칙으로 공정한 참여를 보장합니다. 또한 최대 문장 수에 도달하면 자동으로 완결되어, 제약 속에서 창의성이 발휘됩니다."

**③ 커뮤니티 참여**
"좋아요/싫어요 투표, 계층형 댓글을 통해 독자들도 적극적으로 참여할 수 있습니다."

**④ 완성의 기쁨**
"함께 만든 하나의 완결된 작품을 볼 때의 성취감은 이루 말할 수 없습니다."

"이러한 플랫폼을 구현하면서 저희가 마주한 **기술적 도전 과제**는 다음과 같습니다:"

- **동시 접속**: 여러 사용자의 실시간 상태를 어떻게 동기화할 것인가?
- **데이터 일관성**: 서비스가 분리된 환경에서 데이터 정합성을 어떻게 보장할 것인가?
- **성능 최적화**: 높은 트래픽 상황에서도 빠른 응답 속도를 유지하려면?
- **장애 격리**: 부분 장애가 전체 시스템으로 전파되지 않게 하려면?

"이런 도전 과제들을 MSA로 어떻게 해결했는지 차례로 보여드리겠습니다."

### 핵심 키워드
- 실시간 협업 (WebSocket)
- 창의적 제약 (규칙 기반)
- 커뮤니티 참여 (투표, 댓글)
- 기술적 도전 (동시성, 정합성, 성능, 장애)

### 시간 배분: 2분

---

## Slide 4: 모놀리식 → MSA 전환 배경

### 발표 멘트
"저희는 처음에 **하나의 Spring Boot 애플리케이션**으로 개발을 시작했습니다. 모든 기능이 하나로 통합되어 있어 개발 초기에는 편했지만, 점차 문제점들이 드러났습니다."

**문제점 1: 배포 의존성**
"하나의 기능을 수정해도 **전체 시스템을 재기동**해야 했습니다. 예를 들어 댓글 기능 하나를 수정했는데, 회원 서비스까지 같이 내려갔다 올라와야 했죠."

**문제점 2: 장애 전파**
"댓글 기능에 버그가 생기면 **소설 읽기까지 불가능**해졌습니다. 부분 장애가 전체 시스템 다운으로 이어지는 상황이 발생했습니다."

**문제점 3: 확장성 제한**
"이벤트가 있어서 댓글이 폭주하면, 댓글 서버만 늘리고 싶은데 **전체 애플리케이션을 복제**해야 했습니다. 리소스가 낭비되는 구조였죠."

**문제점 4: 팀 협업의 어려움**
"한 명이 코드를 수정하면 다른 팀원의 작업과 충돌하는 경우가 잦았습니다. Git 머지 충돌도 빈번했고요."

---

"이런 문제들을 해결하기 위해 **마이크로서비스 아키텍처로 전환**을 결정했습니다."

**해결책 1: 서비스 분리**
"3개의 도메인 서비스(Member, Story, Reaction)로 분리하고, 각각 독립적인 데이터베이스를 갖도록 했습니다."

**해결책 2: 독립 배포**
"이제는 댓글 기능만 수정하면 Reaction Service만 재배포하면 됩니다. **무중단 배포**가 가능해졌습니다."

**해결책 3: 장애 격리**
"Circuit Breaker를 적용해 Member Service가 다운되어도 Story Service는 정상 동작합니다. Fallback으로 '익명 사용자'를 표시하면서요."

**해결책 4: 독립 확장**
"댓글이 많아지면 Reaction Service만 스케일 아웃하면 됩니다. 효율적인 리소스 사용이 가능해졌죠."

**해결책 5: 팀 자율성**
"각 팀원이 담당 서비스를 독립적으로 개발하고 배포할 수 있게 되었습니다."

### 핵심 키워드
- AS-IS: 배포 의존성, 장애 전파, 확장 제한
- TO-BE: 독립 배포, 장애 격리, 독립 확장

### 데모 포인트
- Eureka Dashboard에서 3개 서비스가 독립적으로 등록된 모습 보여주기

### 시간 배분: 2분

---

## Slide 5: 시스템 아키텍처

### 발표 멘트
"Next Page MSA는 **6개의 핵심 서비스**로 구성되어 있습니다. Spring Cloud 생태계를 최대한 활용하여 확장 가능하고 탄력적인 시스템을 구축했습니다."

**API Gateway (포트 8000)**
"모든 클라이언트 요청의 **단일 진입점**입니다. 여기서 JWT 토큰을 검증하고, 사용자 정보를 헤더에 담아서 각 마이크로서비스로 라우팅합니다."

**Member Service (포트 8081)**
"회원 가입, 로그인, JWT 토큰 발급을 담당합니다. Refresh Token Rotation으로 보안을 강화했고, Soft Delete로 데이터 복구 가능성을 보장합니다."

**Story Service (포트 8082)**
"릴레이 소설의 핵심입니다. 소설 생성, 문장 이어쓰기, 자동 완결 로직이 여기 있습니다. 또한 WebSocket을 통한 실시간 통신의 허브 역할도 합니다."

**Reaction Service (포트 8083)**
"댓글과 투표를 관리합니다. 계층형 댓글 구조로 무한 대댓글을 지원하고, Unique Index로 중복 투표를 방지합니다."

**Discovery Server (포트 8761 - Eureka)**
"Netflix Eureka 기반으로 모든 서비스가 자동으로 등록되고, 서로를 찾을 수 있게 합니다. 헬스 체크로 장애 서비스는 자동으로 제외됩니다."

**Config Server (포트 8888)**
"Git 기반 중앙 설정 관리입니다. JWT Secret, DB 접속 정보 같은 민감한 정보를 한 곳에서 관리합니다."

**데이터베이스**
"Database per Service 패턴을 적용하여 각 서비스가 독립적인 MariaDB를 갖습니다. `next_page_member`, `next_page_story`, `next_page_reaction` 이렇게 3개로 분리되어 있습니다."

**Common Module**
"3개 서비스의 공통 코드를 라이브러리로 분리했습니다. Feign Client, DTO, Exception Handler가 여기 있어서 코드 중복을 70% 줄였습니다."

### 핵심 키워드
- 6개 서비스 (Gateway, 3개 도메인, 2개 인프라)
- Database per Service
- Common Module로 중복 제거

### 데모 포인트
- 아키텍처 다이어그램 화면에 표시
- Eureka Dashboard 실행 화면 (http://localhost:8761)

### 시간 배분: 2분

---

## Slide 6: 핵심 기능 1 - 릴레이 소설 창작

### 발표 멘트
"Next Page의 가장 핵심적인 기능은 **릴레이 소설 창작 시스템**입니다. 이 시스템의 가장 중요한 특징은 **공정한 참여와 창의적 제약을 동시에 구현**한다는 것입니다."

**규칙 1: 연속 작성 방지**
"마지막 작성자는 다음 문장을 쓸 수 없습니다. 한 사람이 독점하지 않고, 모두가 참여할 기회를 갖도록 했습니다. 단, 관리자는 예외입니다."

**규칙 2: 마지막 문장만 수정/삭제**
"자기가 쓴 마지막 문장만 편집할 수 있습니다. 이미 다음 사람이 이어 쓴 문장은 수정 불가입니다. 이건 소설의 흐름을 보호하기 위함입니다."

**규칙 3: 자동 완결**
"소설 생성 시 최대 문장 수를 정하는데, 이 수에 도달하면 자동으로 COMPLETED 상태로 전환됩니다. 제약 속에서 완결의 기쁨을 느끼게 하는 거죠."

**규칙 4: 수동 완결**
"소설 작성자는 언제든 직접 완결 처리를 할 수 있습니다. 예상보다 일찍 좋은 엔딩이 나오면 그때 끝낼 수 있는 거죠."

---

"이런 규칙들을 **DDD 패턴**으로 구현했습니다."

**Book Aggregate**
"Book 엔티티가 Aggregate Root로서 모든 비즈니스 규칙을 캡슐화합니다."

```java
// 작성 가능 여부 검증
public void validateWritingPossible(Long writerId, boolean isAdmin) {
    // 완결된 소설은 쓸 수 없음
    if (this.status != BookStatus.WRITING) {
        throw new BusinessException(ErrorCode.ALREADY_COMPLETED);
    }
    // 연속 작성 방지 (관리자 제외)
    if (!isAdmin && writerId.equals(this.lastWriterUserId)) {
        throw new BusinessException(ErrorCode.CONSECUTIVE_WRITING_NOT_ALLOWED);
    }
}
```

**동시성 제어**
"여러 사용자가 동시에 문장을 추가하려 할 때를 대비해, **Unique Index(book_id, sequence_no)**를 적용했습니다. 데이터베이스 레벨에서 순서를 보장하는 거죠."

### 핵심 키워드
- 연속 작성 방지
- DDD (Book Aggregate)
- 동시성 제어 (Unique Index)

### 데모 포인트
- 실제 소설에서 연속 작성 시도 → 에러 메시지 표시

### 시간 배분: 2분

---

## Slide 7: 핵심 기능 2 - 실시간 WebSocket

### 발표 멘트
"Next Page의 두 번째 핵심은 **실시간 협업 경험**입니다. STOMP 프로토콜을 활용한 Pub/Sub 메시징으로 끊김 없는 실시간 경험을 제공합니다."

**실시간 타이핑 인디케이터**
"누군가 문장을 작성 중일 때, 다른 사용자들의 화면에 **'OOO님이 작성 중입니다...'** 라는 메시지가 실시간으로 표시됩니다."
- 클라이언트: `/app/typing/{bookId}`로 입력 상태 전송
- 서버: `/topic/typing/{bookId}`로 모든 구독자에게 브로드캐스트

**라이브 업데이트**
"새 문장이 추가되면 **페이지 새로고침 없이** 모든 사용자의 화면에 즉시 나타납니다."

**댓글 알림**
"댓글이 달리면 소설을 보고 있는 모든 사용자에게 실시간으로 알림이 갑니다. 이건 **Feign-WebSocket Bridge** 덕분입니다. Reaction Service에서 댓글을 저장하고, Story Service를 통해 WebSocket으로 알림을 보내는 구조입니다."

**투표 결과 실시간 반영**
"좋아요를 누르면 다른 사용자들의 화면에도 숫자가 바로 바뀝니다."

---

**성능 지표**
"HTTP polling 방식과 비교했을 때:"
- 네트워크 부하: **95% 감소**
- 평균 지연 시간: **50ms 이하**

"이게 가능한 이유는 WebSocket이 연결을 유지하면서 필요할 때만 데이터를 보내기 때문입니다. HTTP는 매번 요청을 새로 만들어야 하지만, WebSocket은 한 번 연결하면 끝이죠."

### 핵심 키워드
- STOMP (Pub/Sub)
- 타이핑 인디케이터
- 라이브 업데이트
- 95% 네트워크 부하 감소

### 데모 포인트
- 두 개의 브라우저 창에서 동시에 타이핑 → 실시간 인디케이터 표시
- 한쪽에서 문장 추가 → 다른 쪽에 즉시 표시

### 시간 배분: 2분

---

## Slide 8: 핵심 기능 3 - 커뮤니티 (댓글/투표)

### 발표 멘트
"세 번째 핵심 기능은 **커뮤니티 참여**입니다."

**계층형 댓글 시스템**
"무한 대댓글을 지원합니다. Self-Referencing 구조로 parent-child 관계를 표현했습니다."

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "parent_id")
private Comment parent;

@OneToMany(mappedBy = "parent")
private List<Comment> children = new ArrayList<>();
```

"**Lazy Loading**으로 필요한 댓글만 로드해서 성능을 최적화했습니다. 100개 댓글이 있어도 처음엔 부모 댓글만 로드하고, 대댓글은 펼칠 때 로드하는 식이죠."

**Soft Delete**
"삭제된 댓글도 구조를 유지하기 위해 **'삭제된 댓글입니다'**로 표시합니다. 완전히 지우면 대댓글들이 고아가 되니까요."

---

**투표 시스템**
"소설 단위, 문장 단위로 좋아요/싫어요를 누를 수 있습니다."

**중복 방지**
"Unique Index로 한 사용자가 같은 소설/문장에 중복 투표를 할 수 없게 막았습니다."
- `UNIQUE KEY uk_book_user (book_id, user_id)`
- `UNIQUE KEY uk_sentence_user (sentence_id, user_id)`

**투표 로직**
1. 최초 투표 → 생성 (LIKE 반영)
2. LIKE 상태에서 DISLIKE 클릭 → 업데이트 (DISLIKE로 변경)
3. LIKE 상태에서 LIKE 재클릭 → 삭제 (투표 취소)

### 핵심 키워드
- 계층형 댓글 (Self-Referencing)
- Lazy Loading
- Soft Delete
- Unique Index (중복 방지)

### 데모 포인트
- 대댓글 작성 및 트리 구조 표시
- 투표 클릭 → 실시간 카운트 변경

### 시간 배분: 1분 30초

---

## Slide 9: 시스템 기능 1 - 보안

### 발표 멘트
"이제 시스템 레벨 기능을 보겠습니다. 첫 번째는 **다층 보안 체계**입니다."

**JWT 토큰 관리**
"**Access Token**은 30분 동안 유효하고, Bearer 방식으로 헤더에 담아 전송합니다."
"**Refresh Token**은 7일 동안 유효하며, HttpOnly Cookie에 저장해서 XSS 공격을 막습니다."

**Refresh Token Rotation**
"토큰을 갱신할 때마다 기존 Refresh Token을 무효화하고 새로운 토큰을 발급합니다. 이렇게 하면 토큰이 탈취되어도 한 번만 사용 가능하기 때문에 보안이 강화됩니다."

---

**Gateway 기반 인증**
"모든 인증 로직을 **API Gateway에서 중앙 처리**합니다."

1. 클라이언트가 JWT를 헤더에 담아 요청
2. Gateway에서 JWT 검증
3. 파싱 결과를 `X-User-Id`, `X-User-Role` 헤더로 변환
4. 마이크로서비스로 전달

"이렇게 하면 **각 마이크로서비스는 인증 로직 없이 비즈니스 로직에만 집중**할 수 있습니다."

---

**암호화 및 보호**
- **BCrypt**: 패스워드를 강력하게 암호화 (Salt + Hash)
- **Soft Delete**: 회원 탈퇴 시 `user_status = 'DELETED'`, `left_at = NOW()`로 설정. 완전 삭제는 하지 않아서 복구 가능
- **Config Server**: JWT Secret 같은 민감 정보는 Git에 암호화하여 저장

### 핵심 키워드
- JWT (Access 30분, Refresh 7일)
- Token Rotation
- Gateway 중앙 인증
- BCrypt, Soft Delete

### 시간 배분: 1분 30초

---

## Slide 10: 시스템 기능 2 - 장애 격리 (Circuit Breaker)

### 발표 멘트
"두 번째 시스템 기능은 **장애 격리**입니다. MSA의 가장 큰 리스크 중 하나가 **Cascading Failure**, 즉 장애 전파입니다."

**문제 상황**
"Member Service가 다운되면, Story Service에서 작가 닉네임을 조회할 수 없습니다. 그러면 소설 목록 조회 자체가 실패하고, 사용자는 소설을 읽을 수 없게 됩니다. 이게 장애 전파입니다."

**해결책: Resilience4j Circuit Breaker**
"Circuit Breaker는 전기 차단기처럼 장애가 감지되면 호출을 차단하고, Fallback을 실행합니다."

---

**3가지 상태**

**1. Closed (정상)**
"모든 요청이 정상적으로 처리됩니다. 최근 10개 요청 중 실패율이 50% 미만이면 이 상태를 유지합니다."

**2. Open (차단)**
"실패율이 50%를 초과하면 **모든 요청을 즉시 Fallback**으로 처리합니다. Member Service에 대한 호출을 완전히 차단하는 거죠. 이 상태는 10초간 유지됩니다."

**3. Half-Open (테스트)**
"10초 경과 후 자동으로 Half-Open 상태로 전환됩니다. 이때 3개의 테스트 요청을 보내서 Member Service가 복구되었는지 확인합니다."
- 성공하면 → Closed (정상 복귀)
- 실패하면 → Open (다시 차단)

---

**Fallback 메커니즘**
```java
private MemberInfoDto getMemberInfoFallback(Long userId, Exception e) {
    return MemberInfoDto.builder()
        .userId(userId)
        .nickname("익명 사용자")  // 기본값 반환
        .build();
}
```

"Member Service가 다운되어도 **'익명 사용자'**를 표시하면서 소설 읽기는 계속 가능합니다."

**결과**
- 부분 장애 시에도 시스템 가용성 확보
- Member Service 다운 시에도 핵심 기능(소설 읽기) 유지
- 장애 전파 차단 및 빠른 자동 복구

### 핵심 키워드
- Cascading Failure
- Resilience4j
- Closed → Open → Half-Open
- Fallback (익명 사용자)

### 데모 포인트
- Member Service 중단 → 소설 목록에서 '익명 사용자' 표시

### 시간 배분: 2분

---

## Slide 11: 시스템 기능 3 - CQRS 패턴

### 발표 멘트
"세 번째는 **CQRS 패턴**입니다. Command Query Responsibility Segregation, 즉 **읽기와 쓰기를 분리**하는 패턴입니다."

**왜 분리했나?**
"쓰기 작업은 **정합성**이 중요하고, 읽기 작업은 **성능**이 중요합니다. 하나의 도구로 둘 다 최적화하기는 어렵죠."

**쓰기 (Command) - JPA**
"문장 추가, 소설 생성, 댓글 작성 같은 **쓰기 작업은 JPA**를 사용합니다."
- 장점: 도메인 로직을 엔티티에 캡슐화, 트랜잭션 관리, 자동 검증

**읽기 (Query) - MyBatis**
"소설 목록 조회, 검색, 필터링 같은 **읽기 작업은 MyBatis**를 사용합니다."
- 장점: 복잡한 JOIN 쿼리 최적화, 동적 SQL (if, choose), 페이지네이션 성능

**예시**
"소설 목록을 조회할 때 카테고리, 상태, 검색어, 정렬 기준 등을 조합하는 동적 쿼리를 MyBatis로 작성했습니다."

```xml
<select id="findBookList" resultType="BookDto">
    SELECT * FROM books
    WHERE 1=1
    <if test="categoryId != null">
        AND category_id = #{categoryId}
    </if>
    <if test="status != null">
        AND status = #{status}
    </if>
    <if test="keyword != null">
        AND title LIKE CONCAT('%', #{keyword}, '%')
    </if>
    ORDER BY created_at DESC
    LIMIT #{offset}, #{limit}
</select>
```

"이렇게 하면 **읽기 성능을 유지하면서도 데이터 정합성을 보장**할 수 있습니다."

### 핵심 키워드
- CQRS (읽기/쓰기 분리)
- JPA (쓰기, 정합성)
- MyBatis (읽기, 성능)

### 시간 배분: 1분 30초

---

## Slide 12: 기술적 차별화

### 발표 멘트
"이제 저희 프로젝트가 **타 서비스와 어떻게 다른지** 말씀드리겠습니다."

**① Event-Driven Architecture**
"단순 게시판이 아니라 **WebSocket & Event-Driven 기반의 동적 협업 도구**입니다."
"STOMP 프로토콜의 Pub/Sub 메시징으로 서비스 간 실시간 이벤트 연동을 구현했습니다."

**② Hybrid Persistence (CQRS)**
"쓰기는 JPA로 도메인 정합성을 보장하고, 읽기는 MyBatis로 복잡한 조회 쿼리를 최적화했습니다. 두 가지의 장점을 모두 취한 거죠."

**③ Gateway Centric Authentication**
"인증 로직을 Gateway로 이관해서 마이크로서비스는 순수 비즈니스 로직에만 집중할 수 있습니다. JWT 파싱 결과를 `X-User-Id`, `X-User-Role` 헤더로 전달하는 방식입니다."

**④ Common Module 표준화**
"3개 서비스 간 중복 코드를 제거하여 프론트엔드 연동 생산성을 **200% 향상**시켰습니다. 전체 MSA 모듈(43개 Task) 빌드 시간을 **21초**로 단축했습니다."

### 핵심 키워드
- Event-Driven
- CQRS
- Gateway 중앙 인증
- Common Module (생산성 200% 향상, 빌드 21초)

### 시간 배분: 1분

---

## Slide 13: Database per Service

### 발표 멘트
"MSA의 핵심 원칙 중 하나가 **Database per Service**입니다. 각 서비스가 자신만의 독립적인 데이터베이스를 가진다는 뜻이죠."

**3개의 독립 DB**
- `next_page_member`: Member Service 전용
- `next_page_story`: Story Service 전용
- `next_page_reaction`: Reaction Service 전용

**Physical FK 제거**
"서비스 간 참조는 **Logical Reference (ID만 저장)**로만 이루어집니다."
"예를 들어 Story의 `writerId`는 Member의 `userId`를 참조하지만, DB 레벨에서는 Foreign Key가 없습니다."

**장점**
1. **서비스 독립성**: 각 서비스가 자신의 DB만 관리
2. **독립 스케일링**: Story DB만 확장하거나, Member DB만 확장 가능
3. **기술 다양성**: 원한다면 서비스마다 다른 DB를 쓸 수도 있음 (PostgreSQL, MongoDB 등)

**단점과 극복**
"단점은 SQL JOIN이 불가능하다는 것입니다. 이건 **Application Level Join**으로 해결했습니다. 이건 트러블슈팅에서 자세히 설명드리겠습니다."

### 핵심 키워드
- Database per Service
- Logical Reference
- 서비스 독립성
- 독립 스케일링

### 시간 배분: 1분

---

## Slide 14: 성공 사례 1 - Common Module

### 발표 멘트
"이제 **주요 성과와 성공 사례**를 보여드리겠습니다. 첫 번째는 **Common Module 구축**입니다."

**Challenge (문제)**
"초기에는 3개 서비스마다 똑같은 코드를 복사-붙여넣기했습니다."
- `ApiResponse<T>` 클래스를 3번 만듦
- Feign Client도 3번 정의
- Exception Handler도 3번 작성

"이러면 뭐가 문제일까요? **한 곳을 수정하면 3곳 모두 수정**해야 합니다. 실수로 한 곳을 빼먹으면 버그가 생기고요."

**Solution (해결)**
"Common Module 라이브러리를 만들어서 모든 공통 코드를 중앙 관리했습니다."

1. **공통 DTO**: `MemberInfoDto`, `BookInfoDto`, `CommentNotificationDto` 등
2. **Feign Clients**: `MemberServiceClient`, `StoryServiceClient`, `ReactionServiceClient`
3. **응답 형식**: `ApiResponse<T>` 통일 (success, data, error 구조)
4. **예외 처리**: `GlobalExceptionHandler`로 전역 에러 처리 일원화
5. **에러 코드 체계**: C(Common), A(Auth), B(Book), M(Member), R(Reaction) 코드 분류

**Result (결과)**
- 프론트엔드 연동 생산성: **200% 향상**
  - 이유: 응답 포맷이 통일되어 프론트엔드가 한 번만 파싱 로직을 작성하면 됨
- 빌드 시간: 43개 Task 병렬 빌드 **21초**
  - 이유: 공통 모듈을 먼저 빌드하고, 나머지를 병렬로 빌드
- 코드 중복률: **70% 감소**

### 핵심 키워드
- Common Module
- 코드 중복 70% 감소
- 생산성 200% 향상
- 빌드 21초

### 시간 배분: 1분 30초

---

## Slide 15: 성공 사례 2 - Feign-WebSocket Bridge

### 발표 멘트
"두 번째 성공 사례는 **서비스 간 실시간 알림 연동**입니다."

**Challenge (문제)**
"Reaction Service에서 댓글을 생성하는데, WebSocket 연결은 Story Service에 있습니다."
"두 서비스가 물리적으로 분리되어 있어서 실시간 알림을 보낼 수 없었습니다."

**Solution (해결) - Feign-WebSocket Bridge**

**Step 1**: Reaction Service에서 댓글 저장
```java
@PostMapping
public ApiResponse<Long> createComment(@RequestBody CreateCommentDto dto) {
    Long commentId = reactionService.createComment(dto);
    // 알림 전송 (비동기)
    storyServiceClient.notifyCommentCreated(dto);
    return ApiResponse.success(commentId);
}
```

**Step 2**: Story Service의 Internal API
```java
@PostMapping("/internal/notify/comments")
public ApiResponse<Void> notifyCommentCreated(@RequestBody dto) {
    // WebSocket 브로드캐스트
    messagingTemplate.convertAndSend("/topic/comments/" + dto.getBookId(), dto);
    return ApiResponse.success();
}
```

**Step 3**: Frontend에서 WebSocket 구독
"프론트엔드는 `/topic/comments/{bookId}`를 구독하고 있다가, 알림이 오면 UI를 업데이트합니다."

**핵심 포인트**
"알림 전송이 실패해도 댓글 생성은 완료됩니다. try-catch로 감싸서 예외를 로그만 남기고 넘어갑니다. 이게 **느슨한 결합**의 핵심입니다."

**Result (결과)**
- 서비스 물리적 분리에도 불구하고 끊김 없는 실시간 경험
- 마이크로서비스 간 이벤트 기반 통신 패턴 확립
- 네트워크 지연 시간 평균 **50ms 이하**

### 핵심 키워드
- Feign-WebSocket Bridge
- 서비스 간 실시간 이벤트
- 느슨한 결합
- 50ms 지연

### 데모 포인트
- 댓글 작성 → 실시간 알림 표시

### 시간 배분: 2분

---

## Slide 16: 트러블슈팅 1 - N+1 문제

### 발표 멘트
"이제 **트러블슈팅** 파트입니다. 개발 과정에서 겪은 실제 문제와 해결 과정을 공유하겠습니다."

**Problem (문제)**
"소설 목록 20개를 조회하는데, 응답 시간이 **평균 300ms**나 걸렸습니다. 왜일까요?"

"각 소설마다 작가 닉네임을 얻기 위해 Member Service를 호출했기 때문입니다."
- 1번: 소설 목록 조회 (Story Service)
- 20번: 각 소설의 작가 닉네임 조회 (Member Service)
- 총 **21번의 네트워크 호출**

"이게 바로 **N+1 문제**입니다. JPA에서만 발생하는 게 아니라 **MSA 간 통신에서도 발생**합니다."

---

**Troubleshooting (해결)**

**Step 1: Batch API 설계**
"Member Service에 여러 사용자를 한 번에 조회하는 API를 만들었습니다."

```java
// MemberServiceClient (Common Module)
@GetMapping("/internal/members/batch")
ApiResponse<Map<Long, MemberInfoDto>> getMembersBatch(
    @RequestParam List<Long> userIds
);

// Member Service 구현
public ApiResponse<Map<Long, MemberInfoDto>> getMembersBatch(List<Long> userIds) {
    // SQL: SELECT * FROM users WHERE user_id IN (?, ?, ?, ...)
    List<Member> members = memberRepository.findAllById(userIds);

    Map<Long, MemberInfoDto> result = members.stream()
        .collect(Collectors.toMap(Member::getUserId, this::toDto));

    return ApiResponse.success(result);
}
```

**Step 2: Application Level Join**
"Story Service에서 작성자 ID를 모아서 한 번에 조회하고, Java 메모리에서 조립합니다."

```java
public Page<BookListResponseDto> getBookList(criteria) {
    // 1. 소설 목록 조회
    Page<Book> books = bookQueryRepository.findAll(criteria);

    // 2. 작성자 ID 추출
    List<Long> writerIds = books.stream()
        .map(Book::getWriterId)
        .distinct()
        .collect(Collectors.toList());

    // 3. 닉네임 일괄 조회 (1번의 네트워크 호출!)
    Map<Long, MemberInfoDto> memberMap =
        memberServiceClient.getMembersBatch(writerIds).getData();

    // 4. 메모리에서 조립
    List<BookListResponseDto> result = books.stream()
        .map(book -> BookListResponseDto.of(book,
            memberMap.get(book.getWriterId()).getNickname()))
        .collect(Collectors.toList());

    return new PageImpl<>(result, pageable, total);
}
```

**Result (결과)**
- 네트워크 호출: **21회 → 2회** (95% 감소)
- 응답 속도: **300ms → 50ms** (6배 향상)
- CPU/네트워크 부하: **80% 감소**

### 핵심 키워드
- N+1 문제 (MSA 버전)
- Batch API
- Application Level Join
- 95% 호출 감소, 6배 성능 향상

### 시간 배분: 2분 30초

---

## Slide 17: 트러블슈팅 2 - 분산 환경 데이터 조인

### 발표 멘트
"두 번째 트러블슈팅은 **분산 환경에서 데이터 조인** 문제입니다."

**Problem (문제)**
"DB가 3개(`next_page_member`, `next_page_story`, `next_page_reaction`)로 분리되어 있어서 SQL JOIN을 할 수 없습니다."

"모놀리식에서는 이렇게 했었죠:"
```sql
SELECT b.*, u.nickname
FROM books b
INNER JOIN users u ON b.writer_id = u.user_id
WHERE b.status = 'WRITING';
```

"하지만 MSA에서는 books와 users가 **다른 데이터베이스**에 있어서 불가능합니다."

---

**Troubleshooting (해결) - Application Level Join**

**Step 1: Physical FK 제거**
"데이터베이스 레벨의 외래키를 제거하고, **ID만 저장**합니다."
- Story의 `writerId`: Member의 `userId`를 참조하지만 FK 없음

**Step 2: Java에서 조립**
1. Story DB에서 소설 목록 조회
2. WriterId 추출
3. Member Service에 Batch 요청
4. Java 메모리에서 데이터 조합

**Architecture Change**
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

**Result (결과)**
- 데이터베이스 의존성 완전 제거
- 서비스 독립성 확보 (각 서비스는 자신의 DB만 관리)
- 서비스별 독립적인 스케일링 가능

**Trade-off**
"단점은 네트워크 호출이 추가된다는 것입니다. 하지만 Batch API로 최소화했고, 서비스 독립성이라는 더 큰 이점을 얻었습니다."

### 핵심 키워드
- Cross-Database Join 불가
- Application Level Join
- Physical FK 제거
- 서비스 독립성

### 시간 배분: 2분

---

## Slide 18: 트러블슈팅 3 - 장애 전파 (Circuit Breaker)

### 발표 멘트
"세 번째는 **장애 전파(Cascading Failure)** 문제입니다. 이미 시스템 기능에서 언급했지만, 실제 겪은 문제로 다시 설명드리겠습니다."

**Problem (문제)**
"테스트 중에 Member Service를 일부러 중단시켰습니다. 그랬더니 Story Service의 소설 조회 기능까지 **500 에러**가 발생했습니다."

"이유는 간단합니다. 소설 목록을 보여줄 때 작가 닉네임을 Member Service에서 가져오는데, Member Service가 죽어서 타임아웃이 발생한 겁니다."

"사용자 입장에서는 **소설을 읽을 수 없게** 되는 최악의 상황이죠."

---

**Troubleshooting (해결) - Resilience4j Circuit Breaker**

**설정**
```yaml
resilience4j:
  circuitbreaker:
    instances:
      memberService:
        slidingWindowSize: 10              # 최근 10개 요청 기준
        failureRateThreshold: 50           # 실패율 50% 초과 시 Open
        waitDurationInOpenState: 10000     # Open 상태 10초 유지
        permittedNumberOfCallsInHalfOpenState: 3  # Half-Open 테스트 3회
```

**Fallback 구현**
```java
@CircuitBreaker(name = "memberService", fallbackMethod = "getMemberInfoFallback")
public MemberInfoDto getMemberInfo(Long userId) {
    return memberServiceClient.getMemberInfo(userId).getData();
}

private MemberInfoDto getMemberInfoFallback(Long userId, Exception e) {
    log.warn("Member Service unavailable. Using fallback for userId: {}", userId);
    return MemberInfoDto.builder()
        .userId(userId)
        .nickname("익명 사용자")  // 기본값
        .build();
}
```

**상태 전이**
1. **Closed (정상)**: 실패율 50% 미만 → 정상 처리
2. **Open (차단)**: 실패율 50% 초과 → 10초간 모든 요청 Fallback 처리
3. **Half-Open (테스트)**: 10초 후 → 3개 테스트 요청 → 성공 시 Closed, 실패 시 Open

**Result (결과)**
- Member Service 다운 시에도 소설 읽기 가능 (닉네임만 "익명 사용자")
- 부분 장애가 전체 시스템으로 확산 방지
- 자동 복구 메커니즘 (Half-Open)

### 핵심 키워드
- Cascading Failure
- Resilience4j
- Fallback (익명 사용자)
- 자동 복구

### 데모 포인트
- Member Service 중단 → 소설 목록 "익명 사용자" 표시
- Member Service 재시작 → 자동으로 정상 복구

### 시간 배분: 2분

---

## Slide 19: 트러블슈팅 4 - WebSocket 서비스 간 연동

### 발표 멘트
"네 번째는 **WebSocket 서비스 간 연동** 문제입니다."

**Problem (문제)**
"Reaction Service에서 댓글을 생성하는데, WebSocket 연결은 Story Service에 있습니다. 어떻게 알림을 보낼까요?"

**Troubleshooting (해결)**

**Step 1: Internal API 제공**
"Story Service에 Internal API를 만들어서 외부에서 WebSocket 브로드캐스트를 트리거할 수 있게 했습니다."

```java
// Story Service
@PostMapping("/internal/notify/comments")
public ApiResponse<Void> notifyCommentCreated(@RequestBody CommentNotificationDto dto) {
    messagingTemplate.convertAndSend("/topic/comments/" + dto.getBookId(), dto);
    return ApiResponse.success();
}
```

**Step 2: Feign Client 호출**
"Reaction Service에서 댓글 저장 후 Feign으로 Story Service의 Internal API를 호출합니다."

```java
// Reaction Service
try {
    storyServiceClient.notifyCommentCreated(dto);
} catch (Exception e) {
    // 알림 실패해도 댓글 생성은 완료
    log.error("Failed to notify story service", e);
}
```

**Result (결과)**
- 서비스 간 실시간 이벤트 전파 성공
- 사용자는 서비스 분리를 인식하지 못함 (Seamless UX)
- 알림 실패해도 핵심 기능(댓글 생성)은 정상 동작

### 핵심 키워드
- Internal API
- Feign-WebSocket Bridge
- Seamless UX

### 시간 배분: 1분

---

## Slide 20: 회고 1 - 정진호, 김태형

### 발표 멘트

**정진호 (Team Leader)**
"N+1 문제는 JPA뿐만 아니라 **MSA 간 통신에서도 발생**한다는 것을 깨달았습니다."

"Batch API 설계를 통해 네트워크 호출을 95% 줄였고, CQRS 패턴으로 읽기/쓰기 성능을 모두 최적화할 수 있었습니다."

"특히 WebSocket과 Feign을 연동하여 서비스 간 실시간 이벤트를 구현한 경험이 가장 뜻깊었습니다. 처음에는 '이게 가능할까?' 싶었는데, Internal API와 SimpMessagingTemplate을 조합하니 깔끔하게 해결되더라고요."

---

**김태형 (Sub Leader)**
"인증 로직을 **Gateway로 일원화**하니, 내부 서비스 개발 시 비즈니스 로직에만 집중할 수 있어 생산성이 크게 향상되었습니다."

"처음에는 '각 서비스에서 JWT를 검증하면 되지 않나?' 싶었는데, Gateway에서 한 번만 검증하고 `X-User-Id` 헤더로 전달하니 코드가 훨씬 깔끔해졌습니다."

"Refresh Token Rotation과 Circuit Breaker를 통해 보안과 안정성을 동시에 확보했고, Soft Delete 패턴으로 데이터 복구 가능성을 높인 것도 좋은 경험이었습니다."

### 핵심 키워드
- 정진호: N+1 해결, CQRS, WebSocket-Feign 연동
- 김태형: Gateway 중앙 인증, Token Rotation, Circuit Breaker

### 시간 배분: 1분 30초

---

## Slide 21: 회고 2 - 정병진, 최현지

### 발표 멘트

**정병진 (Developer)**
"계층형 댓글 구조를 설계하면서 **재귀적 데이터 모델**의 복잡성을 체감했습니다."

"처음에는 모든 댓글을 한 번에 로드했는데, 댓글이 100개만 넘어가도 느려지더라고요. Self Join과 Lazy Loading을 적절히 조합하여 성능을 최적화했습니다."

"투표 시스템에서 동시성 문제를 해결하기 위해 **Unique Index**를 활용한 것이 인상 깊었습니다. Application 레벨에서 중복 체크를 하면 Race Condition이 발생할 수 있는데, DB 레벨에서 막으니 안전하더라고요."

"서비스 간 실시간 알림 연동을 통해 MSA의 진정한 가치를 배웠습니다. 서비스는 분리되어 있지만, 사용자는 하나의 통합된 시스템처럼 느낀다는 게 핵심이죠."

---

**최현지 (Document Manager)**
"API 명세서와 ERD를 작성하며 **데이터 정합성**의 중요성을 깨달았습니다."

"MSA 환경에서는 서비스 간 데이터 일관성을 보장하기 어렵기 때문에, Feign Client의 Fallback과 에러 처리가 얼마나 중요한지 배웠습니다."

"QA 과정에서 발견한 엣지 케이스들을 문서화하여 트러블슈팅 가이드로 만든 것이 팀에 큰 도움이 되었습니다. 예를 들어 '연속 작성 금지' 규칙이 관리자에게는 적용되지 않는다는 걸 문서화하지 않았다면, 계속 헷갈렸을 겁니다."

### 핵심 키워드
- 정병진: 계층형 댓글, Lazy Loading, Unique Index
- 최현지: 데이터 정합성, 에러 처리, 엣지 케이스 문서화

### 시간 배분: 1분 30초

---

## Slide 22: 향후 확장 계획

### 발표 멘트
"앞으로 저희가 계획하고 있는 **주요 기능 개선 사항**을 소개하겠습니다."

**① Elasticsearch 도입**
"지금은 DB의 LIKE 검색을 쓰는데, 소설 내용 전체를 검색하거나 태그 검색을 하려면 Elasticsearch가 필요합니다. 인덱싱으로 빠르고 정확한 검색을 제공할 계획입니다."

**② Redis 캐싱**
"인기 소설, 댓글, 투표 수 같은 자주 조회되는 데이터를 Redis에 캐싱하면 읽기 성능이 크게 향상됩니다. DB 부하도 줄일 수 있고요."

**③ RabbitMQ/Kafka 메시지 큐**
"지금은 Feign으로 동기 통신을 하는데, 비동기 메시징으로 전환하면 서비스 간 결합도를 더 낮출 수 있습니다. 예를 들어 댓글 생성 이벤트를 Kafka에 발행하고, Story Service가 구독하는 식이죠."

**④ 모바일 앱**
"React Native나 Flutter로 모바일 앱을 개발하면 언제 어디서나 릴레이 소설 창작에 참여할 수 있습니다."

**⑤ 독립적인 Notification Service**
"이메일, 푸시 알림, SMS 등 다양한 채널로 알림을 보내는 독립 서비스를 구축할 계획입니다."

**⑥ Analytics Service**
"사용자 행동 분석, 통계 데이터 수집, 대시보드 시각화를 담당하는 서비스입니다. 어떤 장르가 인기 있는지, 어느 시간대에 활동이 많은지 등을 분석할 수 있습니다."

### 핵심 키워드
- Elasticsearch (검색)
- Redis (캐싱)
- Kafka (비동기 메시징)
- Mobile App
- Notification Service
- Analytics Service

### 시간 배분: 1분 30초

---

## Slide 23: Q&A

### 발표 멘트
"이제 질의응답 시간입니다. 궁금하신 점이 있으시면 편하게 질문해 주세요."

---

### 예상 질문 및 답변

#### 기술 관련

**Q1: 왜 Spring Cloud를 선택했나요? Kubernetes와 비교하면?**
A: "Spring Cloud는 **Spring Boot 생태계와의 통합**이 장점입니다. Eureka, Gateway, Config Server 모두 Spring으로 개발되어 학습 곡선이 낮았고, 개발 속도가 빨랐습니다. Kubernetes는 인프라 레벨의 오케스트레이션이라면, Spring Cloud는 애플리케이션 레벨의 MSA 프레임워크입니다. 저희는 개발 생산성을 우선시해서 Spring Cloud를 선택했습니다."

**Q2: JWT 토큰이 탈취되면 어떻게 하나요?**
A: "몇 가지 방어 메커니즘이 있습니다. 첫째, Access Token의 유효 기간을 짧게 설정했습니다(30분). 둘째, Refresh Token Rotation으로 토큰을 재발급할 때마다 기존 토큰을 무효화합니다. 셋째, Refresh Token은 HttpOnly Cookie에 저장해서 XSS 공격을 막습니다. 넷째, 운영 환경에서는 HTTPS를 사용해서 통신을 암호화합니다."

**Q3: N+1 문제를 JPA의 fetch join으로 해결할 수는 없나요?**
A: "일반적인 N+1 문제는 JPA의 `@EntityGraph`나 `fetch join`으로 해결할 수 있습니다. 하지만 저희는 **서비스 간 통신**에서 발생한 N+1 문제였습니다. Member Service와 Story Service가 물리적으로 분리되어 있어서 JPA만으로는 해결할 수 없었고, Batch API 패턴을 적용했습니다."

**Q4: Circuit Breaker의 Fallback이 항상 '익명 사용자'인가요?**
A: "아니요, 상황에 따라 다릅니다. 닉네임 조회 실패 시에는 '익명 사용자'를 반환하지만, 다른 API는 다른 Fallback 전략을 사용합니다. 예를 들어 통계 조회 실패 시에는 0으로 채운 기본 통계 객체를 반환합니다. 중요한 건 **핵심 기능(소설 읽기)을 유지**하는 것입니다."

**Q5: WebSocket 연결이 끊어지면 어떻게 되나요?**
A: "STOMP 클라이언트의 자동 재연결 기능을 활용합니다. 연결이 끊어지면 최대 3번까지 재시도하고, 그래도 실패하면 사용자에게 알림을 표시합니다. 또한 연결이 끊어져도 HTTP API는 정상 동작하므로, 페이지 새로고침으로 최신 데이터를 볼 수 있습니다."

---

#### 아키텍처 관련

**Q6: Database per Service 패턴의 단점은 없나요?**
A: "단점이 있습니다. 첫째, **트랜잭션 관리가 어렵습니다**. 여러 서비스에 걸친 트랜잭션은 Saga 패턴이나 보상 트랜잭션을 써야 합니다. 둘째, **SQL JOIN이 불가능**해서 Application Level Join으로 해결해야 합니다. 셋째, **데이터 중복**이 발생할 수 있습니다. 하지만 이런 단점보다 **서비스 독립성**이라는 장점이 더 크다고 판단했습니다."

**Q7: CQRS 패턴에서 데이터 동기화는 어떻게 하나요?**
A: "저희는 **Same Database CQRS**를 사용합니다. 즉, 읽기와 쓰기가 같은 DB를 사용하지만, 접근 방식만 다릅니다(JPA vs MyBatis). 따라서 동기화 문제가 없습니다. 만약 Read DB와 Write DB를 완전히 분리한다면, Event Sourcing이나 Change Data Capture(CDC)를 사용해야 합니다."

**Q8: Common Module이 변경되면 모든 서비스를 재배포해야 하나요?**
A: "이론적으로는 그렇습니다. 하지만 실제로는 **하위 호환성**을 유지하면서 Common Module을 업데이트합니다. 예를 들어 DTO에 필드를 추가할 때는 `@JsonIgnoreProperties(ignoreUnknown = true)`를 사용해서 구버전 서비스도 정상 동작하도록 합니다. 또한 Common Module의 변경 빈도가 높지 않아서 큰 문제는 아니었습니다."

**Q9: 서비스 간 순환 참조는 없나요?**
A: "순환 참조를 방지하기 위해 **의존성 방향을 명확히**했습니다. Story Service와 Reaction Service는 Member Service를 참조하지만, Member Service는 다른 서비스를 참조하지 않습니다. 또한 Feign Client를 Common Module에 두어서 의존성을 중앙 관리합니다."

---

#### 성능 관련

**Q10: 동시 접속자가 많아지면 어떻게 확장하나요?**
A: "**수평 확장(Scale-Out)**을 사용합니다. Eureka에 동일한 서비스의 인스턴스를 여러 개 등록하면, Gateway가 자동으로 로드 밸런싱을 합니다. 예를 들어 Story Service를 3개 띄우면, 트래픽이 3개로 분산됩니다. 또한 WebSocket은 Sticky Session을 사용해서 같은 사용자는 같은 인스턴스로 라우팅됩니다."

**Q11: 데이터베이스 병목은 어떻게 해결하나요?**
A: "몇 가지 방법이 있습니다. 첫째, **인덱싱**으로 쿼리 성능을 최적화합니다. 둘째, **Redis 캐싱**으로 읽기 부하를 줄입니다. 셋째, **Read Replica**를 추가해서 읽기 쿼리를 분산합니다. 넷째, 테이블을 **샤딩**해서 데이터를 여러 DB로 분산합니다."

**Q12: Batch API가 사용자 수만큼 ID를 받으면 쿼리가 느려지지 않나요?**
A: "맞습니다. IN 절의 파라미터가 너무 많으면 느려집니다. 따라서 **페이지네이션**으로 조회하는 데이터 양을 제한합니다(예: 20개). 만약 100명의 작가가 있는 소설 목록을 보여준다면, Batch API를 여러 번 호출하거나, 프론트엔드에서 페이지네이션을 추가합니다."

---

#### 운영 관련

**Q13: 서비스가 6개나 되는데, 로그 추적은 어떻게 하나요?**
A: "현재는 각 서비스의 로그를 개별적으로 확인하지만, 향후 **Sleuth + Zipkin**이나 **ELK Stack**을 도입할 계획입니다. Sleuth는 요청마다 Trace ID를 부여해서 여러 서비스를 거친 요청을 추적할 수 있게 합니다."

**Q14: 배포 순서가 중요한가요?**
A: "네, 중요합니다. **Config Server → Discovery Server → 도메인 서비스 → Gateway** 순서로 배포해야 합니다. Discovery Server가 없으면 서비스들이 서로를 찾을 수 없고, Gateway가 마지막이어야 클라이언트 요청을 받을 수 있습니다."

**Q15: 테스트는 어떻게 하나요?**
A: "세 가지 레벨로 테스트합니다. 첫째, **Unit Test**로 비즈니스 로직을 검증합니다. 둘째, **Integration Test**로 DB와의 통합을 검증합니다. 셋째, **E2E Test**로 전체 시스템의 동작을 검증합니다. 특히 Circuit Breaker나 Fallback 같은 장애 시나리오는 E2E 테스트로 검증합니다."

---

#### 협업 관련

**Q16: 팀원 간 협업은 어떻게 했나요?**
A: "각자 담당 서비스를 독립적으로 개발하되, **Common Module과 API 명세서**로 통신 규약을 맞췄습니다. 매일 10분씩 스탠드업 미팅을 하고, API가 변경되면 즉시 팀원들에게 공유했습니다. Git 전략은 Feature Branch + Pull Request를 사용했습니다."

**Q17: 가장 어려웠던 점은?**
A: "**데이터 정합성 보장**이 가장 어려웠습니다. 예를 들어 소설을 삭제하면 해당 소설의 댓글과 투표도 삭제해야 하는데, 서비스가 분리되어 있어서 트랜잭션으로 묶을 수 없었습니다. 결국 Soft Delete로 삭제 여부만 표시하고, 백그라운드 배치로 주기적으로 정리하는 방식으로 해결했습니다."

**Q18: 다시 한다면 무엇을 바꾸고 싶나요?**
A: "Event Sourcing을 도입하고 싶습니다. 지금은 상태만 저장하는데, 모든 이벤트(소설 생성, 문장 추가, 댓글 작성 등)를 저장하면 감사 로그, 분석, 재현 등이 가능해집니다. 또한 Kafka를 초기부터 도입했다면 서비스 간 통신이 더 느슨했을 것 같습니다."

---

### 마무리 멘트
"감사합니다. Next Page는 **함께 만드는 이야기**입니다. 저희 프로젝트도 팀원들이 함께 만든 결과물입니다. 질문 더 있으시면 발표 후에도 편하게 물어봐 주세요!"

### 핵심 키워드
- 기술: Spring Cloud, JWT, N+1, Circuit Breaker
- 아키텍처: Database per Service, CQRS, Common Module
- 성능: Scale-Out, 캐싱, 인덱싱
- 운영: 로그 추적, 배포 순서, 테스트
- 협업: API 명세서, 스탠드업, Git 전략

### 시간 배분: 5분

---

## 📊 전체 타임라인

| 슬라이드 | 내용 | 시간 | 누적 |
|---------|------|------|------|
| 1 | 타이틀 | 0:30 | 0:30 |
| 2 | 팀원 소개 | 1:30 | 2:00 |
| 3 | 프로젝트 개요 | 2:00 | 4:00 |
| 4 | MSA 전환 배경 | 2:00 | 6:00 |
| 5 | 시스템 아키텍처 | 2:00 | 8:00 |
| 6 | 릴레이 소설 창작 | 2:00 | 10:00 |
| 7 | 실시간 WebSocket | 2:00 | 12:00 |
| 8 | 커뮤니티 (댓글/투표) | 1:30 | 13:30 |
| 9 | 보안 | 1:30 | 15:00 |
| 10 | Circuit Breaker | 2:00 | 17:00 |
| 11 | CQRS | 1:30 | 18:30 |
| 12 | 기술적 차별화 | 1:00 | 19:30 |
| 13 | Database per Service | 1:00 | 20:30 |
| 14 | Common Module | 1:30 | 22:00 |
| 15 | Feign-WebSocket Bridge | 2:00 | 24:00 |
| 16 | N+1 문제 | 2:30 | 26:30 |
| 17 | 데이터 조인 | 2:00 | 28:30 |
| 18 | 장애 전파 | 2:00 | 30:30 |
| 19 | WebSocket 연동 | 1:00 | 31:30 |
| 20 | 회고 1 | 1:30 | 33:00 |
| 21 | 회고 2 | 1:30 | 34:30 |
| 22 | 향후 계획 | 1:30 | 36:00 |
| 23 | Q&A | 5:00 | 41:00 |

**총 예상 시간: 약 20분 (발표) + 5분 (Q&A) = 25분**

---

## 🎯 발표 팁

### 1. 시작 전
- 심호흡 3회
- 물 한 잔 준비
- 데모 환경 최종 점검
- 타이머 준비 (20분 알람)

### 2. 발표 중
- **천천히, 또박또박** 말하기
- 청중과 **아이컨택** 유지
- 중요한 숫자는 **강조**하기 (95% 감소, 6배 향상)
- **손동작** 활용 (아키텍처 설명 시)
- **쉼** 주기 (문장 끝에 1초)

### 3. 데모 시
- 미리 **로그인** 해두기
- **두 개의 브라우저 창** 준비
- 실시간 기능 시연에 **집중**
- 실패해도 당황하지 말고 **설명으로 커버**

### 4. 질문 시
- 질문을 **다시 반복**하기
- 모르면 **솔직하게** 말하기
- 팀원에게 **도움** 요청 가능

---

## ✅ 최종 체크리스트

발표 전날:
- [ ] 발표 자료 최종 리뷰
- [ ] 노트 숙지 (최소 3회 통독)
- [ ] 데모 시나리오 리허설
- [ ] 예상 질문 답변 연습
- [ ] 팀원들과 역할 분담 (누가 어떤 슬라이드를 발표할지)

발표 당일:
- [ ] 노트북, 충전기, 마우스
- [ ] 프로젝트 실행 (모든 서비스 8761 확인)
- [ ] 백업 자료 (PDF, 동영상)
- [ ] 발표 자료 사본 (USB, 클라우드)
- [ ] 옷차림 점검
- [ ] 발표장 30분 전 도착

**화이팅! 잘 하실 거예요! 🚀**
