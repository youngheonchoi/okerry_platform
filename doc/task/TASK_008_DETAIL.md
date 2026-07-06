# TASK 008. OAuth2/OIDC Authorization Server 구현


## 목적

`okerry platform`을 표준 OAuth2/OIDC Authorization Server 및 Identity Provider로 구성한다.
등록된 클라이언트 서비스가 Authorization Code Flow로 사용자를 인증하고, 발급받은 토큰과 OIDC 사용자 식별 정보를 이용할 수 있도록 한다.


## 구현 범위

* Spring Authorization Server 의존성과 기본 보안 필터 체인 구성
* OAuth2 Authorization Server 및 OIDC Provider Configuration 활성화
* Authorization Code Grant와 Refresh Token Grant 지원
* 공개 클라이언트를 위한 PKCE 검증 지원
* 기존 `/login` 화면과 DB 기반 사용자 인증 연동
* 인증 전 요청한 Authorization Request를 보존하고 로그인 성공 후 원래 흐름으로 복귀
* 직접 로그인 성공과 OAuth2 인증 요청의 로그인 성공 경로 분리
* `oauth2_registered_client` 기반 JDBC `RegisteredClientRepository` 구성
* `oauth2_authorization` 기반 JDBC Authorization 저장소 구성
* Authorization Consent 저장 구조와 JDBC 저장소 추가
* Authorization Endpoint, Token Endpoint, JWK Set Endpoint, OIDC Discovery Endpoint, UserInfo Endpoint 제공
* RSA 서명키를 이용한 JWT Access Token 및 ID Token 발급
* 운영 서명키를 저장소에 커밋하지 않고 외부 설정으로 주입하는 구조 구성
* ID Token과 UserInfo에 표준 `sub`, `iss`, `aud`, `iat`, `exp` 및 허용된 `email`, `name` 클레임 제공
* 사용자 `public_id`를 변경되지 않는 OIDC `sub` 값으로 사용
* 요청된 scope와 사용자 동의 결과에 따른 클레임 노출 제한
* `client_id`, `redirect_uri`, `response_type`, `scope`, Authorization Code, `grant_type`, Client Secret 검증 및 `state` 값의 안전한 왕복 보장
* Authorization Code의 짧은 만료 시간과 일회성 사용 보장
* Refresh Token Rotation과 기존 Refresh Token 재사용 방지
* 로그아웃 및 토큰 폐기 범위 검토 후 표준 엔드포인트 적용
* OAuth2/OIDC 오류 응답에서 내부 예외 및 비밀정보 노출 방지
* 인증 코드, Access Token, Refresh Token, ID Token, Client Secret을 로그에 남기지 않도록 점검
* 테스트용 Registered Client와 OAuth2 Client 역할의 통합 테스트 구성
* Neon PostgreSQL 스키마와 OAuth2 JDBC 저장 구조 일치 여부 반영
* Authorization Server 사용 방법과 클라이언트 연동 흐름을 `/README.md`에 문서화


## 제외 범위

* 클라이언트 서비스의 자체 세션 또는 자체 토큰 구현
* 클라이언트 서비스의 사용자 및 프로필 저장 구현
* 소셜 로그인 또는 외부 Identity Provider 연동
* Client 관리용 관리자 화면과 동적 Client 등록
* Device Authorization Grant, Client Credentials Grant, Resource Owner Password Grant
* 사용자별 세션 관리 화면과 전체 기기 강제 로그아웃 기능
* 운영용 인증서 발급, KMS/HSM 연동 및 운영 인프라 배포
* 실제 클라이언트 서비스의 운영 배포


## 참고 문서

* `/AGENTS.md`
* `/README.md`
* `/doc/architecture/DATABASE.md`
* `/doc/task/DASHBOARD.md`
* Spring Authorization Server 공식 문서
* OAuth 2.0 Authorization Framework
* OAuth 2.0 Authorization Server Metadata
* OAuth 2.0 PKCE
* OpenID Connect Core 및 Discovery


## 구현 단계

1. 현재 Spring Boot 및 Spring Security 버전과 호환되는 Authorization Server 의존성을 적용한다.
2. Authorization Server용 보안 필터 체인과 애플리케이션 로그인용 필터 체인의 우선순위를 분리한다.
3. JDBC 기반 Registered Client, Authorization, Authorization Consent 저장소를 구성하고 DB 문서를 갱신한다.
4. RSA 서명키와 issuer 설정을 외부에서 안전하게 주입하고 JWK Set을 제공한다.
5. Authorization Code, PKCE, Token, Refresh Token 흐름을 구현한다.
6. OIDC Discovery, ID Token, UserInfo와 사용자 클레임 매핑을 구현한다.
7. 로그인 성공 복귀, 동의, 오류 처리와 로그 보안 정책을 적용한다.
8. 정상 흐름과 위변조·재사용·만료 시나리오를 자동 테스트한다.
9. Neon 환경에서 실제 Client를 등록하고 전체 인증 흐름을 확인한다.
10. 클라이언트 연동 방법과 보안 설정을 문서화한다.


## 외부 작업 의존성

* 별도 프로젝트인 `okerry_console`을 OAuth2/OIDC Client로 구성한 뒤 실제 연동 검증을 진행한다.
* `okerry_console`에서는 우선 Authorization Code Flow, Callback, Token 교환 및 UserInfo 조회만 구현한다.
* 사용자, 서비스 및 OAuth2 Client 관리 기능은 실제 로그인 연동 검증 이후 별도 작업으로 구현한다.
* `okerry_console`은 Platform DB에 직접 접근하지 않고, 이후 `okerry platform`에서 제공할 관리자 API를 사용한다.
* `okerry_console` 연동 전까지 TASK 008의 실제 Client 전체 흐름 완료 조건은 보류한다.
* Console을 포함한 각 서비스는 독립된 Client ID, Secret, Redirect URI를 사용하며, Platform은 특정 서비스 전용 분기 없이 등록 정보로 Client를 식별한다.


## 완료 조건

* [x] 등록된 Client가 Authorization Code Flow를 시작할 수 있다.
* [x] 미인증 사용자는 기존 로그인 화면으로 이동하고, 로그인 후 원래 Authorization Request로 복귀한다.
* [x] 등록된 Redirect URI와 정확히 일치하는 URI로만 Authorization Code가 전달된다.
* [x] 공개 Client의 Authorization Code 교환 시 PKCE가 검증된다.
* [x] Authorization Code는 짧은 만료 시간을 가지며 한 번만 사용할 수 있다.
* [x] 유효한 Authorization Code로 Access Token, ID Token 및 조건에 따라 Refresh Token이 발급된다.
* [x] Refresh Token 갱신 시 새 Refresh Token이 발급되고 이전 토큰을 재사용할 수 없다.
* [x] OIDC Discovery와 JWK Set 문서를 조회할 수 있다.
* [x] ID Token의 서명을 공개 JWK로 검증할 수 있고 `iss`, `sub`, `aud`, `iat`, `exp`가 올바르다.
* [x] OIDC `sub`는 사용자의 변경 불가능한 `public_id`를 사용한다.
* [x] UserInfo는 유효한 Access Token과 허용된 scope에 대해서만 사용자 정보를 반환한다.
* [ ] 잘못된 `client_id`, `redirect_uri`, `response_type`, `scope`, `code`, `grant_type`, Client Secret 요청이 거부되고 `state`가 변경 없이 반환된다.
* [ ] OAuth2 Client, Authorization, Consent 데이터가 Neon PostgreSQL에 정상 저장된다.
* [ ] 운영용 Private Key와 Client Secret이 소스 및 Git 추적 파일에 포함되지 않는다.
* [ ] 비밀번호, 토큰, Authorization Code, Client Secret, Private Key가 로그에 출력되지 않는다.
* [ ] OAuth2/OIDC 오류 응답에 내부 예외 상세가 노출되지 않는다.
* [ ] 정상 인증, PKCE 실패, Redirect URI 불일치, Code 재사용, Code 만료, Refresh Token 재사용 테스트가 통과한다.
* [x] 전체 기존 자동 테스트와 새 Authorization Server 테스트가 통과한다.
* [ ] 복수의 등록 Client가 각자의 Redirect URI로 요청할 수 있고 다른 Client의 Redirect URI를 사용할 수 없다.
* [ ] 실제 Neon 환경과 테스트 Client 사이의 로그인부터 UserInfo 조회까지 전체 흐름이 성공한다.
* [ ] `/README.md`에 issuer, 표준 엔드포인트, Client 등록 및 연동 방법이 문서화된다.
* [x] `/doc/task/GUIDE.md`에 모든 프로젝트가 참고할 공통 Client 연동 기준과 Next.js/Auth.js 구현 예시가 문서화된다.


## 진행 로그

### 2026-07-03

* OAuth2/OIDC Authorization Server 구현 범위, 제외 범위, 구현 단계 및 완료 조건을 정의함.
* Spring Security 7.1 Authorization Server starter와 보안 필터 체인을 적용함.
* JDBC Registered Client, Authorization, Consent 저장소를 구성함.
* 외부 RSA 키 주입, OIDC Discovery, JWK Set, ID Token 및 scope 기반 사용자 클레임을 구성함.
* 저장된 Authorization Request 복귀와 직접 로그인 기본 이동을 분리함.
* 공식 7.1 JDBC 스키마에 맞춰 OAuth2 저장 컬럼과 Consent 테이블을 정비함.
* Authorization Code, PKCE, OIDC 클레임 및 Code 재사용 방지 통합 테스트를 추가함.
* Windows JDK 21 환경에서 Authorization Server 통합 테스트 3개와 전체 자동 테스트 24개 통과를 확인함.

### 2026-07-03

* 별도 프로젝트 `okerry_console`을 실제 OAuth2/OIDC Client로 사용하기로 결정함.
* `okerry_console`에서 로그인, Callback, Token 및 UserInfo 연동을 구현한 뒤 TASK 008의 실제 Client 검증을 재개하기로 함.
* 외부 Client 연동이 남아 있으므로 TASK 008과 DASHBOARD 상태는 `진행중`으로 유지함.

### 2026-07-03

* 향후 여러 서비스가 연결되는 것을 전제로 Client별 등록 정보와 Redirect URI를 독립적으로 검증하는 기준을 명시함.
* 두 개의 등록 Client가 각자의 Redirect URI로 처리되고 Client 간 Redirect URI 교차 사용은 거부되는 통합 테스트를 추가함.
* 모든 Client 프로젝트가 공통으로 적용할 Authorization Code, PKCE, Callback, Token, 사용자 매핑 및 보안 기준을 `/doc/task/GUIDE.md`에 작성함.
* Next.js/Auth.js 설정은 공통 기준을 적용하는 구현 예시로 분리함.
* Console의 Callback URI 예시를 Next.js/Auth.js 기본 경로인 `/api/auth/callback/okerry`로 정정함.
* 도메인 도입 전 연동을 위해 GUIDE의 issuer, Authorization, Callback, Token 및 Discovery 예시를 `localhost:8080` Platform과 `localhost:3000` Client 기준으로 정리함.
* 현재 WSL 실행 환경에 Java 21이 없어 새 통합 테스트 실행은 보류함. Java 21 환경에서 통과 확인 후 복수 Client 완료 조건을 갱신해야 함.
