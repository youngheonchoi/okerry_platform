# TASK 010. 서비스 접근 권한 없음 시 접근 신청 메일 발송


## 목적

OAuth2/OIDC Client 서비스에서 `okerry platform`으로 로그인 요청이 들어왔지만,
인증된 사용자가 해당 서비스에 대한 접근 권한을 가지고 있지 않은 경우 Authorization Code를 발급하지 않는다.

대신 사용자가 접근 신청 여부를 명확히 확인한 뒤,
관리자에게 서비스 접근 요청 메일을 발송할 수 있는 흐름을 제공한다.


## 현재 문제

* `okerry food` 같은 서비스가 `registered_services`에 등록되어 있어도 사용자별 `service_users` 매핑이 없으면 해당 사용자는 서비스 접근 권한이 없다.
* 사용자가 서비스에서 시작한 OAuth2/OIDC 로그인 요청으로 platform에 도착했을 때, 접근 권한이 없다는 상황을 사용자에게 명확히 안내하는 흐름이 필요하다.
* 접근 권한이 없는 사용자가 관리자에게 어떤 서비스 접근을 요청하는지 수동으로 설명해야 한다.
* 메일 요청 흐름 없이 바로 실패시키면 사용자가 다음 행동을 알기 어렵다.


## 사용자 흐름

```text
사용자
  -> Client 서비스에서 로그인 시도
  -> okerry platform /oauth2/authorize 요청
  -> platform 로그인 화면
  -> 계정 인증 성공
  -> platform이 요청 client_id에 해당하는 registered_services 조회
  -> 현재 사용자와 해당 서비스의 service_users 접근 권한 확인
  -> 접근 권한 없음
  -> 서비스 접근 신청 안내 화면
  -> 사용자가 신청하기 선택
  -> 관리자에게 접근 요청 메일 발송
  -> 사용자에게 신청 접수 안내 화면 표시
```

접근 권한이 없는 경우에는 Client Callback으로 Authorization Code를 전달하지 않는다.
단, 접근 신청 메일 발송은 platform에 정상 등록된 활성 서비스에 대해서만 제공한다.


## 서비스 요청 유효성 기준

다음 중 하나라도 해당하면 정상적인 서비스 접근 신청 대상으로 보지 않는다.

* 요청 `client_id`에 해당하는 `registered_services`가 존재하지 않는다.
* 연결된 `registered_services.enabled`가 `FALSE`이다.

이 경우에는 사용자에게 접근 신청 화면을 제공하지 않는다.
알 수 없거나 비활성화된 서비스 요청으로 처리하고, Authorization Code를 발급하지 않는다.


## 접근 신청 가능 기준

다음 조건을 모두 충족할 때만 서비스 접근 신청 화면과 관리자 메일 발송을 제공한다.

* 요청 `client_id`에 해당하는 `registered_services`가 존재한다.
* 연결된 `registered_services.enabled`가 `TRUE`이다.
* 현재 로그인 사용자가 `auth_users`에 존재한다.
* 사용자 계정 상태가 `ACTIVE`이다.
* 현재 사용자와 요청 서비스 사이에 접근 가능한 `service_users` 매핑이 없다.

접근 가능한 `service_users` 매핑이 없다는 것은 다음 중 하나를 의미한다.

* 현재 사용자와 요청 서비스 사이에 `service_users` 매핑이 없다.
* `service_users.deleted_at`이 `NULL`이 아니다.
* `service_users.status`가 `ACTIVE` 또는 `PENDING_PROFILE`이 아니다.

`PENDING_PROFILE`은 서비스 자체 추가 프로필 등록을 진행할 수 있어야 하므로 접근 가능한 상태로 본다.


## 접근 신청 불가 기준

다음 중 하나라도 해당하면 접근 신청 메일을 발송하지 않는다.

* 요청 `client_id`에 해당하는 `registered_services`가 존재하지 않는다.
* 연결된 `registered_services.enabled`가 `FALSE`이다.
* 현재 로그인 사용자가 `auth_users`에 존재하지 않는다.
* 사용자 계정 상태가 `ACTIVE`가 아니다.
* OAuth2/OIDC 요청 자체가 유효하지 않다.


## 구현 범위

* OAuth2/OIDC Authorization Request에서 요청된 `client_id`로 `registered_services`를 식별
* `registered_services` 미등록 또는 비활성 서비스 요청은 접근 신청 대상에서 제외
* 로그인 성공 후 Authorization Code 발급 전에 현재 사용자와 요청 서비스의 접근 권한 검증
* 접근 권한이 없을 때 Authorization Code 발급 차단
* 접근 신청 가능한 접근 권한 없음 상태에서만 안내 화면 제공
* 안내 화면에 요청 서비스명, 사용자 이메일, 신청 안내 문구 표시
* 사용자가 명시적으로 신청하기 버튼을 누른 경우에만 관리자 메일 발송
* 신청 POST 요청에 CSRF 보호 적용
* 관리자 메일 본문에 접근 판단에 필요한 최소 정보 포함
* 메일 발송 성공 시 신청 접수 안내 화면 제공
* 메일 발송 실패 시 내부 예외 상세를 노출하지 않는 안전한 오류 안내 제공
* 동일한 OAuth2/OIDC 요청 흐름에서 서비스 식별 정보가 위조되지 않도록 서버 측 상태 기반으로 처리
* 관련 Controller, Service, Mail 발송 로직 테스트 추가
* UI 수정 전 `/doc/design/DESIGN.md` 확인 및 Okerry UI 클래스 우선 사용


## 제외 범위

* Console 관리자 화면 구현
* Console에서 사용자별 접근 가능 서비스 등록 기능 구현
* Console에서 승인 완료 메일을 발송하는 기능
* `service_access_requests` 같은 접근 요청 이력 테이블 추가
* 접근 요청 승인, 반려, 취소 상태 관리
* 관리자가 메일 링크를 눌러 즉시 승인하는 기능
* 사용자가 접근 신청 사유를 입력하는 기능
* 서비스 자체 회원가입 또는 서비스별 프로필 등록 기능
* 접근 권한이 없는 사용자에게 임시 Authorization Code 또는 Token을 발급하는 기능


## 관리자 메일 내용 기준

메일은 접근 권한 부여 요청을 관리자에게 알리는 용도로만 사용한다.
실제 접근 권한 부여는 Console 또는 DB 관리 절차에서 별도로 수행한다.

메일에는 다음 정보를 포함한다.

* 요청 사용자 이메일
* 요청 사용자 이름
* 요청 서비스명
* 요청 서비스 키
* 요청 서비스 OAuth2 Client ID
* 요청 시각
* 사용자가 접근 권한을 보유하지 않아 Authorization Code 발급이 차단되었다는 설명

메일에는 다음 정보를 포함하지 않는다.

* 비밀번호
* Access Token
* Refresh Token
* ID Token
* Authorization Code
* Client Secret
* Private Key
* 내부 예외 스택 트레이스


## 보안 및 오류 처리 기준

* 접근 권한이 없는 사용자에게 OAuth2/OIDC 인증 성공 결과를 Client로 전달하지 않는다.
* `registered_services`에 등록되지 않은 `client_id`는 접근 신청 대상으로 취급하지 않는다.
* 비활성화된 `registered_services`는 접근 신청 대상으로 취급하지 않는다.
* 요청 파라미터로 전달된 서비스명, 사용자 이메일, redirect_uri를 그대로 신뢰하지 않는다.
* 서비스 정보는 검증된 Authorization Request의 `client_id`와 DB의 `registered_services`를 기준으로 조회한다.
* 사용자 정보는 현재 Spring Security principal과 DB의 `auth_users`를 기준으로 조회한다.
* 메일 수신자 주소는 서버 설정값을 사용하고 요청 파라미터로 받지 않는다.
* 메일 본문과 로그에 보안 민감 정보를 남기지 않는다.
* 접근 권한 없음은 인증 실패와 구분하되, 내부 권한 테이블 구조나 예외 상세를 노출하지 않는다.
* 메일 발송 실패가 발생해도 OAuth2 Code 발급으로 우회하지 않는다.


## 구현 단계

1. `/doc/architecture/DATABASE.md`의 `registered_services`, `service_users`, `oauth2_registered_client` 구조를 확인한다.
2. OAuth2/OIDC Authorization Request에서 요청 Client를 `registered_services.oauth2_client_id`와 연결하는 방법을 확정한다.
3. 로그인 성공 후 Authorization Code 발급 전 사용자별 서비스 접근 권한을 검증하는 위치를 결정한다.
4. 접근 권한 없음 안내 화면과 신청 POST 엔드포인트를 설계한다.
5. 신청 POST가 서버 측에 저장된 요청 서비스 정보를 사용하도록 상태 전달 방식을 정한다.
6. 관리자 메일 발송 서비스와 설정값을 추가한다.
7. 접근 권한 있음, 접근 신청 가능 상태, 미등록 서비스, 비활성 서비스, 메일 발송 성공, 메일 발송 실패, 요청 위조 시나리오를 테스트한다.
8. 실제 서비스 로그인 요청에서 권한 없음 시 Code가 발급되지 않고 메일 신청 흐름으로 이동하는지 확인한다.


## 작업 체크리스트

* [x] OAuth2/OIDC 요청 `client_id`와 `registered_services` 연결 기준 확정
* [x] 미등록 서비스와 비활성 서비스는 접근 신청 대상에서 제외
* [x] Authorization Code 발급 전 서비스 접근 권한 검증 추가
* [x] 접근 권한 없음 시 Code 발급 차단
* [x] 접근 신청 가능한 경우에만 서비스 접근 신청 안내 화면 추가
* [x] 신청하기 POST 엔드포인트 추가
* [x] 신청 POST CSRF 보호 적용
* [x] 관리자 메일 발송 로직 추가
* [x] 메일 발송 성공 안내 화면 추가
* [x] 메일 발송 실패 시 안전한 오류 안내 추가
* [x] 보안 민감 정보가 화면, 메일, 로그에 노출되지 않는지 확인
* [x] 관련 자동 테스트 추가
* [ ] 실제 OAuth2/OIDC 로그인 요청으로 권한 없음 흐름 검증


## 완료 조건

* [ ] 접근 권한이 있는 사용자는 기존 OAuth2/OIDC Authorization Flow로 정상 복귀한다.
* [ ] 접근 권한이 없는 사용자는 Authorization Code를 발급받지 못한다.
* [ ] 등록된 활성 서비스에 대한 접근 권한이 없는 사용자에게만 요청 서비스 기준의 접근 신청 안내 화면이 표시된다.
* [ ] `registered_services`에 없는 서비스 요청은 접근 신청 화면과 메일 발송 대상으로 처리되지 않는다.
* [ ] 비활성화된 서비스 요청은 접근 신청 화면과 메일 발송 대상으로 처리되지 않는다.
* [ ] 사용자가 신청하기를 선택한 경우에만 관리자 메일이 발송된다.
* [ ] 관리자 메일에는 사용자와 서비스 식별에 필요한 최소 정보가 포함된다.
* [ ] 관리자 메일과 로그에 Token, Authorization Code, Client Secret 등 보안 민감 정보가 포함되지 않는다.
* [ ] 메일 발송 실패 시 내부 예외 상세가 사용자 화면에 노출되지 않는다.
* [ ] 요청 파라미터 조작으로 다른 서비스에 대한 신청 메일을 보내지 못한다.
* [ ] Console 관련 승인, 등록, 승인 완료 메일 기능은 이 작업에 포함되지 않는다.
* [ ] 관련 자동 테스트가 통과한다.


## 진행 로그

### 2026-07-08

* 기업 SSO 방식에서는 platform 회원이더라도 특정 서비스 접근 권한이 없으면 해당 서비스 로그인은 차단하는 것으로 정책을 정리함.
* 접근 권한이 없는 경우 자동으로 `service_users` 매핑을 만들지 않고, 사용자 확인 후 관리자에게 접근 요청 메일만 발송하는 MVP 흐름으로 범위를 확정함.
* Console에서 접근 가능 서비스를 등록하고 승인 완료 메일을 발송하는 기능은 별도 작업으로 분리하기로 함.
* `registered_services.oauth2_client_id`와 OAuth2 `client_id`를 연결 기준으로 사용하도록 구현함.
* 등록되지 않았거나 비활성화된 서비스는 접근 신청 대상에서 제외하고 별도 안내 화면으로 차단하도록 구현함.
* 등록된 활성 서비스에 대해 사용자 `service_users` 접근 권한이 없으면 Authorization Code 발급 전 신청 화면으로 전환하도록 구현함.
* 신청하기 POST에서 서버 세션에 저장된 요청 정보를 기준으로 관리자 접근 요청 메일을 발송하도록 구현함.
* 권한 없음 및 미등록 서비스 흐름에 대한 OAuth2 통합 테스트를 추가함.
* `./gradlew test --tests com.okerry.platform.config.AuthorizationServerIntegrationTest` 실행을 시도했으나 현재 쉘에 `JAVA_HOME` 및 `java` 실행 파일이 없어 테스트를 완료하지 못함.
