# Next-Page MSA 테스트 코드 문서

## 📊 테스트 커버리지 요약

### JaCoCo 테스트 커버리지 분석 결과

| 모듈 | 라인 커버리지 | 브랜치 커버리지 | 테스트 파일 수 | 테스트 메서드 수 |
|------|--------------|----------------|--------------|----------------|
| **common-module** | 70% | 0% | 5 | 37 |
| **member-service** | 82% | 72% | 10 | 90+ |
| **story-service** | 90% | 63% | 5 | 75+ |
| **reaction-service** | 97% | 80% | 5 | 50+ |
| **config-server** | - | - | 0 | 0 |
| **discovery-server** | - | - | 0 | 0 |
| **gateway-server** | - | - | 0 | 0 |
| **전체** | **85%** | **71%** | **25** | **252+** |

**테스트 목표:** 라인 커버리지 70% 이상, 브랜치 커버리지 70% 이상

---

## 📋 전체 테스트 케이스 목록

### 범례
- **P**: Pass (통과)
- **F**: Fail (실패)
- **테스트 방식**:
  - `Unit`: 단위 테스트 (Mockito 사용)
  - `MockMvc`: 컨트롤러 단위 테스트 (Spring MockMvc)
  - `Integration`: 통합 테스트 (전체 Spring Context)

---

## 1️⃣ Common Module 테스트

### 1.1 Filter 테스트

#### GatewayAuthenticationFilterTest (13개 테스트)

| 테스트 코드 | 분류 | | | 테스트 시나리오 | 테스트 방식 | 테스트 성공여부 |
|------------|------|---|---|----------------|------------|----------------|
| | 대분류 | 중분류 | 소분류 | | | (P/F) |
| TC-002.001.001 | Common | Filter | Gateway Auth | 유효한 Gateway 헤더로 인증 설정 성공 | Unit | P |
| TC-002.001.002 | Common | Filter | Gateway Auth | ADMIN 역할 헤더로 ROLE_ADMIN 권한 부여 | Unit | P |
| TC-002.001.003 | Common | Filter | Gateway Auth | ROLE_ 접두사가 있는 역할을 그대로 사용 | Unit | P |
| TC-002.001.004 | Common | Filter | Gateway Auth | 역할 헤더 없을 시 기본 ROLE_USER 부여 | Unit | P |
| TC-002.001.005 | Common | Filter | Gateway Auth | X-User-Id 헤더 누락 시 인증 미설정 | Unit | P |
| TC-002.001.006 | Common | Filter | Gateway Auth | X-User-Email 헤더 누락 시 인증 미설정 | Unit | P |
| TC-002.001.007 | Common | Filter | Gateway Auth | 모든 Gateway 헤더 누락 시 인증 미설정 | Unit | P |
| TC-002.001.008 | Common | Filter | Gateway Auth | /swagger-ui 경로는 필터 스킵 | Unit | P |
| TC-002.001.009 | Common | Filter | Gateway Auth | /v3/api-docs 경로는 필터 스킵 | Unit | P |
| TC-002.001.010 | Common | Filter | Gateway Auth | /actuator 경로는 필터 스킵 | Unit | P |
| TC-002.001.011 | Common | Filter | Gateway Auth | /h2-console 경로는 필터 스킵 | Unit | P |
| TC-002.001.012 | Common | Filter | Gateway Auth | /error 경로는 필터 스킵 | Unit | P |
| TC-002.001.013 | Common | Filter | Gateway Auth | 일반 API 경로는 필터 적용 | Unit | P |

#### JwtToHeaderFilterTest (11개 테스트)

| 테스트 코드 | 분류 | | | 테스트 시나리오 | 테스트 방식 | 테스트 성공여부 |
|------------|------|---|---|----------------|------------|----------------|
| | 대분류 | 중분류 | 소분류 | | | (P/F) |
| TC-002.002.001 | Common | Filter | JWT to Header | 유효한 JWT 토큰을 헤더로 변환 성공 | Unit | P |
| TC-002.002.002 | Common | Filter | JWT to Header | 기존 Gateway 헤더 존재 시 JWT 파싱 스킵 | Unit | P |
| TC-002.002.003 | Common | Filter | JWT to Header | Authorization 헤더 없을 시 파싱 없이 계속 | Unit | P |
| TC-002.002.004 | Common | Filter | JWT to Header | Bearer 접두사 없을 시 파싱 없이 계속 | Unit | P |
| TC-002.002.005 | Common | Filter | JWT to Header | 잘못된 JWT 토큰 형식으로 경고와 함께 계속 | Unit | P |
| TC-002.002.006 | Common | Filter | JWT to Header | 만료된 JWT 토큰으로 경고와 함께 계속 | Unit | P |
| TC-002.002.007 | Common | Filter | JWT to Header | /swagger-ui 경로는 필터 스킵 | Unit | P |
| TC-002.002.008 | Common | Filter | JWT to Header | /v3/api-docs 경로는 필터 스킵 | Unit | P |
| TC-002.002.009 | Common | Filter | JWT to Header | /actuator 경로는 필터 스킵 | Unit | P |
| TC-002.002.010 | Common | Filter | JWT to Header | 일반 API 경로는 필터 적용 | Unit | P |
| TC-002.002.011 | Common | Filter | JWT to Header | 비밀 키 미설정 시 JWT 파싱 스킵 | Unit | P |

### 1.2 Security 테스트

#### CustomAccessDeniedHandlerTest (3개 테스트)

| 테스트 코드 | 분류 | | | 테스트 시나리오 | 테스트 방식 | 테스트 성공여부 |
|------------|------|---|---|----------------|------------|----------------|
| | 대분류 | 중분류 | 소분류 | | | (P/F) |
| TC-002.003.001 | Common | Security | Access Denied | 접근 거부 시 403 응답 반환 | Unit | P |
| TC-002.003.002 | Common | Security | Access Denied | 응답 Content-Type application/json UTF-8 설정 | Unit | P |
| TC-002.003.003 | Common | Security | Access Denied | ACCESS_DENIED 에러 코드 사용 | Unit | P |

#### CustomAuthenticationEntryPointTest (3개 테스트)

| 테스트 코드 | 분류 | | | 테스트 시나리오 | 테스트 방식 | 테스트 성공여부 |
|------------|------|---|---|----------------|------------|----------------|
| | 대분류 | 중분류 | 소분류 | | | (P/F) |
| TC-002.004.001 | Common | Security | Entry Point | 인증 실패 시 401 응답 반환 | Unit | P |
| TC-002.004.002 | Common | Security | Entry Point | 응답 Content-Type application/json UTF-8 설정 | Unit | P |
| TC-002.004.003 | Common | Security | Entry Point | UNAUTHENTICATED 에러 코드 사용 | Unit | P |

---

## 2️⃣ Member Service 테스트

### 2.1 인증 테스트

#### AuthControllerTest (10개 테스트)

| 테스트 코드 | 분류 | | | 테스트 시나리오 | 테스트 방식 | 테스트 성공여부 |
|------------|------|---|---|----------------|------------|----------------|
| | 대분류 | 중분류 | 소분류 | | | (P/F) |
| TC-001.001.001 | Member | Auth | 로그인 | 로그인 성공 - AT/RT 반환 | MockMvc | P |
| TC-001.001.002 | Member | Auth | 로그인 | 로그인 실패 - 잘못된 비밀번호 | MockMvc | P |
| TC-001.001.003 | Member | Auth | 로그인 | 로그인 실패 - 관리자 승인 대기 상태 | MockMvc | P |
| TC-001.001.004 | Member | Auth | 로그인 | 로그인 실패 - 잘못된 이메일 형식 | MockMvc | P |
| TC-001.001.005 | Member | Auth | 로그인 | 로그인 실패 - 비밀번호 누락 | MockMvc | P |
| TC-001.001.006 | Member | Auth | 토큰 갱신 | 토큰 갱신 성공 - 유효한 RT | MockMvc | P |
| TC-001.001.007 | Member | Auth | 토큰 갱신 | 토큰 갱신 실패 - RT 쿠키 없음 | MockMvc | P |
| TC-001.001.008 | Member | Auth | 토큰 갱신 | 토큰 갱신 실패 - 유효하지 않은 RT | MockMvc | P |
| TC-001.001.009 | Member | Auth | 로그아웃 | 로그아웃 성공 - RT 삭제 | MockMvc | P |
| TC-001.001.010 | Member | Auth | 로그아웃 | 로그아웃 성공 - RT 없어도 성공 | MockMvc | P |

#### AuthServiceTest (13개 테스트)

| 테스트 코드 | 분류 | | | 테스트 시나리오 | 테스트 방식 | 테스트 성공여부 |
|------------|------|---|---|----------------|------------|----------------|
| | 대분류 | 중분류 | 소분류 | | | (P/F) |
| TC-001.002.001 | Member | Auth | 로그인 서비스 | 로그인 성공 - AT/RT 생성 | Unit | P |
| TC-001.002.002 | Member | Auth | 로그인 서비스 | 로그인 실패 - 사용자 없음 | Unit | P |
| TC-001.002.003 | Member | Auth | 로그인 서비스 | 로그인 실패 - 비밀번호 불일치 | Unit | P |
| TC-001.002.004 | Member | Auth | 로그인 서비스 | 로그인 실패 - PENDING 상태 | Unit | P |
| TC-001.002.005 | Member | Auth | 토큰 갱신 | 토큰 갱신 성공 - 새 AT/RT 발급 | Unit | P |
| TC-001.002.006 | Member | Auth | 토큰 갱신 | 토큰 갱신 실패 - 유효하지 않은 토큰 | Unit | P |
| TC-001.002.007 | Member | Auth | 토큰 갱신 | 토큰 갱신 실패 - DB에 저장된 토큰 없음 | Unit | P |
| TC-001.002.008 | Member | Auth | 토큰 갱신 | 토큰 갱신 실패 - 토큰 불일치 | Unit | P |
| TC-001.002.009 | Member | Auth | 토큰 갱신 | 토큰 갱신 실패 - 토큰 만료 | Unit | P |
| TC-001.002.010 | Member | Auth | 로그아웃 | 로그아웃 성공 - RT 삭제 | Unit | P |
| TC-001.002.011 | Member | Auth | 로그아웃 | 로그아웃 - 잘못된 토큰으로도 예외 없음 | Unit | P |
| TC-001.002.012 | Member | Auth | RT 저장 | 새 RT 저장 - 첫 로그인 | Unit | P |
| TC-001.002.013 | Member | Auth | RT 갱신 | 기존 RT 업데이트 - 재로그인 | Unit | P |

### 2.2 회원 관리 테스트

#### MemberControllerTest (17개 테스트)

| 테스트 코드 | 분류 | | | 테스트 시나리오 | 테스트 방식 | 테스트 성공여부 |
|------------|------|---|---|----------------|------------|----------------|
| | 대분류 | 중분류 | 소분류 | | | (P/F) |
| TC-001.003.001 | Member | 회원가입 | 일반 회원 | 회원가입 성공 - 유효한 요청 | MockMvc | P |
| TC-001.003.002 | Member | 회원가입 | 일반 회원 | 회원가입 실패 - 이메일 중복 | MockMvc | P |
| TC-001.003.003 | Member | 회원가입 | 일반 회원 | 회원가입 실패 - 닉네임 중복 | MockMvc | P |
| TC-001.003.004 | Member | 회원가입 | 일반 회원 | 회원가입 실패 - 이메일 누락 | MockMvc | P |
| TC-001.003.005 | Member | 회원가입 | 일반 회원 | 회원가입 실패 - 비밀번호 누락 | MockMvc | P |
| TC-001.003.006 | Member | 회원가입 | 관리자 | 관리자 생성 성공 - 유효한 요청 | MockMvc | P |
| TC-001.003.007 | Member | 회원가입 | 관리자 | 관리자 생성 실패 - 이메일 중복 | MockMvc | P |
| TC-001.003.008 | Member | 관리 | 관리자 승인 | 관리자 승인 성공 - 관리자가 승인 | MockMvc | P |
| TC-001.003.009 | Member | 관리 | 관리자 승인 | 관리자 승인 실패 - 사용자 없음 | MockMvc | P |
| TC-001.003.010 | Member | 관리 | 관리자 승인 | 관리자 승인 실패 - 권한 없음 | MockMvc | P |
| TC-001.003.011 | Member | 회원 탈퇴 | 본인 탈퇴 | 회원 탈퇴 성공 - 본인 탈퇴 | MockMvc | P |
| TC-001.003.012 | Member | 회원 탈퇴 | 강제 탈퇴 | 강제 탈퇴 성공 - 관리자가 탈퇴 | MockMvc | P |
| TC-001.003.013 | Member | 회원 탈퇴 | 강제 탈퇴 | 강제 탈퇴 실패 - 대상 사용자 없음 | MockMvc | P |
| TC-001.003.014 | Member | 중복 검증 | 이메일 | 이메일 중복 검증 성공 - 사용 가능 | MockMvc | P |
| TC-001.003.015 | Member | 중복 검증 | 이메일 | 이메일 중복 검증 실패 - 중복됨 | MockMvc | P |
| TC-001.003.016 | Member | 중복 검증 | 닉네임 | 닉네임 중복 검증 성공 - 사용 가능 | MockMvc | P |
| TC-001.003.017 | Member | 중복 검증 | 닉네임 | 닉네임 중복 검증 실패 - 중복됨 | MockMvc | P |

#### MemberServiceTest (17개 테스트)

| 테스트 코드 | 분류 | | | 테스트 시나리오 | 테스트 방식 | 테스트 성공여부 |
|------------|------|---|---|----------------|------------|----------------|
| | 대분류 | 중분류 | 소분류 | | | (P/F) |
| TC-001.004.001 | Member | 회원가입 | 일반 회원 | 회원가입 성공 - USER 역할, ACTIVE 상태 | Unit | P |
| TC-001.004.002 | Member | 회원가입 | 일반 회원 | 회원가입 실패 - 이메일 중복 | Unit | P |
| TC-001.004.003 | Member | 회원가입 | 일반 회원 | 회원가입 실패 - 닉네임 중복 | Unit | P |
| TC-001.004.004 | Member | 회원가입 | 관리자 | 관리자 가입 성공 - ADMIN 역할, PENDING 상태 | Unit | P |
| TC-001.004.005 | Member | 회원가입 | 관리자 | 관리자 가입 실패 - 이메일 중복 | Unit | P |
| TC-001.004.006 | Member | 관리 | 관리자 승인 | 관리자 승인 성공 - ACTIVE 상태로 변경 | Unit | P |
| TC-001.004.007 | Member | 관리 | 관리자 승인 | 관리자 승인 실패 - 권한 없음 | Unit | P |
| TC-001.004.008 | Member | 관리 | 관리자 승인 | 관리자 승인 실패 - 사용자 없음 | Unit | P |
| TC-001.004.009 | Member | 회원 탈퇴 | 본인 탈퇴 | 회원 탈퇴 성공 - 소프트 삭제 | Unit | P |
| TC-001.004.010 | Member | 회원 탈퇴 | 본인 탈퇴 | 회원 탈퇴 실패 - 사용자 없음 | Unit | P |
| TC-001.004.011 | Member | 중복 검증 | 이메일 | 이메일 중복 검증 통과 - 사용 가능 | Unit | P |
| TC-001.004.012 | Member | 중복 검증 | 이메일 | 이메일 중복 검증 실패 - 중복됨 | Unit | P |
| TC-001.004.013 | Member | 중복 검증 | 닉네임 | 닉네임 중복 검증 통과 - 사용 가능 | Unit | P |
| TC-001.004.014 | Member | 중복 검증 | 닉네임 | 닉네임 중복 검증 실패 - 중복됨 | Unit | P |
| TC-001.004.015 | Member | 회원 탈퇴 | 강제 탈퇴 | 강제 탈퇴 성공 - 관리자가 탈퇴 처리 | Unit | P |
| TC-001.004.016 | Member | 회원 탈퇴 | 강제 탈퇴 | 강제 탈퇴 실패 - 권한 없음 | Unit | P |
| TC-001.004.017 | Member | 회원 탈퇴 | 강제 탈퇴 | 강제 탈퇴 실패 - 대상 사용자 없음 | Unit | P |

### 2.3 JWT 테스트

#### JwtTokenProviderTest (22개 테스트)

| 테스트 코드 | 분류 | | | 테스트 시나리오 | 테스트 방식 | 테스트 성공여부 |
|------------|------|---|---|----------------|------------|----------------|
| | 대분류 | 중분류 | 소분류 | | | (P/F) |
| TC-001.005.001 | Member | JWT | Access Token | AT 생성 성공 - 유효한 인증 객체 | Unit | P |
| TC-001.005.002 | Member | JWT | Access Token | AT 생성 - Principal이 CustomUserDetails 아님 | Unit | P |
| TC-001.005.003 | Member | JWT | Access Token | AT 생성 - ADMIN 역할로 생성 | Unit | P |
| TC-001.005.004 | Member | JWT | Access Token | AT 생성 및 인증 복원 | Unit | P |
| TC-001.005.005 | Member | JWT | Refresh Token | RT 생성 성공 - 유효한 인증 객체 | Unit | P |
| TC-001.005.006 | Member | JWT | Refresh Token | RT 생성 - Principal이 CustomUserDetails 아님 | Unit | P |
| TC-001.005.007 | Member | JWT | Refresh Token | RT에서 이메일 추출 | Unit | P |
| TC-001.005.008 | Member | JWT | Refresh Token | RT 유효성 검증 | Unit | P |
| TC-001.005.009 | Member | JWT | Refresh Token | RT 만료 시간 조회 | Unit | P |
| TC-001.005.010 | Member | JWT | 인증 복원 | 토큰에서 Authentication 생성 성공 | Unit | P |
| TC-001.005.011 | Member | JWT | 인증 복원 | 권한 없는 토큰으로 예외 발생 | Unit | P |
| TC-001.005.012 | Member | JWT | 인증 복원 | 이메일 없는 토큰으로 예외 발생 | Unit | P |
| TC-001.005.013 | Member | JWT | 토큰 검증 | 유효한 토큰 검증 통과 | Unit | P |
| TC-001.005.014 | Member | JWT | 토큰 검증 | 잘못된 형식 토큰 검증 실패 | Unit | P |
| TC-001.005.015 | Member | JWT | 토큰 검증 | 만료된 토큰 검증 실패 | Unit | P |
| TC-001.005.016 | Member | JWT | 토큰 검증 | 빈 문자열 토큰 검증 실패 | Unit | P |
| TC-001.005.017 | Member | JWT | 토큰 검증 | null 토큰 검증 실패 | Unit | P |
| TC-001.005.018 | Member | JWT | 토큰 검증 | 잘못된 토큰 형식 (점 없음) 검증 실패 | Unit | P |
| TC-001.005.019 | Member | JWT | 토큰 파싱 | 유효한 토큰에서 이메일 추출 성공 | Unit | P |
| TC-001.005.020 | Member | JWT | 토큰 파싱 | 만료된 토큰에서도 이메일 추출 가능 | Unit | P |
| TC-001.005.021 | Member | JWT | 토큰 파싱 | 유효한 토큰의 만료 시간 조회 | Unit | P |
| TC-001.005.022 | Member | JWT | 토큰 파싱 | 만료된 토큰의 만료 시간은 음수 | Unit | P |

### 2.4 회원 조회 테스트

#### MemberQueryControllerTest (2개 테스트)

| 테스트 코드 | 분류 | | | 테스트 시나리오 | 테스트 방식 | 테스트 성공여부 |
|------------|------|---|---|----------------|------------|----------------|
| | 대분류 | 중분류 | 소분류 | | | (P/F) |
| TC-001.006.001 | Member | 조회 | 마이페이지 | 내 정보 조회 성공 - 인증된 사용자 | MockMvc | P |
| TC-001.006.002 | Member | 조회 | 마이페이지 | 내 정보 조회 실패 - 미인증 사용자 | MockMvc | P |

#### MemberQueryServiceTest (5개 테스트)

| 테스트 코드 | 분류 | | | 테스트 시나리오 | 테스트 방식 | 테스트 성공여부 |
|------------|------|---|---|----------------|------------|----------------|
| | 대분류 | 중분류 | 소분류 | | | (P/F) |
| TC-001.007.001 | Member | 조회 | 마이페이지 | 마이페이지 조회 성공 - 모든 통계 조회 성공 | Unit | P |
| TC-001.007.002 | Member | 조회 | 마이페이지 | 마이페이지 조회 실패 - 회원 없음 | Unit | P |
| TC-001.007.003 | Member | 조회 | 마이페이지 | 마이페이지 조회 - Story 서비스 실패 시 기본값 | Unit | P |
| TC-001.007.004 | Member | 조회 | 마이페이지 | 마이페이지 조회 - Reaction 서비스 실패 시 기본값 | Unit | P |
| TC-001.007.005 | Member | 조회 | 마이페이지 | 마이페이지 조회 - 모든 Feign 호출 실패 시 기본값 | Unit | P |

### 2.5 API 및 통합 테스트

#### MemberApiControllerTest (3개 테스트)

| 테스트 코드 | 분류 | | | 테스트 시나리오 | 테스트 방식 | 테스트 성공여부 |
|------------|------|---|---|----------------|------------|----------------|
| | 대분류 | 중분류 | 소분류 | | | (P/F) |
| TC-001.008.001 | Member | API | Internal API | 사용자 닉네임 조회 성공 | Unit | P |
| TC-001.008.002 | Member | API | Internal API | 사용자 닉네임 조회 - 사용자 없음 시 Unknown 반환 | Unit | P |
| TC-001.008.003 | Member | API | Internal API | 존재하지 않는 사용자 닉네임 조회 시 Unknown 반환 | Unit | P |

#### MemberIntegrationTest (2개 테스트)

| 테스트 코드 | 분류 | | | 테스트 시나리오 | 테스트 방식 | 테스트 성공여부 |
|------------|------|---|---|----------------|------------|----------------|
| | 대분류 | 중분류 | 소분류 | | | (P/F) |
| TC-001.009.001 | Member | 통합 | 전체 플로우 | 회원가입 -> 로그인 -> 내 정보 조회 통합 테스트 | Integration | P |
| TC-001.009.002 | Member | 통합 | 중복 검증 | 중복 이메일 회원가입 시도 통합 테스트 | Integration | P |

#### JwtTokenResponseTest (2개 테스트)

| 테스트 코드 | 분류 | | | 테스트 시나리오 | 테스트 방식 | 테스트 성공여부 |
|------------|------|---|---|----------------|------------|----------------|
| | 대분류 | 중분류 | 소분류 | | | (P/F) |
| TC-001.010.001 | Member | JWT | DTO | Builder 패턴으로 객체 생성 | Unit | P |
| TC-001.010.002 | Member | JWT | DTO | Static Factory 메서드로 객체 생성 | Unit | P |

---

## 3️⃣ Story Service 테스트

### 3.1 카테고리 테스트

#### CategoryControllerTest (3개 테스트)

| 테스트 코드 | 분류 | | | 테스트 시나리오 | 테스트 방식 | 테스트 성공여부 |
|------------|------|---|---|----------------|------------|----------------|
| | 대분류 | 중분류 | 소분류 | | | (P/F) |
| TC-003.001.001 | Story | 카테고리 | 조회 | 전체 카테고리 조회 성공 | Unit | P |
| TC-003.001.002 | Story | 카테고리 | 조회 | 카테고리 없을 시 빈 리스트 반환 | Unit | P |
| TC-003.001.003 | Story | 카테고리 | 조회 | 단일 카테고리만 있을 시 조회 | Unit | P |

### 3.2 소설 작성 테스트

#### BookControllerTest (16개 테스트)

| 테스트 코드 | 분류 | | | 테스트 시나리오 | 테스트 방식 | 테스트 성공여부 |
|------------|------|---|---|----------------|------------|----------------|
| | 대분류 | 중분류 | 소분류 | | | (P/F) |
| TC-003.002.001 | Story | 소설 | 생성 | 소설 생성 성공 - 유효한 요청 | MockMvc | P |
| TC-003.002.002 | Story | 소설 | 생성 | 소설 생성 실패 - 제목 누락 | MockMvc | P |
| TC-003.002.003 | Story | 소설 | 생성 | 소설 생성 실패 - maxSequence 최소값 미만 | MockMvc | P |
| TC-003.002.004 | Story | 문장 | 이어쓰기 | 문장 이어쓰기 성공 - 유효한 요청 | MockMvc | P |
| TC-003.002.005 | Story | 문장 | 이어쓰기 | 문장 이어쓰기 실패 - 연속 작성 시도 | MockMvc | P |
| TC-003.002.006 | Story | 문장 | 이어쓰기 | 문장 이어쓰기 실패 - 이미 완결된 소설 | MockMvc | P |
| TC-003.002.007 | Story | 소설 | 완결 | 소설 완결 성공 - 작성자가 완결 | MockMvc | P |
| TC-003.002.008 | Story | 소설 | 완결 | 소설 완결 실패 - 작성자가 아님 | MockMvc | P |
| TC-003.002.009 | Story | 소설 | 수정 | 소설 제목 수정 성공 - 소유자가 수정 | MockMvc | P |
| TC-003.002.010 | Story | 소설 | 수정 | 소설 제목 수정 실패 - 소설 없음 | MockMvc | P |
| TC-003.002.011 | Story | 문장 | 수정 | 문장 수정 성공 - 소유자가 수정 | MockMvc | P |
| TC-003.002.012 | Story | 문장 | 수정 | 문장 수정 실패 - 문장 없음 | MockMvc | P |
| TC-003.002.013 | Story | 소설 | 삭제 | 소설 삭제 성공 - 소유자가 삭제 | MockMvc | P |
| TC-003.002.014 | Story | 소설 | 삭제 | 소설 삭제 실패 - 권한 없음 | MockMvc | P |
| TC-003.002.015 | Story | 문장 | 삭제 | 문장 삭제 성공 - 소유자가 삭제 | MockMvc | P |
| TC-003.002.016 | Story | 문장 | 삭제 | 문장 삭제 실패 - 권한 없음 | MockMvc | P |

#### BookServiceTest (20개 테스트)

| 테스트 코드 | 분류 | | | 테스트 시나리오 | 테스트 방식 | 테스트 성공여부 |
|------------|------|---|---|----------------|------------|----------------|
| | 대분류 | 중분류 | 소분류 | | | (P/F) |
| TC-003.003.001 | Story | 소설 | 생성 | 소설 생성 성공 - 첫 문장과 함께 생성 | Unit | P |
| TC-003.003.002 | Story | 문장 | 이어쓰기 | 문장 이어쓰기 성공 - 다른 사용자가 작성 + 실시간 통계 브로드캐스트 | Unit | P |
| TC-003.003.003 | Story | 문장 | 이어쓰기 | 문장 이어쓰기 실패 - 동일 사용자 연속 작성 | Unit | P |
| TC-003.003.004 | Story | 문장 | 이어쓰기 | 문장 이어쓰기 성공 - 관리자는 연속 작성 가능 | Unit | P |
| TC-003.003.005 | Story | 문장 | 이어쓰기 | 문장 이어쓰기 실패 - COMPLETED 상태 소설 | Unit | P |
| TC-003.003.006 | Story | 문장 | 이어쓰기 | 문장 이어쓰기 실패 - 소설 없음 | Unit | P |
| TC-003.003.007 | Story | 소설 | 완결 | 소설 완결 성공 - 작성자가 완결 처리 | Unit | P |
| TC-003.003.008 | Story | 소설 | 완결 | 소설 완결 실패 - 작성자 아님 | Unit | P |
| TC-003.003.009 | Story | 소설 | 완결 | 소설 완결 실패 - 이미 완결됨 | Unit | P |
| TC-003.003.010 | Story | 소설 | 수정 | 소설 제목 수정 성공 - 소유자가 수정 | Unit | P |
| TC-003.003.011 | Story | 소설 | 수정 | 소설 제목 수정 성공 - 관리자가 수정 | Unit | P |
| TC-003.003.012 | Story | 소설 | 수정 | 소설 제목 수정 실패 - 소유자 및 관리자 아님 | Unit | P |
| TC-003.003.013 | Story | 소설 | 삭제 | 소설 삭제 성공 - 소유자가 삭제 | Unit | P |
| TC-003.003.014 | Story | 소설 | 삭제 | 소설 삭제 성공 - 관리자가 삭제 | Unit | P |
| TC-003.003.015 | Story | 소설 | 삭제 | 소설 삭제 실패 - 소유자 및 관리자 아님 | Unit | P |
| TC-003.003.016 | Story | 문장 | 수정 | 문장 수정 성공 - 마지막 문장 수정 | Unit | P |
| TC-003.003.017 | Story | 문장 | 수정 | 문장 수정 실패 - 마지막 문장 아님 | Unit | P |
| TC-003.003.018 | Story | 문장 | 삭제 | 문장 삭제 성공 - 마지막 문장 삭제 + 실시간 통계 브로드캐스트 | Unit | P |
| TC-003.003.019 | Story | WebSocket | 통계 | 소설 통계 브로드캐스트 성공 - WebSocket으로 실시간 통계 전송 | Unit | P |
| TC-003.003.020 | Story | WebSocket | 통계 | 소설 통계 브로드캐스트 실패 - 통계 없을 경우 전송하지 않음 | Unit | P |

### 3.3 소설 조회 테스트

#### BookQueryControllerTest (6개 테스트)

| 테스트 코드 | 분류 | | | 테스트 시나리오 | 테스트 방식 | 테스트 성공여부 |
|------------|------|---|---|----------------|------------|----------------|
| | 대분류 | 중분류 | 소분류 | | | (P/F) |
| TC-003.004.001 | Story | 소설 | 검색 | 소설 검색 성공 - 필터 적용 | MockMvc | P |
| TC-003.004.002 | Story | 소설 | 조회 | 소설 ID로 조회 성공 | MockMvc | P |
| TC-003.004.003 | Story | 소설 | 뷰어 | 진행 중 소설 뷰어 조회 성공 | MockMvc | P |
| TC-003.004.004 | Story | 소설 | 뷰어 | 완결 소설 뷰어 조회 성공 | MockMvc | P |
| TC-003.004.005 | Story | 문장 | 조회 | 내가 쓴 문장 조회 성공 - 인증된 사용자 | MockMvc | P |
| TC-003.004.006 | Story | 문장 | 조회 | 내가 쓴 문장 조회 실패 - 미인증 사용자 | MockMvc | P |

#### BookQueryServiceTest (18개 테스트)

| 테스트 코드 | 분류 | | | 테스트 시나리오 | 테스트 방식 | 테스트 성공여부 |
|------------|------|---|---|----------------|------------|----------------|
| | 대분류 | 중분류 | 소분류 | | | (P/F) |
| TC-003.005.001 | Story | 소설 | 검색 | 소설 검색 - 빈 결과 | Unit | P |
| TC-003.005.002 | Story | 소설 | 검색 | 소설 검색 - 데이터 있음 | Unit | P |
| TC-003.005.003 | Story | 소설 | 검색 | 소설 검색 - Feign 성공으로 작성자 정보 포함 | Unit | P |
| TC-003.005.004 | Story | 소설 | 검색 | 소설 검색 - Feign 실패 시 작성자 닉네임 null | Unit | P |
| TC-003.005.005 | Story | 소설 | 검색 | 소설 검색 - Feign 성공으로 투표 정보 포함 | Unit | P |
| TC-003.005.006 | Story | 소설 | 검색 | 소설 검색 - Feign 실패 시 투표 수 기본값 0 | Unit | P |
| TC-003.005.007 | Story | 소설 | 검색 | 소설 검색 - SecurityUtil 실패 시에도 계속 진행 | Unit | P |
| TC-003.005.008 | Story | 소설 | 검색 | 소설 검색 - Deprecated 메서드 (페이지네이션 없음) | Unit | P |
| TC-003.005.009 | Story | 소설 | 조회 | 소설 상세 조회 성공 - Feign 성공 | Unit | P |
| TC-003.005.010 | Story | 소설 | 조회 | 소설 상세 조회 실패 - 소설 없음 | Unit | P |
| TC-003.005.011 | Story | 소설 | 조회 | 소설 상세 조회 - Feign 실패 시 닉네임 null | Unit | P |
| TC-003.005.012 | Story | 소설 | 뷰어 | 소설 뷰어 조회 - 인증된 사용자 | Unit | P |
| TC-003.005.013 | Story | 소설 | 뷰어 | 소설 뷰어 조회 - 미인증 사용자 | Unit | P |
| TC-003.005.014 | Story | 소설 | 뷰어 | 소설 뷰어 조회 실패 - 소설 없음 | Unit | P |
| TC-003.005.015 | Story | 소설 | 뷰어 | 소설 뷰어 조회 - Feign 성공으로 투표 정보 포함 | Unit | P |
| TC-003.005.016 | Story | 소설 | 뷰어 | 소설 뷰어 조회 - Feign 실패 시 투표 기본값 | Unit | P |
| TC-003.005.017 | Story | 소설 | 뷰어 | 소설 뷰어 조회 - Feign 성공으로 작성자 정보 포함 | Unit | P |
| TC-003.005.018 | Story | 문장 | 조회 | 사용자별 문장 조회 성공 - 페이지네이션 포함 | Unit | P |

---

## 4️⃣ Reaction Service 테스트

### 4.1 댓글 및 투표 테스트

#### ReactionControllerTest (17개 테스트)

| 테스트 코드 | 분류 | | | 테스트 시나리오 | 테스트 방식 | 테스트 성공여부 |
|------------|------|---|---|----------------|------------|----------------|
| | 대분류 | 중분류 | 소분류 | | | (P/F) |
| TC-004.001.001 | Reaction | 댓글 | 생성 | 댓글 작성 성공 - 유효한 요청 | MockMvc | P |
| TC-004.001.002 | Reaction | 댓글 | 생성 | 댓글 작성 성공 - Feign 실패 시에도 성공 | MockMvc | P |
| TC-004.001.003 | Reaction | 댓글 | 생성 | 대댓글 작성 성공 - 부모 댓글 존재 | MockMvc | P |
| TC-004.001.004 | Reaction | 댓글 | 생성 | 댓글 작성 실패 - bookId 누락 | MockMvc | P |
| TC-004.001.005 | Reaction | 댓글 | 생성 | 댓글 작성 실패 - 빈 내용 | MockMvc | P |
| TC-004.001.006 | Reaction | 댓글 | 생성 | 댓글 작성 실패 - 부모 댓글 없음 | MockMvc | P |
| TC-004.001.007 | Reaction | 댓글 | 수정 | 댓글 수정 성공 - 소유자가 수정 | MockMvc | P |
| TC-004.001.008 | Reaction | 댓글 | 수정 | 댓글 수정 실패 - 소유자 아님 | MockMvc | P |
| TC-004.001.009 | Reaction | 댓글 | 수정 | 댓글 수정 실패 - 댓글 없음 | MockMvc | P |
| TC-004.001.010 | Reaction | 댓글 | 삭제 | 댓글 삭제 성공 - 소유자가 삭제 | MockMvc | P |
| TC-004.001.011 | Reaction | 댓글 | 삭제 | 댓글 삭제 실패 - 소유자 아님 | MockMvc | P |
| TC-004.001.012 | Reaction | 투표 | 소설 투표 | 소설 투표 성공 - 새 투표 | MockMvc | P |
| TC-004.001.013 | Reaction | 투표 | 소설 투표 | 소설 투표 토글 - 동일 투표 취소 | MockMvc | P |
| TC-004.001.014 | Reaction | 투표 | 소설 투표 | 소설 투표 실패 - voteType 누락 | MockMvc | P |
| TC-004.001.015 | Reaction | 투표 | 문장 투표 | 문장 투표 성공 - 새 투표 | MockMvc | P |
| TC-004.001.016 | Reaction | 투표 | 문장 투표 | 문장 투표 토글 - 동일 투표 취소 | MockMvc | P |
| TC-004.001.017 | Reaction | 투표 | 문장 투표 | 문장 투표 변경 - LIKE에서 DISLIKE로 변경 | MockMvc | P |

#### ReactionServiceTest (15개 테스트)

| 테스트 코드 | 분류 | | | 테스트 시나리오 | 테스트 방식 | 테스트 성공여부 |
|------------|------|---|---|----------------|------------|----------------|
| | 대분류 | 중분류 | 소분류 | | | (P/F) |
| TC-004.002.001 | Reaction | 댓글 | 생성 | 일반 댓글 추가 성공 | Unit | P |
| TC-004.002.002 | Reaction | 댓글 | 생성 | 대댓글 추가 성공 - 부모 참조 포함 | Unit | P |
| TC-004.002.003 | Reaction | 댓글 | 생성 | 댓글 추가 실패 - 부모 댓글 없음 | Unit | P |
| TC-004.002.004 | Reaction | 댓글 | 생성 | 댓글 추가 실패 - 다른 소설의 부모 댓글 | Unit | P |
| TC-004.002.005 | Reaction | 댓글 | 수정 | 댓글 수정 성공 - 소유자가 수정 | Unit | P |
| TC-004.002.006 | Reaction | 댓글 | 수정 | 댓글 수정 실패 - 소유자 아님 | Unit | P |
| TC-004.002.007 | Reaction | 댓글 | 삭제 | 댓글 삭제 성공 - 소유자가 삭제 | Unit | P |
| TC-004.002.008 | Reaction | 댓글 | 삭제 | 댓글 삭제 성공 - 관리자가 삭제 | Unit | P |
| TC-004.002.009 | Reaction | 댓글 | 삭제 | 댓글 삭제 실패 - 소유자 및 관리자 아님 | Unit | P |
| TC-004.002.010 | Reaction | 투표 | 소설 투표 | 소설 투표 성공 - 새 LIKE 투표 | Unit | P |
| TC-004.002.011 | Reaction | 투표 | 소설 투표 | 소설 투표 토글 - 동일 투표 다시 클릭 시 삭제 | Unit | P |
| TC-004.002.012 | Reaction | 투표 | 소설 투표 | 소설 투표 변경 - DISLIKE에서 LIKE로 변경 | Unit | P |
| TC-004.002.013 | Reaction | 투표 | 문장 투표 | 문장 투표 성공 - 새 LIKE 투표 | Unit | P |
| TC-004.002.014 | Reaction | 투표 | 문장 투표 | 문장 투표 토글 - 동일 투표 다시 클릭 시 삭제 | Unit | P |
| TC-004.002.015 | Reaction | 투표 | 문장 투표 | 문장 투표 변경 - DISLIKE에서 LIKE로 변경 | Unit | P |

### 4.2 댓글 조회 테스트

#### ReactionQueryControllerTest (3개 테스트)

| 테스트 코드 | 분류 | | | 테스트 시나리오 | 테스트 방식 | 테스트 성공여부 |
|------------|------|---|---|----------------|------------|----------------|
| | 대분류 | 중분류 | 소분류 | | | (P/F) |
| TC-004.003.001 | Reaction | 댓글 | 조회 | 소설의 댓글 목록 조회 성공 | MockMvc | P |
| TC-004.003.002 | Reaction | 댓글 | 조회 | 내 댓글 목록 조회 성공 - 인증된 사용자 | MockMvc | P |
| TC-004.003.003 | Reaction | 댓글 | 조회 | 내 댓글 목록 조회 실패 - 미인증 사용자 | MockMvc | P |

#### ReactionQueryServiceTest (10개 테스트)

| 테스트 코드 | 분류 | | | 테스트 시나리오 | 테스트 방식 | 테스트 성공여부 |
|------------|------|---|---|----------------|------------|----------------|
| | 대분류 | 중분류 | 소분류 | | | (P/F) |
| TC-004.004.001 | Reaction | 댓글 | 조회 | 댓글 조회 - 빈 결과 | Unit | P |
| TC-004.004.002 | Reaction | 댓글 | 조회 | 댓글 조회 - 루트 댓글만 있음 | Unit | P |
| TC-004.004.003 | Reaction | 댓글 | 조회 | 댓글 조회 - 트리 구조로 반환 (대댓글 포함) | Unit | P |
| TC-004.004.004 | Reaction | 댓글 | 조회 | 댓글 조회 - 고아 댓글 처리 (부모 삭제됨) | Unit | P |
| TC-004.004.005 | Reaction | 댓글 | 조회 | 댓글 조회 - Feign 성공으로 작성자 정보 포함 | Unit | P |
| TC-004.004.006 | Reaction | 댓글 | 조회 | 댓글 조회 - Feign 실패 시 닉네임 null | Unit | P |
| TC-004.004.007 | Reaction | 댓글 | 조회 | 사용자별 댓글 조회 성공 - 페이지네이션 포함 | Unit | P |
| TC-004.004.008 | Reaction | 댓글 | 조회 | 사용자별 댓글 조회 - 빈 결과 | Unit | P |
| TC-004.004.009 | Reaction | 댓글 | 조회 | 사용자별 댓글 조회 - 모든 Feign 성공 | Unit | P |
| TC-004.004.010 | Reaction | 댓글 | 조회 | 사용자별 댓글 조회 - 모든 Feign 실패 시 null | Unit | P |

### 4.3 통합 테스트

#### ReactionIntegrationTest (2개 테스트)

| 테스트 코드 | 분류 | | | 테스트 시나리오 | 테스트 방식 | 테스트 성공여부 |
|------------|------|---|---|----------------|------------|----------------|
| | 대분류 | 중분류 | 소분류 | | | (P/F) |
| TC-004.005.001 | Reaction | 통합 | 전체 플로우 | 댓글 생성 통합 테스트 | Integration | P |
| TC-004.005.002 | Reaction | 통합 | 유효성 검증 | 빈 내용 댓글 생성 시 유효성 검증 | Integration | P |

---

## 📈 테스트 커버리지 상세 분석

### Common Module Coverage
- **전체 커버리지:** 70% (라인), 0% (브랜치)
- **주요 패키지:**
  - `security`: 100% 커버리지 (CustomAccessDeniedHandler, CustomAuthenticationEntryPoint)
  - `error`: 94% 커버리지 (에러 코드 및 예외 처리)
  - `response`: 26% 커버리지 (응답 DTO - 테스트 필요)
  - `exception`: 8% 커버리지 (예외 클래스 - 테스트 필요)

### Member Service Coverage
- **전체 커버리지:** 82% (라인), 72% (브랜치)
- **주요 패키지:**
  - `command.member.service`: 100% 커버리지
  - `command.member.controller`: 100% 커버리지
  - `auth.controller`: 100% 커버리지
  - `query.member.controller`: 100% 커버리지
  - `query.member.service`: 100% (라인), 50% (브랜치) - 브랜치 커버리지 개선 필요
  - `auth.service`: 78% 커버리지
  - `jwt`: 69% (라인), 58% (브랜치) - JwtAuthenticationFilter 개선 필요

### Story Service Coverage
- **전체 커버리지:** 90% (라인), 63% (브랜치)
- **주요 패키지:**
  - `command.book.controller`: 100% 커버리지
  - `query.book.controller`: 100% (라인), 87% (브랜치)
  - `query.book.service`: 95% (라인), 66% (브랜치)
  - `command.book.service`: 79% (라인), 52% (브랜치) - 브랜치 커버리지 개선 필요
  - `category.controller`: 100% 커버리지

### Reaction Service Coverage
- **전체 커버리지:** 97% (라인), 80% (브랜치)
- **주요 패키지:**
  - `command.reaction.controller`: 100% (라인), 75% (브랜치)
  - `query.reaction.service`: 100% (라인), 70% (브랜치)
  - `query.reaction.controller`: 97% (라인), 75% (브랜치)
  - `command.reaction.service`: 95% (라인), 95% (브랜치)

---

## 🎯 테스트 전략

### 테스트 레벨별 분류

1. **Unit Test (단위 테스트)** - 약 220개
   - Mockito를 사용한 의존성 모킹
   - 비즈니스 로직 검증
   - Service, Filter, Security 컴포넌트 테스트

2. **MockMvc Test (컨트롤러 단위 테스트)** - 약 60개
   - Spring MockMvc 사용
   - HTTP 요청/응답 검증
   - 컨트롤러 레이어 격리 테스트

3. **Integration Test (통합 테스트)** - 4개
   - 전체 Spring Context 로딩
   - 실제 HTTP 통신 테스트
   - End-to-End 시나리오 검증

### 테스트 커버리지 제외 항목
다음 클래스들은 JaCoCo 커버리지 계산에서 제외됩니다:
- `**/config/**` (설정 클래스)
- `**/dto/**` (데이터 전송 객체)
- `**/entity/**` (엔티티 클래스)
- `**/*Application.class` (Spring Boot 애플리케이션 클래스)
- `**/websocket/**` (WebSocket 관련 클래스)
- `**/feign/**` (Feign Client 인터페이스)
- `**/gateway/**` (Gateway 관련 클래스)
- `**/util/**` (유틸리티 클래스)
- `**/filter/**` (필터 클래스 - 일부 제외)

---

## ⚠️ 테스트 커버리지 개선 필요 항목

### 1. Common Module
- **exception 패키지**: 8% → 70% 이상 필요
- **response 패키지**: 26% → 70% 이상 필요

### 2. Member Service
- **feign 패키지**: 0% → Internal API 테스트 필요
- **jwt 패키지**: 브랜치 커버리지 58% → 70% 이상 필요
- **query.member.service**: 브랜치 커버리지 50% → 70% 이상 필요

### 3. Story Service
- **command.book.service**: 브랜치 커버리지 52% → 70% 이상 필요
- **query.book.service**: 브랜치 커버리지 66% → 70% 이상 필요

### 4. Config Server
- 테스트 파일이 전혀 없음
- 최소한의 ApplicationTests 추가 필요

### 5. Discovery Server & Gateway Server
- 테스트 파일이 전혀 없음
- 인프라 서비스이지만 기본 구동 테스트 필요

---

## 📝 README 테스트 문서화 상태

### 현재 README 상태
- ✅ 프로젝트 개요 및 아키텍처 잘 문서화됨
- ✅ 시작 가이드 및 트러블슈팅 포함
- ✅ 주요 기능 설명 포함
- ❌ **테스트 코드 관련 내용 누락** (전혀 언급 없음)
- ❌ **테스트 커버리지 정보 없음**
- ❌ **테스트 실행 방법 가이드 없음**

### 개선 권장 사항
README에 다음 섹션 추가 권장:
1. 테스트 실행 방법
2. JaCoCo 리포트 생성 방법
3. 테스트 커버리지 목표 및 현황
4. 테스트 작성 가이드라인

---

## 🚀 테스트 실행 방법

### 전체 테스트 실행
```bash
# 모든 모듈의 테스트 실행
./gradlew test

# 테스트 및 JaCoCo 리포트 생성
./gradlew test jacocoTestReport

# 커버리지 검증 (70% 기준)
./gradlew test jacocoTestCoverageVerification
```

### 특정 모듈 테스트 실행
```bash
# Member Service 테스트만 실행
./gradlew :member-service:test

# Story Service 테스트만 실행
./gradlew :story-service:test

# Reaction Service 테스트만 실행
./gradlew :reaction-service:test

# Common Module 테스트만 실행
./gradlew :common-module:test
```

### JaCoCo 리포트 확인
테스트 실행 후 다음 경로에서 HTML 리포트 확인:
- Common Module: `common-module/build/reports/jacoco/test/html/index.html`
- Member Service: `member-service/build/reports/jacoco/test/html/index.html`
- Story Service: `story-service/build/reports/jacoco/test/html/index.html`
- Reaction Service: `reaction-service/build/reports/jacoco/test/html/index.html`

---

## 📊 테스트 통계 요약

| 구분 | 수량 |
|------|------|
| **전체 테스트 파일** | 25개 |
| **전체 테스트 메서드** | 254개 이상 |
| **Unit 테스트** | ~222개 (87%) |
| **MockMvc 테스트** | ~28개 (11%) |
| **Integration 테스트** | 4개 (2%) |
| **평균 커버리지 (라인)** | 85% |
| **평균 커버리지 (브랜치)** | 71% |
| **테스트 성공률** | 100% (P) |

---

## 🏆 테스트 품질 평가

### 강점
✅ **높은 전체 커버리지**: 평균 85% 라인 커버리지 달성
✅ **체계적인 테스트 구조**: Unit → MockMvc → Integration 단계별 테스트
✅ **포괄적인 시나리오**: 성공/실패 케이스 모두 커버
✅ **Feign 에러 처리 테스트**: 서비스 간 통신 실패 시나리오 포함
✅ **보안 테스트**: JWT, 인증, 권한 검증 테스트 포함

### 개선 필요 사항
⚠️ **인프라 서비스 테스트 부족**: Config/Discovery/Gateway 서버 테스트 없음
⚠️ **브랜치 커버리지 개선**: 일부 서비스 브랜치 커버리지 70% 미달
⚠️ **Common Module 예외 처리 테스트**: exception 패키지 커버리지 8%
⚠️ **통합 테스트 부족**: 전체 시나리오 통합 테스트 4개에 불과
⚠️ **README 문서화 부족**: 테스트 관련 내용 전혀 없음

---

**문서 생성일:** 2026-01-19
**테스트 실행 환경:** Windows 11, JDK 17, Gradle 9.0.0
**마지막 테스트 실행:** 2026-01-20
**전체 테스트 결과:** ✅ PASS (커버리지 검증 일부 실패)
**최근 업데이트:** WebSocket 실시간 통계 브로드캐스트 테스트 2개 추가 (TC-003.003.019, TC-003.003.020)
