# AGENTS.md

## 문서의 목적

이 문서는 `okerry platform` 프로젝트를 구현할 때 지속적으로 참고해야 하는 개발 기준을 정의합니다.
`AGENTS.md`에는 프로젝트 전반에서 변하지 않아야 하는 원칙, 책임 경계, 보안 기준, 도메인 경계, 코드 작성 기준만 작성합니다.



## 프로젝트 성격

`okerry platform`은 여러 사이드 프로젝트에서 공통으로 사용할 중앙 인증 서버입니다.
이 프로젝트는 단일 서비스 내부의 로그인 기능이 아니라, **OAuth2/OIDC 기반 Authorization Server이자 Identity Provider**로 다룹니다.
각 클라이언트 서비스는 자체 로그인 화면을 제공하지 않고, 사용자를 `okerry platform`으로 리다이렉트합니다.
`okerry platform`은 사용자를 인증하고, 인증 결과를 표준 OAuth2/OIDC 흐름에 따라 클라이언트 서비스로 전달합니다.



## 문서 역할 구분

### /README.md

프로젝트 소개, 기술 스택, 서비스 흐름, 인증 구조 개념을 설명합니다.

### /AGENTS.md

AI 에이전트와 개발자가 지속적으로 지켜야 할 개발 기준을 설명합니다.
변경 가능성이 큰 태스크 요구사항이나 MVP 범위는 작성하지 않습니다.

### /doc/task/DASHBOARD.md

현재 단계에서 진행할 전체 작업 목록을 관리합니다.
각 작업의 번호, 제목, 상태, 우선순위, 의존 관계, 상세 문서 링크를 작성합니다.
개별 작업의 상세 요구사항, 구현 범위, 완료 조건은 `TASK_xx_DETAIL.md`에서 관리합니다.

### /doc/task/TASK_xx_DETAIL.md

개별 작업의 상세 내용을 관리합니다.
작업 목적, 구현 범위, 제외 범위, 참고 문서, 구현 단계, 완료 조건을 작성합니다.
작업 중 변경되는 세부 내용은 `AGENTS.md`가 아니라 해당 상세 문서에 반영합니다.

### /doc/architecture/DATABASE.md

DB 테이블 구조, 컬럼 의미, 인덱스, 제약 조건, 초기 데이터, OAuth2/OIDC 저장소 구조를 설명합니다.
사용자, 권한, 서비스, OAuth2 Client, Authorization, Token 저장 구조를 구현하거나 수정할 때는 반드시 `/doc/architecture/DATABASE.md`를 먼저 확인합니다.
DB 스키마와 도메인 모델, JPA Entity, Repository, Migration Script가 서로 어긋나지 않도록 유지합니다.

### /doc/design/DESIGN.md

프로젝트 UI의 색상, 타이포그래피, 간격, radius, 컴포넌트 스타일, 라이트/다크 모드 기준을 설명합니다.
로그인, 회원가입, 서비스 선택 화면 등 사용자에게 노출되는 UI를 구현하거나 수정할 때는 반드시 `/doc/design/DESIGN.md`를 먼저 확인합니다.

### /src/main/resources/static/css/platform_style.css

디자인 시스템에서 사용하는 CSS 변수의 실제 정의 파일입니다.
색상, 간격, radius, shadow, font 등 스타일 값을 사용할 때는 하드코딩하지 않고 `platform_style.css`에 정의된 CSS 변수를 우선 사용합니다.
새로운 스타일 값이 필요하면 코드에 직접 값을 넣기 전에 `DESIGN.md`와 `platform_style.css`에 추가해야 하는 공통 토큰인지 먼저 판단합니다.



## 책임 경계

### okerry platform이 담당하는 것

* SSO 사용자 회원가입
* SSO 사용자 로그인
* 사용자 인증
* OAuth2 Authorization Flow
* OIDC 사용자 식별 정보 제공
* Access Token 발급
* Refresh Token 발급, 갱신, 폐기
* ID Token 발급
* OAuth2 Client 관리
* 등록된 서비스 목록 관리
* SSO 직접 접근 시 서비스 선택 화면 제공

### 클라이언트 서비스가 담당하는 것

* 사용자를 `okerry platform`으로 리다이렉트
* 인증 완료 후 Callback 처리
* SSO 사용자와 서비스 사용자 매핑
* 서비스 자체 세션 또는 자체 토큰 생성
* 서비스별 추가 프로필 정보 수집
* 서비스별 권한 및 접근 제어 처리

기능의 위치가 애매한 경우, `okerry platform`은 인증과 사용자 식별까지만 담당하도록 유지합니다.



## 회원가입 경계

`okerry platform`의 회원가입과 클라이언트 서비스의 회원가입은 다릅니다.

### SSO 회원가입

SSO 회원가입은 여러 서비스에서 공통으로 사용할 인증 계정을 생성하는 과정입니다.
SSO 사용자 정보에는 인증과 사용자 식별에 필요한 정보만 포함합니다.

예시:

* 이메일
* 비밀번호
* 이름
* 권한
* 계정 상태

### 서비스 회원가입

서비스 회원가입은 각 서비스 이용에 필요한 추가 정보를 등록하는 과정입니다.

예시:

* 닉네임
* 프로필 이미지
* 서비스별 약관 동의
* 서비스별 설정 정보
* 서비스 도메인에 필요한 추가 정보

서비스별 정보는 `okerry platform`의 핵심 사용자 모델에 넣지 않습니다.



## 도메인 경계 기준

`okerry platform`은 인증, 인가, 사용자 식별, 토큰 관리에 필요한 정보만 관리합니다.
서비스별 비즈니스 데이터는 `okerry platform`이 관리하지 않습니다.
도메인 모델을 추가하거나 수정할 때는 다음을 확인합니다.

* 이 정보가 인증 또는 사용자 식별에 필요한가?
* 여러 서비스에서 공통으로 사용되는 정보인가?
* 특정 서비스의 화면, 정책, 기능에만 필요한 정보는 아닌가?
* 보안상 SSO 서버가 직접 관리해야 하는 정보인가?

위 질문에 명확히 답하기 어렵다면 SSO 핵심 모델에 추가하지 않습니다.



## 직접 접근 처리 기준

사용자가 OAuth2/OIDC 인증 요청 없이 `okerry platform`에 직접 접근한 경우, 로그인 화면을 바로 보여주지 않습니다.
대신 서비스 선택 화면을 제공합니다.
이유는 `okerry platform`이 어떤 클라이언트 서비스로 인증 결과를 전달해야 하는지 알 수 없기 때문입니다.
직접 접근 흐름은 다음 기준을 따릅니다.

```text id="pu1wni"
사용자
  -> okerry platform 직접 접근
  -> 서비스 선택 화면
  -> 선택한 클라이언트 서비스로 이동
  -> 클라이언트 서비스에서 정상 로그인 요청 시작
  -> okerry platform 로그인 화면
```



## OAuth2/OIDC 기준

표준 OAuth2/OIDC 흐름을 우선합니다.
표준 방식이 존재하는 경우 임의의 자체 인증 흐름을 만들지 않습니다.
다음 값은 반드시 검증 대상으로 취급합니다.

* client_id
* redirect_uri
* response_type
* scope
* state
* code
* grant_type
* client_secret

`redirect_uri`는 요청값을 그대로 신뢰하지 않고, 등록된 Redirect URI 목록과 비교해야 합니다.
Authorization Code는 짧은 만료 시간을 가지며 한 번만 사용할 수 있어야 합니다.
ID Token에는 issuer, subject, audience, issued at, expiration 정보가 올바르게 포함되어야 합니다.
Spring Security가 모든 프로젝트별 검증을 자동으로 처리한다고 가정하지 않습니다.
프로젝트에서 명시적으로 보장해야 하는 검증은 코드상에서 드러나야 합니다.



## 보안 기준

보안 관련 코드는 편의보다 안전성을 우선합니다.

* 비밀번호는 평문으로 저장하지 않습니다.
* Access Token은 짧은 만료 시간을 가집니다.
* Refresh Token은 서버에서 폐기할 수 있어야 합니다.
* 가능하면 Refresh Token 원문이 아니라 해시값을 저장합니다.
* Client Secret은 외부에 노출하지 않습니다.
* Private Key는 저장소에 커밋하지 않습니다.
* 내부 예외 상세 정보를 사용자에게 그대로 노출하지 않습니다.
* 인증 실패 메시지는 공격자가 정보를 추론하기 어렵게 작성합니다.
* 개발 편의를 이유로 보안 검증을 우회하지 않습니다.

로그에 남기면 안 되는 정보:

* 비밀번호
* Access Token
* Refresh Token
* ID Token
* Authorization Code
* Client Secret
* Private Key



## 코드 작성 기준

단순하고 명확한 코드를 우선합니다.
보안 로직은 읽기 쉽게 작성합니다.
축약어보다 의미가 분명한 이름을 사용합니다.
필요해지기 전까지 과한 추상화를 만들지 않습니다.
도메인 메서드에 숨겨진 부수효과를 만들지 않습니다.
현재 문제를 명확히 해결하지 않는 프레임워크, 라이브러리, 패턴을 추가하지 않습니다.
인증, 토큰, 보안 검증 로직은 이후 테스트를 추가하기 쉽도록 입력과 결과가 명확한 구조로 작성합니다.