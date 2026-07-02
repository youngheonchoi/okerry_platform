# TASK 005. SSO 사용자 저장 기반 구현


## 목적

SSO 회원가입과 로그인 기능을 구현하기 전에 사용자, 인증 정보, 권한을 안전하게 저장하고 조회할 수 있는 DB 영속성 기반을 구축한다.


## 구현 범위

* MySQL `chldudgjs` 데이터베이스의 기존 `auth` 관련 테이블과 데이터를 먼저 확인한다.
* 기존 테이블을 임의로 삭제하거나 덮어쓰지 않고 `/doc/architecture/DATABASE.md`와의 차이를 정리한다.
* `auth_users`, `auth_user_credentials`, `auth_roles`, `auth_user_roles` 스키마는 사용자가 직접 관리한다.
* 애플리케이션은 `/doc/architecture/DATABASE.md`의 스키마를 기준으로 매핑하고 실행 시 스키마 일치 여부만 검증한다.
* DB 스키마와 일치하는 사용자, 인증 정보, 권한 도메인 모델 및 JPA Entity를 구현한다.
* 사용자와 권한 조회에 필요한 Repository를 구현한다.
* 비밀번호 원문이 저장되지 않도록 비밀번호 해시 저장 구조를 적용한다.
* 주요 제약 조건과 Repository 동작을 검증하는 테스트를 작성한다.


## DB 접속 참고

* MySQL 데이터베이스명: `chldudgjs`
* 사용자명: `root`
* 비밀번호는 사용자가 실행 시 직접 입력하거나 환경변수 등 저장소에 남지 않는 방식으로 주입한다.
* 비밀번호, 접속 비밀정보, 실제 사용자 데이터는 코드, 설정 파일, 문서, 로그에 기록하지 않는다.
* 기존 `auth` 관련 테이블이 여러 개 있으므로 스키마 확인 전에는 생성, 변경, 삭제 작업을 수행하지 않는다.


## 제외 범위

* 실제 회원가입 요청 처리 및 계정 생성 흐름
* 이메일 인증번호 발급 및 검증
* Spring Security 로그인 인증의 DB 연동
* OAuth2/OIDC Authorization Server 구성
* 서비스별 사용자 프로필 및 비즈니스 데이터 저장
* 기존 운영 데이터의 임의 변경 또는 삭제


## 참고 문서

* /AGENTS.md
* /doc/architecture/DATABASE.md
* /doc/task/DASHBOARD.md
* /doc/task/TASK_005_SCHEMA_INSPECTION.sql


## 작업 체크리스트

* [ ] `chldudgjs`의 기존 `auth` 관련 테이블 및 제약 조건 확인
* [ ] 기존 DB 구조와 `/doc/architecture/DATABASE.md` 차이 정리
* [x] 안전한 MySQL 접속 설정 구성
* [x] 사용자 관리 스키마와 일치하는 JPA 매핑 구성
* [x] 사용자, 인증 정보, 권한 Entity 구현
* [x] Repository 구현
* [x] 비밀번호 해시 저장 구조 적용
* [x] Repository 및 DB 제약 조건 테스트 작성
* [x] 전체 테스트 실행


## 완료 조건

* [x] DB 비밀번호와 비밀정보가 저장소 및 로그에 남지 않는다.
* [x] 애플리케이션이 기존 테이블을 생성하거나 변경하지 않도록 구성된다.
* [ ] 사용자, 인증 정보, 권한 스키마가 `/doc/architecture/DATABASE.md`와 일치한다.
* [x] 사용자와 권한을 Repository로 저장하고 조회할 수 있다.
* [x] 비밀번호 원문이 DB에 저장되지 않는다.
* [x] 주요 유일성, 외래 키 및 조회 동작에 대한 테스트가 통과한다.
* [x] `./gradlew test`가 통과한다.


## 진행 로그

### 2026-07-02

* 작업 상세 문서 초안 작성.
* 아직 구현 작업은 시작하지 않음.

### 2026-07-02

* 실제 DB 접속을 `db` 프로필과 환경변수 기반으로 분리함.
* 사용자, 인증 정보, 권한 Entity와 Repository를 구현함.
* BCrypt 강도 12를 사용하는 비밀번호 해시 서비스를 추가함.
* H2 기반 Repository, 유일성 제약 조건, 비밀번호 해시 테스트를 추가함.
* Windows JDK 21 환경에서 `gradlew.bat test --no-daemon` 실행 통과를 확인함.
* 현재 실행 환경에 MySQL CLI와 DB 비밀번호가 없어 실제 `chldudgjs` 스키마 비교는 수행하지 못함.
* 기존 DB를 변경하지 않고 확인할 수 있도록 `TASK_005_SCHEMA_INSPECTION.sql`을 추가함.

### 2026-07-02

* DB 스키마는 사용자가 직접 관리한다는 방침에 따라 Flyway 의존성, 설정, 마이그레이션 파일을 제거함.
* 이후 DB 관련 구현은 `/doc/architecture/DATABASE.md`를 기준으로 JPA 매핑과 검증만 수행하도록 작업 범위를 변경함.
