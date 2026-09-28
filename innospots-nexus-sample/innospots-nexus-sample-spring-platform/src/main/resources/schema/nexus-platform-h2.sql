-- H2 DDL for nexus-platform entities.
-- Generated from the corresponding MySQL schema; indexes use H2 syntax.

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
    created_at     TIMESTAMP,
    updated_at     TIMESTAMP,
    created_by     VARCHAR(64),
    updated_by     VARCHAR(64),
    PRIMARY KEY (enterprise_id),
    CONSTRAINT uk_nx_enterprise_tenant UNIQUE (tenant_id)
);

CREATE TABLE IF NOT EXISTS nx_tenant (
    tenant_id             VARCHAR(32)  NOT NULL,
    tenant_name           VARCHAR(128) NOT NULL,
    tenant_code           VARCHAR(64)  NOT NULL,
    status                VARCHAR(32)  NOT NULL,
    plan_code             VARCHAR(64),
    owner_tenant_user_id  VARCHAR(32),
    created_at            TIMESTAMP,
    updated_at            TIMESTAMP,
    created_by            VARCHAR(64),
    updated_by            VARCHAR(64),
    PRIMARY KEY (tenant_id),
    CONSTRAINT uk_nx_tenant_code UNIQUE (tenant_code)
);

CREATE TABLE IF NOT EXISTS nx_platform_user (
    platform_user_id  VARCHAR(32)  NOT NULL,
    login_name        VARCHAR(64)  NOT NULL,
    display_name      VARCHAR(128),
    email             VARCHAR(128),
    mobile            VARCHAR(32),
    employee_no       VARCHAR(64),
    status            VARCHAR(32)  NOT NULL,
    last_login_time   TIMESTAMP,
    last_login_ip     VARCHAR(64),
    created_at        TIMESTAMP,
    updated_at        TIMESTAMP,
    created_by        VARCHAR(64),
    updated_by        VARCHAR(64),
    PRIMARY KEY (platform_user_id),
    CONSTRAINT uk_nx_platform_user_login_name UNIQUE (login_name)
);

CREATE INDEX IF NOT EXISTS idx_nx_platform_user_status ON nx_platform_user (status);

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
    token_expires_at      TIMESTAMP,
    created_at            TIMESTAMP,
    updated_at            TIMESTAMP,
    created_by            VARCHAR(64),
    updated_by            VARCHAR(64),
    PRIMARY KEY (identity_id),
    CONSTRAINT uk_nx_platform_user_oauth_provider_subject UNIQUE (provider, provider_subject)
);

CREATE INDEX IF NOT EXISTS idx_nx_platform_user_oauth_user ON nx_platform_user_oauth (platform_user_id);
