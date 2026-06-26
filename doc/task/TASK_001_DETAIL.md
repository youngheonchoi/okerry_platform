# TASK 001. 로그인 화면 구현


## 목적

okerry platform의 로그인 화면을 구현한다.



## 참고 문서

* /AGENTS.md
* /doc/design/DESIGN.md
* /doc/task/DASHBOARD.md
* /src/main/resources/static/css/platform_style.css



## 작업 체크리스트

* [x] 로그인 화면 파일 생성
* [x] 공통 CSS 연결
* [x] Thymeleaf fragment 구조 적용
* [x] 샘플 화면에서 레이아웃 확인
* [x] 테스트 실행



## 완료 조건

* [x] 로그인 화면이 렌더링된다.
* [x] 공통 CSS가 연결된다.
* [x] 스타일 값은 하드코딩보다 공통 CSS 변수를 우선 사용한다.
* [x] 브라우저에서 샘플 페이지로 레이아웃을 확인할 수 있다.
* [x] `./gradlew test`가 통과한다.


## 진행 로그

### 2026-06-26(예시1)

* 작업 상세 문서 초안 작성.
* 아직 구현 작업은 시작하지 않음.

### 2026-06-26

* `/login` 요청을 처리하는 `LoginController`를 추가함.
* 커스텀 로그인 화면을 사용하도록 Spring Security 설정을 추가함.
* `auth/login.html` 로그인 템플릿과 `fragments/layout.html` 공통 레이아웃 fragment를 추가함.
* `platform_style.css`에 디자인 시스템 기반 CSS 변수와 로그인 화면 스타일을 추가함.
* 로그인 화면 렌더링과 인증 전 리다이렉트 흐름을 확인하는 MockMvc 테스트를 추가함.
* 테스트 환경에서 DB 자동설정에 묶이지 않도록 `src/test/resources/application.properties`에 DataSource/JPA 자동설정 제외를 추가함.
* `./gradlew test`를 실행했으나 현재 환경에 `JAVA_HOME`과 `java`가 없어 실행하지 못함.

### 2026-06-26

* `/tmp`에 JDK 21을 내려받아 `JAVA_HOME=/tmp/jdk21`로 테스트를 실행함.
* Spring Boot 4.1 기준 `AutoConfigureMockMvc` 패키지 변경을 반영해 테스트 import를 수정함.
* `JAVA_HOME=/tmp/jdk21 GRADLE_USER_HOME=/tmp/gradle-home ./gradlew test --no-daemon` 통과를 확인함.
* `http://localhost:18080/login`에서 로그인 화면 HTML 렌더링과 `/css/platform_style.css` 연결을 HTTP 응답으로 확인함.
* DB 설정 전에도 `OkerryPlatformApplication` 실행으로 화면을 볼 수 있도록 기본 application 설정에 DataSource/JPA 자동설정 제외를 추가함.
