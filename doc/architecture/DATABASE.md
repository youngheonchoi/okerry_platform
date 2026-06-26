**MySQL 기준 DDL**



```sql
CREATE DATABASE IF NOT EXISTS okerry_sso
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE okerry_sso;
```





---

# 1. `auth_users`

SSO 인증 사용자 계정 테이블입니다.

```sql
CREATE TABLE auth_users (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '내부 사용자 PK',
    public_id VARCHAR(36) NOT NULL COMMENT '외부 노출용 사용자 식별자 UUID, JWT sub로 사용 권장',

    email VARCHAR(255) NOT NULL COMMENT '사용자 이메일, 로그인 ID로 사용',
    name VARCHAR(100) NOT NULL COMMENT '사용자 이름',

    status VARCHAR(30) NOT NULL COMMENT '계정 상태: ACTIVE, PENDING, LOCKED, DISABLED, WITHDRAWN',
    email_verified BOOLEAN NOT NULL DEFAULT FALSE COMMENT '이메일 인증 여부',

    last_login_at DATETIME NULL COMMENT '마지막 로그인 일시',

    created_at DATETIME NOT NULL COMMENT '생성 일시',
    updated_at DATETIME NOT NULL COMMENT '수정 일시',
    deleted_at DATETIME NULL COMMENT '삭제 또는 탈퇴 일시, soft delete 용도',

    PRIMARY KEY (id),
    UNIQUE KEY uk_auth_users_public_id (public_id),
    UNIQUE KEY uk_auth_users_email (email),
    KEY idx_auth_users_status (status)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='SSO 인증 사용자 계정';
```





---

# 2. `auth_user_credentials`

사용자의 비밀번호 인증 정보 테이블입니다.

```sql
CREATE TABLE auth_user_credentials (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '사용자 인증 정보 PK',

    user_id BIGINT NOT NULL COMMENT 'auth_users.id',

    password_hash VARCHAR(255) NOT NULL COMMENT '암호화된 비밀번호 해시',
    password_algorithm VARCHAR(50) NOT NULL COMMENT '비밀번호 해시 알고리즘: BCRYPT, ARGON2 등',

    failed_login_count INT NOT NULL DEFAULT 0 COMMENT '연속 로그인 실패 횟수',
    locked_until DATETIME NULL COMMENT '계정 잠금 해제 예정 일시',

    password_updated_at DATETIME NOT NULL COMMENT '비밀번호 마지막 변경 일시',

    created_at DATETIME NOT NULL COMMENT '생성 일시',
    updated_at DATETIME NOT NULL COMMENT '수정 일시',

    PRIMARY KEY (id),
    UNIQUE KEY uk_auth_user_credentials_user_id (user_id),

    CONSTRAINT fk_auth_user_credentials_user
        FOREIGN KEY (user_id)
        REFERENCES auth_users (id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='SSO 사용자 비밀번호 인증 정보';
```





---

# 3. `auth_roles`

SSO 권한 테이블입니다.

```sql
CREATE TABLE auth_roles (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '권한 PK',

    code VARCHAR(50) NOT NULL COMMENT '권한 코드: AUTH_USER, CONSOLE_ADMIN 등',
    name VARCHAR(100) NOT NULL COMMENT '권한 이름',
    description VARCHAR(255) NULL COMMENT '권한 설명',

    created_at DATETIME NOT NULL COMMENT '생성 일시',
    updated_at DATETIME NOT NULL COMMENT '수정 일시',

    PRIMARY KEY (id),
    UNIQUE KEY uk_auth_roles_code (code)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='SSO 및 Console 접근 권한';
```

초기 데이터

```sql
INSERT INTO auth_roles (
    code,
    name,
    description,
    created_at,
    updated_at
) VALUES
(
    'AUTH_USER',
    '일반 사용자',
    'SSO 일반 인증 사용자',
    NOW(),
    NOW()
),
(
    'CONSOLE_ADMIN',
    '콘솔 관리자',
    'okerry Console 접근 관리자',
    NOW(),
    NOW()
);
```





---

# 4. `auth_user_roles`

사용자와 권한 매핑 테이블입니다.

```sql
CREATE TABLE auth_user_roles (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '사용자 권한 매핑 PK',

    user_id BIGINT NOT NULL COMMENT 'auth_users.id',
    role_id BIGINT NOT NULL COMMENT 'auth_roles.id',

    created_at DATETIME NOT NULL COMMENT '생성 일시',

    PRIMARY KEY (id),
    UNIQUE KEY uk_auth_user_roles_user_role (user_id, role_id),
    KEY idx_auth_user_roles_role_id (role_id),

    CONSTRAINT fk_auth_user_roles_user
        FOREIGN KEY (user_id)
        REFERENCES auth_users (id),

    CONSTRAINT fk_auth_user_roles_role
        FOREIGN KEY (role_id)
        REFERENCES auth_roles (id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='SSO 사용자 권한 매핑';
```





---

# 5. `registered_services`

SSO에 등록된 서비스 목록 테이블입니다.

`서비스 선택 화면`, `서비스 메타데이터`, `login_start_url` 관리용입니다.

```sql
CREATE TABLE registered_services (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '등록 서비스 PK',

    service_key VARCHAR(100) NOT NULL COMMENT '서비스 고유 키: service-a, okerry-console 등',
    name VARCHAR(100) NOT NULL COMMENT '서비스 표시 이름',
    description VARCHAR(255) NULL COMMENT '서비스 설명',

    home_url VARCHAR(500) NOT NULL COMMENT '서비스 홈 URL',
    login_start_url VARCHAR(500) NOT NULL COMMENT '서비스 로그인 시작 URL, SSO 직접 접근 후 서비스 선택 시 이동할 URL',
    logo_url VARCHAR(500) NULL COMMENT '서비스 로고 URL',

    oauth2_client_id VARCHAR(100) NOT NULL COMMENT 'oauth2_registered_client.client_id',

    display_order INT NOT NULL DEFAULT 0 COMMENT '서비스 선택 화면 표시 순서',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '서비스 활성화 여부',

    created_at DATETIME NOT NULL COMMENT '생성 일시',
    updated_at DATETIME NOT NULL COMMENT '수정 일시',

    PRIMARY KEY (id),
    UNIQUE KEY uk_registered_services_service_key (service_key),
    UNIQUE KEY uk_registered_services_oauth2_client_id (oauth2_client_id),
    KEY idx_registered_services_enabled_order (enabled, display_order)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='SSO에 연결된 서비스 메타데이터';
```





---

# 6. `service_users`

SSO 사용자와 특정 서비스의 가입/이용 상태를 연결하는 테이블입니다.

```sql
CREATE TABLE service_users (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '서비스 사용자 매핑 PK',

    user_id BIGINT NOT NULL COMMENT 'auth_users.id',
    service_id BIGINT NOT NULL COMMENT 'registered_services.id',

    status VARCHAR(30) NOT NULL COMMENT '서비스 회원 상태: ACTIVE, PENDING_PROFILE, SUSPENDED, WITHDRAWN',

    joined_at DATETIME NOT NULL COMMENT '서비스 최초 연결 또는 가입 일시',
    last_accessed_at DATETIME NULL COMMENT '해당 서비스 마지막 접근 일시',

    created_at DATETIME NOT NULL COMMENT '생성 일시',
    updated_at DATETIME NOT NULL COMMENT '수정 일시',
    deleted_at DATETIME NULL COMMENT '서비스 회원 탈퇴 또는 연결 해제 일시',

    PRIMARY KEY (id),
    UNIQUE KEY uk_service_users_user_service (user_id, service_id),
    KEY idx_service_users_service_status (service_id, status),
    KEY idx_service_users_user_id (user_id),

    CONSTRAINT fk_service_users_user
        FOREIGN KEY (user_id)
        REFERENCES auth_users (id),

    CONSTRAINT fk_service_users_service
        FOREIGN KEY (service_id)
        REFERENCES registered_services (id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='SSO 사용자와 서비스별 회원 상태 매핑';
```





---

# 7. `oauth2_registered_client`

OAuth2/OIDC Client 설정 테이블입니다.
Spring Authorization Server의 공식 `oauth2_registered_client` 스키마는 `id`, `client_id`, `client_secret`, `redirect_uris`, `scopes`, `client_settings`, `token_settings` 등을 포함합니다.

```sql
CREATE TABLE oauth2_registered_client (
    id VARCHAR(100) NOT NULL COMMENT 'RegisteredClient 내부 식별자',

    client_id VARCHAR(100) NOT NULL COMMENT 'OAuth2 client_id',
    client_id_issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'client_id 발급 일시',

    client_secret VARCHAR(200) DEFAULT NULL COMMENT 'OAuth2 client_secret, confidential client인 경우 사용',
    client_secret_expires_at TIMESTAMP NULL DEFAULT NULL COMMENT 'client_secret 만료 일시',

    client_name VARCHAR(200) NOT NULL COMMENT '클라이언트 표시 이름',

    client_authentication_methods VARCHAR(1000) NOT NULL COMMENT '클라이언트 인증 방식 목록: client_secret_basic, client_secret_post, none 등',
    authorization_grant_types VARCHAR(1000) NOT NULL COMMENT '허용 grant type 목록: authorization_code, refresh_token 등',

    redirect_uris VARCHAR(1000) DEFAULT NULL COMMENT '허용 redirect_uri 목록',
    post_logout_redirect_uris VARCHAR(1000) DEFAULT NULL COMMENT 'OIDC logout 이후 허용 redirect URI 목록',

    scopes VARCHAR(1000) NOT NULL COMMENT '허용 scope 목록: openid, profile, email 등',

    client_settings VARCHAR(2000) NOT NULL COMMENT '클라이언트 설정 JSON 문자열',
    token_settings VARCHAR(2000) NOT NULL COMMENT '토큰 설정 JSON 문자열',

    PRIMARY KEY (id),
    UNIQUE KEY uk_oauth2_registered_client_client_id (client_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='OAuth2/OIDC 등록 클라이언트 설정';
```

예시로 들어갈 데이터 형태는 이런 식입니다.

```sql
INSERT INTO oauth2_registered_client (
    id,
    client_id,
    client_id_issued_at,
    client_secret,
    client_secret_expires_at,
    client_name,
    client_authentication_methods,
    authorization_grant_types,
    redirect_uris,
    post_logout_redirect_uris,
    scopes,
    client_settings,
    token_settings
) VALUES (
    UUID(),
    'okerry-console-client',
    CURRENT_TIMESTAMP,
    '{bcrypt}$2a$10$REPLACE_WITH_ENCODED_SECRET',
    NULL,
    'okerry Console',
    'client_secret_basic',
    'authorization_code,refresh_token',
    'https://console.okerry.com/login/oauth2/code/okerry',
    'https://console.okerry.com/logout/callback',
    'openid,profile,email',
    '{"@class":"java.util.Collections$UnmodifiableMap","settings.client.require-proof-key":false,"settings.client.require-authorization-consent":false}',
    '{"@class":"java.util.Collections$UnmodifiableMap","settings.token.reuse-refresh-tokens":false,"settings.token.access-token-time-to-live":["java.time.Duration",3600.000000000],"settings.token.refresh-token-time-to-live":["java.time.Duration",2592000.000000000],"settings.token.id-token-signature-algorithm":"RS256","settings.token.access-token-format":{"@class":"org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat","value":"self-contained"}}'
);
```





---

# 8. `oauth2_authorization`

OAuth2/OIDC 인증 흐름에서 발급된 authorization code, access token, refresh token, id token 상태를 저장하는 테이블입니다.

Spring Authorization Server 문서에서 `OAuth2Authorization`은 grant flow 완료 후 생성되며 access token, optional refresh token, authorization code, OIDC id token 등을 연결해 보관하는 모델로 설명됩니다.


```sql
CREATE TABLE oauth2_authorization (
    id VARCHAR(100) NOT NULL COMMENT 'OAuth2Authorization 내부 식별자',

    registered_client_id VARCHAR(100) NOT NULL COMMENT 'oauth2_registered_client.id',
    principal_name VARCHAR(200) NOT NULL COMMENT '인증 주체 이름, 일반적으로 사용자 public_id 또는 username',
    authorization_grant_type VARCHAR(100) NOT NULL COMMENT '사용된 grant type: authorization_code, refresh_token 등',

    authorized_scopes VARCHAR(1000) DEFAULT NULL COMMENT '승인된 scope 목록',
    attributes BLOB DEFAULT NULL COMMENT 'OAuth2Authorization 부가 속성 직렬화 데이터',
    state VARCHAR(500) DEFAULT NULL COMMENT 'OAuth2 authorization request state 값',

    authorization_code_value BLOB DEFAULT NULL COMMENT 'Authorization Code 값',
    authorization_code_issued_at TIMESTAMP NULL DEFAULT NULL COMMENT 'Authorization Code 발급 일시',
    authorization_code_expires_at TIMESTAMP NULL DEFAULT NULL COMMENT 'Authorization Code 만료 일시',
    authorization_code_metadata BLOB DEFAULT NULL COMMENT 'Authorization Code 메타데이터 직렬화 데이터',

    access_token_value BLOB DEFAULT NULL COMMENT 'Access Token 값',
    access_token_issued_at TIMESTAMP NULL DEFAULT NULL COMMENT 'Access Token 발급 일시',
    access_token_expires_at TIMESTAMP NULL DEFAULT NULL COMMENT 'Access Token 만료 일시',
    access_token_metadata BLOB DEFAULT NULL COMMENT 'Access Token 메타데이터 직렬화 데이터',
    access_token_type VARCHAR(100) DEFAULT NULL COMMENT 'Access Token 타입: Bearer 등',
    access_token_scopes VARCHAR(1000) DEFAULT NULL COMMENT 'Access Token에 포함된 scope 목록',

    oidc_id_token_value BLOB DEFAULT NULL COMMENT 'OIDC ID Token 값',
    oidc_id_token_issued_at TIMESTAMP NULL DEFAULT NULL COMMENT 'OIDC ID Token 발급 일시',
    oidc_id_token_expires_at TIMESTAMP NULL DEFAULT NULL COMMENT 'OIDC ID Token 만료 일시',
    oidc_id_token_metadata BLOB DEFAULT NULL COMMENT 'OIDC ID Token 메타데이터 직렬화 데이터',
    oidc_id_token_claims BLOB DEFAULT NULL COMMENT 'OIDC ID Token claims 직렬화 데이터',

    refresh_token_value BLOB DEFAULT NULL COMMENT 'Refresh Token 값',
    refresh_token_issued_at TIMESTAMP NULL DEFAULT NULL COMMENT 'Refresh Token 발급 일시',
    refresh_token_expires_at TIMESTAMP NULL DEFAULT NULL COMMENT 'Refresh Token 만료 일시',
    refresh_token_metadata BLOB DEFAULT NULL COMMENT 'Refresh Token 메타데이터 직렬화 데이터',

    user_code_value BLOB DEFAULT NULL COMMENT 'Device Authorization Grant user_code 값, MVP에서는 보통 미사용',
    user_code_issued_at TIMESTAMP NULL DEFAULT NULL COMMENT 'user_code 발급 일시',
    user_code_expires_at TIMESTAMP NULL DEFAULT NULL COMMENT 'user_code 만료 일시',
    user_code_metadata BLOB DEFAULT NULL COMMENT 'user_code 메타데이터 직렬화 데이터',

    device_code_value BLOB DEFAULT NULL COMMENT 'Device Authorization Grant device_code 값, MVP에서는 보통 미사용',
    device_code_issued_at TIMESTAMP NULL DEFAULT NULL COMMENT 'device_code 발급 일시',
    device_code_expires_at TIMESTAMP NULL DEFAULT NULL COMMENT 'device_code 만료 일시',
    device_code_metadata BLOB DEFAULT NULL COMMENT 'device_code 메타데이터 직렬화 데이터',

    PRIMARY KEY (id),

    KEY idx_oauth2_authorization_registered_client_id (registered_client_id),
    KEY idx_oauth2_authorization_principal_name (principal_name),
    KEY idx_oauth2_authorization_state (state),
    KEY idx_oauth2_authorization_authorization_code_expires_at (authorization_code_expires_at),
    KEY idx_oauth2_authorization_access_token_expires_at (access_token_expires_at),
    KEY idx_oauth2_authorization_refresh_token_expires_at (refresh_token_expires_at)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='OAuth2/OIDC authorization code 및 token 상태 저장';
```