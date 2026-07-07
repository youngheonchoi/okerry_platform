# TASK 009. 직접 로그인 후 사용자별 서비스 목록 제공


## 목적

사용자가 OAuth2/OIDC Authorization Request 없이 `okerry platform` 로그인 화면에 직접 접근해 인증한 경우,
인증된 계정이 실제로 접근할 수 있는 서비스만 `/portal/services`에 표시한다.

OAuth2/OIDC Client에서 시작된 로그인은 기존 Authorization Request로 복귀해야 하며,
직접 로그인 흐름과 OAuth2/OIDC 로그인 흐름이 서로의 성공 URL을 덮어쓰지 않도록 분리한다.


## 현재 문제

* 직접 로그인 성공 시 `/portal/services`로 이동하도록 설정되어 있으나 서비스 목록이 DB와 연결되어 있지 않다.
* `PortalServiceListController`가 중복된 임시 서비스 목록과 `/portal/services` 자기 자신을 이동 주소로 반환한다.
* `registered_services`, `service_users`에 저장된 사용자별 서비스 접근 상태가 화면에 반영되지 않는다.
* 사용자 또는 서비스 데이터가 없거나 DB 조회가 실패할 때 정상적인 빈 화면이나 안전한 오류 화면 대신 예외가 발생할 수 있다.
* OAuth2/OIDC 로그인 요청과 직접 로그인 요청의 성공 후 이동 경로를 회귀 테스트로 명확히 보장하지 않는다.


## 사용자 흐름

### 직접 로그인

```text
사용자
  -> /login 직접 접근
  -> 계정 인증
  -> /portal/services
  -> 인증 계정에 연결되고 현재 이용 가능한 서비스만 조회
  -> 서비스 선택
  -> registered_services.login_start_url로 이동
  -> 선택한 Client가 정상 OAuth2/OIDC Authorization Request 시작
```

### OAuth2/OIDC Client에서 시작한 로그인

```text
사용자
  -> Client 서비스
  -> /oauth2/authorize
  -> /login
  -> 계정 인증
  -> 저장된 /oauth2/authorize 요청으로 복귀
  -> Client Callback으로 Authorization Code 전달
```

OAuth2/OIDC Client에서 시작한 로그인 성공 후에는 `/portal/services`를 거치지 않는다.


## 서비스 접근 가능 기준

목록에는 다음 조건을 모두 충족하는 서비스만 표시한다.

* 현재 로그인 사용자가 `auth_users`에 존재한다.
* 사용자 계정 상태가 `ACTIVE`이다.
* `service_users.user_id`가 현재 사용자와 연결되어 있다.
* `service_users.deleted_at`이 `NULL`이다.
* `service_users.status`가 `ACTIVE` 또는 `PENDING_PROFILE`이다.
* 연결된 `registered_services.enabled`가 `TRUE`이다.

`PENDING_PROFILE` 사용자는 해당 서비스가 관리하는 추가 프로필 등록을 완료할 수 있어야 하므로 목록에 포함한다.
`SUSPENDED`, `WITHDRAWN` 상태의 연결은 표시하지 않는다.

목록 정렬은 `registered_services.display_order ASC`, 동일 순서에서는 `registered_services.name ASC`로 고정한다.


## 구현 범위

* `registered_services`에 대응하는 읽기 전용 도메인 모델 또는 조회 모델 구현
* `service_users`에 대응하는 사용자별 서비스 접근 조회 구현
* 로그인 principal의 이메일을 정규화해 `auth_users`를 조회하고 내부 사용자 ID로 서비스 관계 조회
* Controller에 하드코딩된 `availableServices()`와 중복 임시 데이터 제거
* 화면에 서비스명, 설명, 로고, 서비스 상태 및 이동 URL 제공
* 서비스 이동 URL로 `registered_services.login_start_url` 사용
* OAuth2 Client에서 시작된 Saved Request는 기존 `/oauth2/authorize` 흐름으로 복귀
* Saved Request가 없는 직접 로그인만 `/portal/services`로 이동
* 접근 가능한 서비스가 없을 때 오류가 아니라 명확한 빈 상태 화면 표시
* 인증 사용자와 DB 사용자가 일치하지 않을 때 내부 정보를 노출하지 않는 안전한 오류 처리
* DB 조회 오류를 Whitelabel 상세 예외로 사용자에게 노출하지 않는 공통 오류 처리 검토
* 사용자별 서비스 조회와 로그인 성공 분기 자동 테스트 추가
* `DATABASE.md`의 테이블 및 제약 조건과 Entity/Repository 매핑 일치 확인
* UI 수정 전 `/doc/design/DESIGN.md` 확인 및 Okerry UI 클래스 우선 사용


## 제외 범위

* 서비스별 추가 프로필 데이터 관리
* 서비스 자체 회원가입 및 자체 세션 생성
* OAuth2 Client 등록·수정 관리 화면
* `registered_services` 또는 `service_users` 관리 CRUD 화면
* Console 관리자 권한 Claim 또는 관리자 권한 조회 API 구현
* 특정 서비스의 비즈니스 권한을 Platform에서 판정하는 기능
* Platform이 Client Callback을 대신 처리하는 기능


## 보안 및 오류 처리 기준

* 요청 파라미터로 전달된 사용자 ID 또는 서비스 URL을 신뢰하지 않는다.
* 현재 Spring Security principal로 식별한 사용자 관계만 조회한다.
* 서비스 이동 주소는 요청값이 아니라 DB의 `login_start_url`을 사용한다.
* 비활성화되거나 사용자에게 연결되지 않은 서비스는 목록 및 이동 대상에서 제외한다.
* 목록 조회 과정에서 비밀번호, Client Secret, Authorization Code 또는 Token을 조회하거나 로그에 남기지 않는다.
* DB 예외 메시지, SQL, 스택 트레이스를 사용자 화면에 그대로 노출하지 않는다.
* 서비스 카드에서 임의의 외부 URL을 직접 입력받아 리다이렉트하지 않는다.


## 구현 단계

1. `/doc/architecture/DATABASE.md`의 `auth_users`, `registered_services`, `service_users` 구조를 재확인한다.
2. 현재 직접 로그인과 OAuth2/OIDC Saved Request 복귀 동작을 테스트로 고정한다.
3. 사용자별 접근 가능 서비스를 한 번의 명확한 조회로 반환하는 Repository를 구현한다.
4. Controller의 임시 서비스 목록을 Application Service 또는 조회 Service 호출로 교체한다.
5. 서비스 목록 화면을 DB 조회 결과, 빈 상태, `PENDING_PROFILE` 상태에 맞게 수정한다.
6. 사용자 없음, 서비스 없음, 비활성 서비스, 중지된 연결 및 DB 오류 시나리오를 검증한다.
7. 기존 로그인, 회원가입 및 OAuth2/OIDC 통합 테스트를 포함한 전체 테스트를 실행한다.
8. 실제 Neon 데이터로 직접 로그인 후 서비스 선택 및 OAuth2/OIDC 재진입 흐름을 확인한다.


## 작업 체크리스트

* [x] 직접 로그인 성공 시 `/portal/services`로 이동
* [x] OAuth2/OIDC 로그인 성공 시 저장된 Authorization Request로 복귀
* [x] `registered_services` 조회 모델 및 Repository 구현
* [x] `service_users` 기반 사용자별 접근 가능 서비스 조회 구현
* [x] Controller 하드코딩 서비스 목록 제거
* [x] `enabled`, 서비스 사용자 상태 및 삭제 여부 필터 적용
* [x] `display_order`, 서비스명 정렬 적용
* [x] 서비스 카드 이동 주소에 `login_start_url` 적용
* [x] 접근 가능 서비스가 없는 경우 빈 상태 제공
* [x] 사용자 및 DB 오류에 대한 안전한 처리 적용
* [x] 사용자별 서비스 조회 단위/통합 테스트 추가
* [x] 직접 로그인과 OAuth2/OIDC 로그인 분기 회귀 테스트 추가
* [ ] 전체 테스트 실행
* [ ] 실제 Neon 환경 검증


## 완료 조건

* [ ] 직접 로그인한 사용자가 오류 없이 `/portal/services`에 도달한다.
* [ ] 목록에는 해당 사용자에게 연결된 이용 가능 서비스만 표시된다.
* [ ] 다른 사용자에게만 연결된 서비스와 비활성 서비스는 표시되지 않는다.
* [ ] `SUSPENDED`, `WITHDRAWN`, 삭제된 서비스 사용자 연결은 표시되지 않는다.
* [ ] `PENDING_PROFILE` 서비스는 표시되고 Client 로그인 시작 URL로 이동할 수 있다.
* [ ] 서비스가 하나도 없는 사용자에게 예외 대신 빈 상태 화면이 표시된다.
* [ ] 서비스 선택 시 Client의 `login_start_url`로 이동해 표준 OAuth2/OIDC 요청이 시작된다.
* [ ] OAuth2/OIDC 요청에서 시작된 로그인은 서비스 목록이 아니라 원래 Authorization Request로 복귀한다.
* [ ] 내부 DB 예외나 보안 관련 값이 사용자 화면 또는 로그에 노출되지 않는다.
* [ ] 관련 자동 테스트와 기존 전체 테스트가 통과한다.
* [ ] 실제 Neon 환경에서 직접 로그인부터 서비스 선택까지 검증된다.


## 진행 로그

### 2026-07-06

* 직접 로그인 후 사용자별 서비스 목록으로 이동하는 과정에서 오류가 발생한다는 현상을 확인함.
* 현재 Controller의 하드코딩 목록을 제거하고 `service_users`, `registered_services` 기반으로 전환하는 요구사항을 정의함.
* 직접 로그인 성공 경로와 OAuth2/OIDC Saved Request 복귀 경로를 분리해 검증하도록 범위를 확정함.

### 2026-07-07

* `registered_services`, `service_users` JPA Entity와 Repository를 추가하고, 사용자별 접근 가능 서비스 조회를 `PortalServiceListQuery`로 분리함.
* `PortalServiceListController`의 하드코딩 임시 서비스 목록을 제거하고 인증 principal 이메일 기준 DB 조회 결과를 사용하도록 변경함.
* 서비스 목록 화면에 로고, 상태 뱃지, `PENDING_PROFILE`, 빈 상태, 조회 실패 안내를 추가하고 이동 URL은 `registered_services.login_start_url`을 사용하도록 변경함.
* 포털 통합 테스트를 DB 기반 필터링, 정렬, 빈 상태, principal 불일치 시나리오로 갱신하고 OAuth2/OIDC 로그인은 저장된 Authorization Request로 복귀하는 단언을 추가함.
* 플랫폼 루트(`/`) 직접 접근 시 404 Whitelabel이 발생하지 않도록 `/portal/services` 리다이렉트 매핑과 회귀 테스트를 추가함.
* `/error`가 로그인 성공 후 Saved Request로 복귀되어 `status=999` Whitelabel이 표시되지 않도록 성공 핸들러와 공통 오류 화면을 추가함.
* `./gradlew test`는 실행했으나 현재 환경에 `JAVA_HOME` 및 `java` 실행 파일이 없어 전체 테스트를 완료하지 못함.
