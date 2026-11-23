-- H2 DDL for nexus-portal domain entities.
-- Tenant / tenant member / portal user / workspace / project scopes.

CREATE TABLE IF NOT EXISTS nx_tenant (
    tenant_id             VARCHAR(32)  NOT NULL,
    tenant_name           VARCHAR(128) NOT NULL,
    tenant_code           VARCHAR(64)  NOT NULL,
    tenant_type           VARCHAR(32)  NOT NULL,
    status                VARCHAR(32)  NOT NULL,
    plan_code             VARCHAR(64),
    owner_portal_user_id  VARCHAR(32),
    created_at            TIMESTAMP,
    updated_at            TIMESTAMP,
    created_by            VARCHAR(64),
    updated_by            VARCHAR(64),
    PRIMARY KEY (tenant_id),
    CONSTRAINT uk_nx_tenant_code UNIQUE (tenant_code)
);

CREATE TABLE IF NOT EXISTS nx_portal_user (
    portal_user_id     VARCHAR(32)  NOT NULL,
    user_name          VARCHAR(64)  NOT NULL,
    display_name       VARCHAR(128),
    email              VARCHAR(128),
    mobile             VARCHAR(32),
    region             VARCHAR(32),
    time_zone          VARCHAR(64),
    language           VARCHAR(32),
    avatar_key         VARCHAR(256),
    register_source    VARCHAR(32)  NOT NULL,
    status             VARCHAR(32)  NOT NULL,
    email_verified     BOOLEAN      NOT NULL,
    mobile_verified    BOOLEAN      NOT NULL,
    last_login_time    TIMESTAMP,
    last_login_ip      VARCHAR(64),
    created_at         TIMESTAMP,
    updated_at         TIMESTAMP,
    created_by         VARCHAR(64),
    updated_by         VARCHAR(64),
    PRIMARY KEY (portal_user_id),
    CONSTRAINT uk_nx_portal_user_user_name UNIQUE (user_name),
    CONSTRAINT uk_nx_portal_user_email UNIQUE (email),
    CONSTRAINT uk_nx_portal_user_mobile UNIQUE (mobile)
);

CREATE INDEX IF NOT EXISTS idx_nx_portal_user_status ON nx_portal_user (status);

CREATE TABLE IF NOT EXISTS nx_portal_user_oauth (
    identity_id             VARCHAR(32)  NOT NULL,
    portal_user_id          VARCHAR(32)  NOT NULL,
    provider                VARCHAR(64)  NOT NULL,
    provider_subject        VARCHAR(256) NOT NULL,
    provider_account        VARCHAR(128),
    provider_display_name   VARCHAR(128),
    provider_email          VARCHAR(128),
    provider_avatar_url     VARCHAR(512),
    access_token_key        VARCHAR(256),
    refresh_token_key       VARCHAR(256),
    token_expires_at        TIMESTAMP,
    created_at              TIMESTAMP,
    updated_at              TIMESTAMP,
    created_by              VARCHAR(64),
    updated_by              VARCHAR(64),
    PRIMARY KEY (identity_id),
    CONSTRAINT uk_nx_portal_user_oauth_provider_subject UNIQUE (provider, provider_subject)
);

CREATE INDEX IF NOT EXISTS idx_nx_portal_user_oauth_user ON nx_portal_user_oauth (portal_user_id);

CREATE TABLE IF NOT EXISTS nx_tenant_member (
    tenant_member_id  VARCHAR(32) NOT NULL,
    tenant_id         VARCHAR(32) NOT NULL,
    portal_user_id    VARCHAR(32) NOT NULL,
    status            VARCHAR(32) NOT NULL,
    joined_at         TIMESTAMP   NOT NULL,
    member_role       VARCHAR(32),
    created_at        TIMESTAMP,
    updated_at        TIMESTAMP,
    created_by        VARCHAR(64),
    updated_by        VARCHAR(64),
    PRIMARY KEY (tenant_member_id),
    CONSTRAINT uk_nx_tenant_member_portal_user UNIQUE (tenant_id, portal_user_id)
);

CREATE INDEX IF NOT EXISTS idx_nx_tenant_member_portal_user ON nx_tenant_member (portal_user_id, status);

CREATE TABLE IF NOT EXISTS nx_workspace (
    workspace_id    VARCHAR(32)  NOT NULL,
    tenant_id       VARCHAR(32)  NOT NULL,
    workspace_name  VARCHAR(128) NOT NULL,
    workspace_code  VARCHAR(64)  NOT NULL,
    description     VARCHAR(512),
    status          VARCHAR(32)  NOT NULL,
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP,
    created_by      VARCHAR(64),
    updated_by      VARCHAR(64),
    PRIMARY KEY (workspace_id),
    CONSTRAINT uk_nx_workspace_tenant_code UNIQUE (tenant_id, workspace_code)
);

CREATE TABLE IF NOT EXISTS nx_workspace_member (
    workspace_member_id  VARCHAR(32) NOT NULL,
    tenant_id            VARCHAR(32) NOT NULL,
    workspace_id         VARCHAR(32) NOT NULL,
    tenant_member_id     VARCHAR(32) NOT NULL,
    portal_user_id       VARCHAR(32) NOT NULL,
    status               VARCHAR(32) NOT NULL,
    member_role          VARCHAR(32),
    created_at           TIMESTAMP,
    updated_at           TIMESTAMP,
    created_by           VARCHAR(64),
    updated_by           VARCHAR(64),
    PRIMARY KEY (workspace_member_id),
    CONSTRAINT uk_nx_workspace_member UNIQUE (workspace_id, tenant_member_id)
);

CREATE INDEX IF NOT EXISTS idx_nx_workspace_member_tenant ON nx_workspace_member (tenant_id, tenant_member_id);

CREATE TABLE IF NOT EXISTS nx_project (
    project_id    VARCHAR(32)  NOT NULL,
    tenant_id     VARCHAR(32)  NOT NULL,
    workspace_id  VARCHAR(32)  NOT NULL,
    project_name  VARCHAR(128) NOT NULL,
    project_code  VARCHAR(64)  NOT NULL,
    description   VARCHAR(512),
    status        VARCHAR(32)  NOT NULL,
    created_at    TIMESTAMP,
    updated_at    TIMESTAMP,
    created_by    VARCHAR(64),
    updated_by    VARCHAR(64),
    PRIMARY KEY (project_id),
    CONSTRAINT uk_nx_project_workspace_code UNIQUE (workspace_id, project_code)
);
