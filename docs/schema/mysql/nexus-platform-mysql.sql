-- MySQL DDL for innospots-nexus-platform entities.
-- IDs are application assigned; inherited audit columns are expanded per entity.

CREATE TABLE IF NOT EXISTS nx_enterprise (
    enterprise_id  VARCHAR(32)   NOT NULL,
    tenant_id      VARCHAR(32)   NOT NULL,
    legal_name     VARCHAR(256)  NOT NULL,
    credit_code    VARCHAR(64),
    industry       VARCHAR(64),
    contact_name   VARCHAR(128),
    contact_phone  VARCHAR(32),
    contact_email  VARCHAR(128),
    address        VARCHAR(512),
    extra          VARCHAR(1024),
    created_at     DATETIME(6),
    updated_at     DATETIME(6),
    created_by     VARCHAR(64),
    updated_by     VARCHAR(64),
    PRIMARY KEY (enterprise_id),
    CONSTRAINT uk_nx_enterprise_tenant UNIQUE (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS nx_tenant (
    tenant_id             VARCHAR(32)  NOT NULL,
    tenant_name           VARCHAR(128) NOT NULL,
    tenant_code           VARCHAR(64)  NOT NULL,
    status                VARCHAR(32)  NOT NULL,
    plan_code             VARCHAR(64),
    owner_tenant_user_id  VARCHAR(32),
    created_at            DATETIME(6),
    updated_at            DATETIME(6),
    created_by            VARCHAR(64),
    updated_by            VARCHAR(64),
    PRIMARY KEY (tenant_id),
    CONSTRAINT uk_nx_tenant_code UNIQUE (tenant_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS nx_platform_user (
    platform_user_id  VARCHAR(32)  NOT NULL,
    login_name        VARCHAR(64)  NOT NULL,
    display_name      VARCHAR(128),
    email             VARCHAR(128),
    mobile            VARCHAR(32),
    employee_no       VARCHAR(64),
    status            VARCHAR(32)  NOT NULL,
    last_login_time   DATETIME(6),
    last_login_ip     VARCHAR(64),
    created_at        DATETIME(6),
    updated_at        DATETIME(6),
    created_by        VARCHAR(64),
    updated_by        VARCHAR(64),
    PRIMARY KEY (platform_user_id),
    CONSTRAINT uk_nx_platform_user_login_name UNIQUE (login_name),
    KEY idx_nx_platform_user_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS nx_platform_user_oauth (
    identity_id           VARCHAR(32)  NOT NULL,
    platform_user_id      VARCHAR(32)  NOT NULL,
    provider              VARCHAR(64)  NOT NULL,
    provider_subject      VARCHAR(256) NOT NULL,
    provider_account      VARCHAR(128),
    provider_display_name VARCHAR(128),
    provider_email        VARCHAR(128),
    provider_avatar_url   VARCHAR(512),
    access_token_key      VARCHAR(256),
    refresh_token_key     VARCHAR(256),
    token_expires_at      DATETIME(6),
    created_at            DATETIME(6),
    updated_at            DATETIME(6),
    created_by            VARCHAR(64),
    updated_by            VARCHAR(64),
    PRIMARY KEY (identity_id),
    KEY idx_nx_platform_user_oauth_user (platform_user_id),
    CONSTRAINT uk_nx_platform_user_oauth_provider_subject UNIQUE (provider, provider_subject)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
