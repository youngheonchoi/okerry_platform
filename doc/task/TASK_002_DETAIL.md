# TASK 002. 로그인, 회원가입 화면 구현


## 목적

Okerry UI를 사용하여 okerry platform의 로그인 화면과 SSO 회원가입 화면을 구현한다.


## 구현 범위

* 기존 로그인 화면을 Okerry UI 기준에 맞게 수정한다.
* SSO 회원가입 화면을 추가한다.
* 로그인 화면과 회원가입 화면 사이의 이동 경로를 제공한다.
* Okerry UI CDN을 먼저 불러오고, 프로젝트 전용 스타일이 필요한 경우 그다음에 `platform_style.css`를 불러온다.
* 버튼, 입력 필드, 카드, 레이아웃, 텍스트 등 공통 UI에는 Okerry UI 클래스와 CSS 변수를 우선 사용한다.
* Okerry UI에 없는 프로젝트 전용 스타일만 `platform_style.css`에 추가한다.
* 화면 렌더링과 화면 간 이동을 검증하는 테스트를 작성한다.


## 제외 범위

* 실제 로그인 인증 처리 변경
* 회원가입 정보 저장 및 계정 생성 로직
* 이메일 인증 및 비밀번호 찾기 기능
* OAuth2/OIDC Authorization Flow 변경
* 서비스별 추가 프로필 정보 수집


## 참고 문서

* /AGENTS.md
* /doc/design/DESIGN.md
* /doc/task/DASHBOARD.md
* /src/main/resources/static/css/platform_style.css


## 작업 체크리스트

* [x] 로그인 화면을 Okerry UI 기준으로 수정
* [x] 회원가입 화면 파일 및 렌더링 경로 추가
* [x] 로그인 화면과 회원가입 화면 간 이동 경로 추가
* [x] Okerry UI CDN과 `platform_style.css` 연결 순서 적용
* [x] 프로젝트 전용 스타일 작성 기준 준수
* [x] 화면 렌더링 및 이동 테스트 추가
* [x] 테스트 실행


## 완료 조건

* [x] 로그인 화면과 회원가입 화면이 정상적으로 렌더링된다.
* [x] 두 화면 사이를 이동할 수 있다.
* [x] Okerry UI CDN이 `platform_style.css`보다 먼저 로드된다.
* [x] 공통 UI는 Okerry UI 클래스와 CSS 변수를 우선 사용한다.
* [x] `platform_style.css`에는 Okerry UI에 없는 프로젝트 전용 스타일만 추가된다.
* [x] 화면 렌더링 및 이동 관련 테스트가 통과한다.
* [x] `./gradlew test`의 테스트 태스크가 통과한다.


## 진행 로그

### 2026-07-01

* 작업 상세 문서 초안 작성.
* 아직 구현 작업은 시작하지 않음.

### 2026-07-01

* 기존 로그인 화면을 Okerry UI 클래스 기반으로 수정함.
* 공통 레이아웃에서 Okerry UI CDN을 `platform_style.css`보다 먼저 로드하도록 변경함.
* SSO 회원가입 화면과 `/signup` 렌더링 경로를 추가함.
* 로그인 화면과 회원가입 화면 사이의 이동 링크를 추가함.
* `platform_style.css`에서 공통 컴포넌트 재정의를 제거하고 인증 화면 전용 배치 스타일만 유지함.
* 로그인 및 회원가입 화면 렌더링, 화면 간 이동 링크, CDN 로드 순서를 검증하는 테스트를 추가함.
* `./gradlew test --no-daemon` 실행에서 모든 테스트 태스크가 통과함.
* 현재 실행 환경의 Gradle 캐시 잠금 파일 I/O 오류로 테스트 성공 후 Gradle 서비스 종료 단계가 실패 코드로 끝나는 현상을 확인함.
