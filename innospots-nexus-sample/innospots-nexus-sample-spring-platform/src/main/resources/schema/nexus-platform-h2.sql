-- H2 DDL for nexus-platform entities.
-- Aligned with docs/schema/mysql/nexus-platform-mysql.sql; indexes use H2 syntax.

CREATE TABLE IF NOT EXISTS nx_pl_enterprise (
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
    CONSTRAINT uk_nx_pl_enterprise_tenant UNIQUE (tenant_id)
);

CREATE TABLE IF NOT EXISTS nx_pl_tenant (
    tenant_id             VARCHAR(32)  NOT NULL,
    tenant_name           VARCHAR(128) NOT NULL,
    tenant_code           VARCHAR(64)  NOT NULL,
    tenant_type           VARCHAR(32)  NOT NULL,
    status                VARCHAR(32)  NOT NULL,
    plan_code             VARCHAR(64),
    owner_tenant_user_id  VARCHAR(32),
    created_at            TIMESTAMP,
    updated_at            TIMESTAMP,
    created_by            VARCHAR(64),
    updated_by            VARCHAR(64),
    PRIMARY KEY (tenant_id),
    CONSTRAINT uk_nx_pl_tenant_code UNIQUE (tenant_code)
);

CREATE TABLE IF NOT EXISTS nx_pl_user (
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
    CONSTRAINT uk_nx_pl_user_login_name UNIQUE (login_name)
);

CREATE INDEX IF NOT EXISTS idx_nx_pl_user_status ON nx_pl_user (status);

CREATE TABLE IF NOT EXISTS nx_pl_user_oauth (
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
    CONSTRAINT uk_nx_pl_user_oauth_provider_subject UNIQUE (provider, provider_subject)
);

CREATE INDEX IF NOT EXISTS idx_nx_pl_user_oauth_user ON nx_pl_user_oauth (platform_user_id);

CREATE TABLE IF NOT EXISTS nx_pl_invite (
    invite_id            VARCHAR(32)   NOT NULL,
    invite_token         VARCHAR(64)   NOT NULL,
    invite_code          VARCHAR(32)   NOT NULL,
    email                VARCHAR(128),
    mobile               VARCHAR(32),
    login_name           VARCHAR(64),
    default_role_codes   VARCHAR(512),
    status               VARCHAR(32)   NOT NULL,
    delivery_mode        VARCHAR(32)   NOT NULL,
    expires_at           TIMESTAMP     NOT NULL,
    accepted_at          TIMESTAMP,
    platform_user_id     VARCHAR(32),
    revoked_at           TIMESTAMP,
    code_failed_attempts INTEGER       NOT NULL DEFAULT 0,
    created_at           TIMESTAMP,
    updated_at           TIMESTAMP,
    created_by           VARCHAR(64),
    updated_by           VARCHAR(64),
    PRIMARY KEY (invite_id),
    CONSTRAINT uk_nx_pl_invite_token UNIQUE (invite_token),
    CONSTRAINT uk_nx_pl_invite_code UNIQUE (invite_code)
);

CREATE INDEX IF NOT EXISTS idx_nx_pl_invite_status ON nx_pl_invite (status);

CREATE INDEX IF NOT EXISTS idx_nx_pl_invite_expires ON nx_pl_invite (expires_at);

CREATE TABLE IF NOT EXISTS nx_pl_access_request (
    access_request_id   VARCHAR(32)   NOT NULL,
    applicant_name      VARCHAR(128)  NOT NULL,
    login_name          VARCHAR(64),
    platform_user_id    VARCHAR(32),
    email               VARCHAR(128),
    mobile              VARCHAR(32),
    description         VARCHAR(512),
    status              VARCHAR(32)   NOT NULL,
    reject_reason       VARCHAR(512),
    approved_invite_id  VARCHAR(32),
    reviewed_at         TIMESTAMP,
    reviewed_by         VARCHAR(64),
    created_at          TIMESTAMP,
    updated_at          TIMESTAMP,
    created_by          VARCHAR(64),
    updated_by          VARCHAR(64),
    PRIMARY KEY (access_request_id)
);

CREATE INDEX IF NOT EXISTS idx_nx_pl_access_request_status ON nx_pl_access_request (status);

CREATE INDEX IF NOT EXISTS idx_nx_pl_access_request_user ON nx_pl_access_request (platform_user_id);
