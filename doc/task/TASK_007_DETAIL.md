# TASK 007. Neon PostgreSQL 전환


## 목적

인증 서버의 DB 문서, 애플리케이션 연결, JPA 기반 회원가입과 로그인 기능을 PostgreSQL 기반 Neon으로 전환한다.


## 구현 범위

* `/doc/architecture/DATABASE.md`를 Neon PostgreSQL 문법으로 구성
* 자동 증가 PK를 PostgreSQL identity column으로 변경
* 날짜, 바이너리, Boolean 타입을 PostgreSQL 타입으로 변경
* 고유 제약 조건, 외래 키, CHECK 제약 조건 및 인덱스를 PostgreSQL 문법으로 구성
* 기본 권한과 OAuth2 Client 예시 INSERT를 재실행 가능한 형태로 변경
* Neon SQL Editor 실행 순서와 보안 주의사항 문서화
* PostgreSQL JDBC 드라이버 적용 및 기존 DB 드라이버 제거
* pooled Neon endpoint와 SSL을 사용하는 `neon` 프로필 구성
* DB 비밀번호는 `NEON_DATABASE_PASSWORD` 환경변수로만 주입
* 회원가입, 로그인, 권한 조회 및 마지막 로그인 갱신 기능의 PostgreSQL 호환성 반영
* 테스트 DB를 PostgreSQL 호환 모드로 변경하고 전체 테스트 수행
* Gmail SMTP를 이용한 회원가입 이메일 인증번호 발급 및 확인
* SMTP App Password는 `SMTP_GMAIL_APP_PASSWORD` 환경변수로만 주입
* 인증번호는 원문 대신 BCrypt 해시로 저장하고 5분 만료, 5회 시도, 60초 재전송 제한 적용
* 이메일 인증을 완료한 동일 세션만 회원가입 허용


## 제외 범위

* 실제 Neon 프로젝트, branch, database 생성
* 기존 DB 데이터 이전
* 실제 Neon DB에서 DDL 실행
* OAuth2/OIDC Authorization Server 구현


## 참고 문서

* /AGENTS.md
* /doc/architecture/DATABASE.md
* /doc/task/DASHBOARD.md
* Neon PostgreSQL compatibility documentation
* PostgreSQL documentation


## 작업 체크리스트

* [x] 기존 DB 전용 DDL 식별
* [x] PostgreSQL 타입 및 identity 문법 변환
* [x] PostgreSQL 제약 조건 및 인덱스 문법 변환
* [x] 초기 데이터 INSERT 문법 변환
* [x] Neon 실행 및 보안 기준 추가
* [x] PostgreSQL JDBC 드라이버 적용
* [x] `neon` 프로필과 SSL 연결 설정 구성
* [x] 회원가입 및 로그인 기능의 Neon 프로필 전환
* [x] 테스트 DB의 PostgreSQL 호환 모드 적용
* [x] Gmail SMTP 및 환경변수 설정 구성
* [x] 이메일 인증번호 저장 모델 및 Repository 구현
* [x] 인증번호 발급, 만료, 시도 및 재전송 제한 구현
* [x] 회원가입 이메일 인증 필수 조건 적용
* [x] 이메일 인증 자동 테스트 통과
* [x] 전체 자동 테스트 통과
* [x] Neon SQL Editor에서 전체 DDL 실행 확인
* [x] 실제 Neon 연결 및 애플리케이션 기동 확인


## 완료 조건

* [x] `DATABASE.md`에 이전 DB 전용 테이블 문법이 남아 있지 않는다.
* [x] PostgreSQL에서 지원하는 타입과 제약 조건으로 작성된다.
* [x] 초기 권한 데이터는 중복 없이 실행할 수 있다.
* [x] 토큰 및 직렬화 데이터는 `BYTEA`로 정의된다.
* [x] DB 연결 비밀정보가 문서에 포함되지 않는다.
* [x] 애플리케이션에서 PostgreSQL JDBC 드라이버와 `neon` 프로필을 사용한다.
* [x] DB 비밀번호가 코드와 설정 파일에 포함되지 않는다.
* [x] Gmail App Password가 코드와 설정 파일에 포함되지 않는다.
* [x] 실제 Gmail SMTP로 인증번호를 수신하고 확인할 수 있다.
* [x] 인증하지 않은 이메일로 회원가입할 수 없다.
* [x] 회원가입과 로그인이 PostgreSQL 호환 테스트에서 통과한다.
* [x] Neon SQL Editor에서 전체 DDL이 오류 없이 실행된다.
* [x] 실제 Neon DB에 연결한 애플리케이션이 정상 기동한다.


## 진행 로그

### 2026-07-02

* 기존 `DATABASE.md`를 Neon PostgreSQL 기준 DDL로 전환함.
* 실제 Neon DB 실행 검증은 사용자가 대상 DB를 선택한 뒤 수행해야 하므로 남겨둠.

### 2026-07-02

* 추가 요구사항에 따라 작업 범위를 애플리케이션 DB 연결과 회원가입 및 로그인 기능까지 확장함.
* PostgreSQL JDBC 드라이버, `neon` 프로필, pooled endpoint, SSL 및 환경변수 기반 비밀번호 설정을 반영함.
* 회원가입, 로그인 및 마지막 로그인 갱신 Bean의 활성 프로필을 `neon`으로 변경함.
* H2 통합 테스트를 PostgreSQL 호환 모드로 변경함.
* 현재 에이전트 실행 환경에서는 `NEON_DATABASE_PASSWORD`를 감지할 수 없어 실제 Neon 접속 검증을 보류함.
* PostgreSQL JDBC 드라이버 다운로드와 전체 테스트 실행은 외부 실행 승인이 완료되지 않아 보류함.

### 2026-07-02

* Gmail SMTP 이메일 인증 요구사항을 작업 범위에 추가함.
* 인증번호 BCrypt 해시 저장, 5분 만료, 최대 5회 확인, 60초 재전송 제한을 구현함.
* 인증 완료 이메일을 HTTP 세션에 연결하고 동일 이메일 인증이 없으면 회원가입을 차단하도록 변경함.
* SMTP 비밀번호는 `SMTP_GMAIL_APP_PASSWORD` 환경변수로만 주입하도록 구성함.
* Windows JDK 21 환경에서 전체 21개 테스트 통과를 확인함.
