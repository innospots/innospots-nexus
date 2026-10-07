-- H2 DDL for nexus-console entities.
-- Generated from the corresponding MySQL schema; indexes use H2 syntax.

CREATE TABLE IF NOT EXISTS nx_console_catalog_resource (
    resource_id          VARCHAR(32)  NOT NULL,
    owner_plugin_id      VARCHAR(256) NOT NULL,
    module_key           VARCHAR(128) NOT NULL,
    resource_type        VARCHAR(32)  NOT NULL,
    resource_key         VARCHAR(256) NOT NULL,
    parent_resource_id   VARCHAR(32),
    page_key             VARCHAR(256),
    datasource_key       VARCHAR(128),
    route_path           VARCHAR(512),
    request_method       VARCHAR(16),
    request_url          VARCHAR(512),
    display_name         VARCHAR(256),
    sort_order           INTEGER          NOT NULL,
    status               VARCHAR(32)  NOT NULL,
    security_realm       VARCHAR(32)  NOT NULL,
    created_at           TIMESTAMP,
    updated_at           TIMESTAMP,
    created_by           VARCHAR(64),
    updated_by           VARCHAR(64),
    PRIMARY KEY (resource_id),
    CONSTRAINT uk_nx_console_catalog_resource_key UNIQUE (resource_key)
);

CREATE INDEX IF NOT EXISTS idx_nx_console_catalog_resource_source ON nx_console_catalog_resource (owner_plugin_id, module_key, resource_type, status);

CREATE INDEX IF NOT EXISTS idx_nx_console_catalog_resource_parent ON nx_console_catalog_resource (parent_resource_id, sort_order);

CREATE INDEX IF NOT EXISTS idx_nx_console_catalog_resource_request ON nx_console_catalog_resource (page_key, request_method, request_url);

CREATE INDEX IF NOT EXISTS idx_nx_console_catalog_resource_realm ON nx_console_catalog_resource (security_realm);

CREATE TABLE IF NOT EXISTS nx_otp_challenge (
    challenge_id    VARCHAR(32)  NOT NULL,
    purpose         VARCHAR(32)  NOT NULL,
    channel         VARCHAR(32)  NOT NULL,
    destination     VARCHAR(256) NOT NULL,
    algorithm       VARCHAR(64)  NOT NULL,
    code_verifier   VARCHAR(512) NOT NULL,
    expires_at      TIMESTAMP  NOT NULL,
    consumed_at     TIMESTAMP,
    attempt_count   INTEGER          NOT NULL,
    max_attempts    INTEGER          NOT NULL,
    resend_count    INTEGER          NOT NULL,
    owner_type      VARCHAR(32)  NOT NULL,
    owner_id        VARCHAR(32),
    security_realm  VARCHAR(32)  NOT NULL,
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP,
    created_by      VARCHAR(64),
    updated_by      VARCHAR(64),
    PRIMARY KEY (challenge_id)
);

CREATE INDEX IF NOT EXISTS idx_nx_otp_challenge_lookup ON nx_otp_challenge (owner_type, owner_id, security_realm, purpose, channel, destination);

CREATE INDEX IF NOT EXISTS idx_nx_otp_challenge_expires ON nx_otp_challenge (expires_at);

CREATE TABLE IF NOT EXISTS nx_user_credential (
    credential_id       VARCHAR(32)   NOT NULL,
    subject_id          VARCHAR(32)   NOT NULL,
    credential_kind     VARCHAR(32)   NOT NULL,
    algorithm           VARCHAR(64)   NOT NULL,
    verifier            VARCHAR(512)  NOT NULL,
    verifier_params     VARCHAR(1024),
    credential_version  INTEGER           NOT NULL,
    force_reset         BOOLEAN    NOT NULL,
    failed_attempts     INTEGER           NOT NULL,
    locked_until        TIMESTAMP,
    expired_at          TIMESTAMP,
    owner_type          VARCHAR(32)   NOT NULL,
    owner_id            VARCHAR(32),
    security_realm      VARCHAR(32)   NOT NULL,
    created_at          TIMESTAMP,
    updated_at          TIMESTAMP,
    created_by          VARCHAR(64),
    updated_by          VARCHAR(64),
    PRIMARY KEY (credential_id),
    CONSTRAINT uk_nx_user_credential_subject UNIQUE (owner_type, owner_id, security_realm, credential_kind, subject_id)
);

CREATE INDEX IF NOT EXISTS idx_nx_user_credential_subject ON nx_user_credential (subject_id, credential_kind);

CREATE TABLE IF NOT EXISTS nx_dictionary_item (
    dictionary_item_id  VARCHAR(32)  NOT NULL,
    type_code           VARCHAR(64)  NOT NULL,
    item_value          VARCHAR(64)  NOT NULL,
    item_name           VARCHAR(128) NOT NULL,
    status              VARCHAR(32)  NOT NULL,
    sort_order          INTEGER          NOT NULL,
    built_in            BOOLEAN   NOT NULL,
    owner_type          VARCHAR(32)  NOT NULL,
    owner_id            VARCHAR(32),
    security_realm      VARCHAR(32)  NOT NULL,
    created_at          TIMESTAMP,
    updated_at          TIMESTAMP,
    created_by          VARCHAR(64),
    updated_by          VARCHAR(64),
    PRIMARY KEY (dictionary_item_id),
    CONSTRAINT uk_nx_dictionary_item_value UNIQUE (owner_type, owner_id, security_realm, type_code, item_value)
);

CREATE INDEX IF NOT EXISTS idx_nx_dictionary_item_type ON nx_dictionary_item (owner_type, owner_id, type_code, sort_order);

CREATE INDEX IF NOT EXISTS idx_nx_dictionary_item_realm ON nx_dictionary_item (security_realm);

CREATE TABLE IF NOT EXISTS nx_dictionary_type (
    dictionary_type_id  VARCHAR(32)  NOT NULL,
    type_code           VARCHAR(64)  NOT NULL,
    type_name           VARCHAR(128) NOT NULL,
    status              VARCHAR(32)  NOT NULL,
    sort_order          INTEGER          NOT NULL,
    built_in            BOOLEAN   NOT NULL,
    owner_type          VARCHAR(32)  NOT NULL,
    owner_id            VARCHAR(32),
    security_realm      VARCHAR(32)  NOT NULL,
    created_at          TIMESTAMP,
    updated_at          TIMESTAMP,
    created_by          VARCHAR(64),
    updated_by          VARCHAR(64),
    PRIMARY KEY (dictionary_type_id),
    CONSTRAINT uk_nx_dictionary_type_code UNIQUE (owner_type, owner_id, security_realm, type_code)
);

CREATE INDEX IF NOT EXISTS idx_nx_dictionary_type_status ON nx_dictionary_type (owner_type, owner_id, status);

CREATE INDEX IF NOT EXISTS idx_nx_dictionary_type_realm ON nx_dictionary_type (security_realm);

CREATE TABLE IF NOT EXISTS nx_audit_log (
    audit_log_id     VARCHAR(32)   NOT NULL,
    action           VARCHAR(64)   NOT NULL,
    path             VARCHAR(256)  NOT NULL,
    operated_time    TIMESTAMP   NOT NULL,
    actor            VARCHAR(64),
    message          VARCHAR(512),
    status_code      VARCHAR(32),
    execution_result VARCHAR(32)   NOT NULL,
    key_parameters   VARCHAR(2048),
    owner_type       VARCHAR(32)   NOT NULL,
    owner_id         VARCHAR(32),
    security_realm   VARCHAR(32)   NOT NULL,
    created_at       TIMESTAMP,
    updated_at       TIMESTAMP,
    created_by       VARCHAR(64),
    updated_by       VARCHAR(64),
    PRIMARY KEY (audit_log_id)
);

CREATE INDEX IF NOT EXISTS idx_nx_audit_log_time ON nx_audit_log (operated_time);

CREATE INDEX IF NOT EXISTS idx_nx_audit_log_action ON nx_audit_log (action);

CREATE INDEX IF NOT EXISTS idx_nx_audit_log_actor ON nx_audit_log (actor);

CREATE INDEX IF NOT EXISTS idx_nx_audit_log_result ON nx_audit_log (execution_result);

CREATE INDEX IF NOT EXISTS idx_nx_audit_log_owner_time ON nx_audit_log (owner_type, owner_id, operated_time);

CREATE INDEX IF NOT EXISTS idx_nx_audit_log_realm_time ON nx_audit_log (security_realm, operated_time);

CREATE TABLE IF NOT EXISTS nx_menu (
    menu_id         VARCHAR(32)  NOT NULL,
    parent_id       VARCHAR(32),
    menu_key        VARCHAR(64)  NOT NULL,
    menu_name       VARCHAR(128) NOT NULL,
    menu_type       VARCHAR(32)  NOT NULL,
    route_path      VARCHAR(256),
    component_key   VARCHAR(128),
    redirect_path   VARCHAR(256),
    external_url    VARCHAR(512),
    icon            VARCHAR(128),
    open_mode       VARCHAR(32)  NOT NULL,
    visible         BOOLEAN   NOT NULL,
    status          VARCHAR(32)  NOT NULL,
    sort_order      INTEGER          NOT NULL,
    built_in        BOOLEAN   NOT NULL,
    owner_type      VARCHAR(32)  NOT NULL,
    owner_id        VARCHAR(32),
    security_realm  VARCHAR(32)  NOT NULL,
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP,
    created_by      VARCHAR(64),
    updated_by      VARCHAR(64),
    PRIMARY KEY (menu_id),
    CONSTRAINT uk_nx_menu_owner_key UNIQUE (owner_type, owner_id, security_realm, menu_key)
);

CREATE INDEX IF NOT EXISTS idx_nx_menu_owner_parent_order ON nx_menu (owner_type, owner_id, parent_id, sort_order);

CREATE INDEX IF NOT EXISTS idx_nx_menu_owner_status_visible ON nx_menu (owner_type, owner_id, status, visible);

CREATE INDEX IF NOT EXISTS idx_nx_menu_realm ON nx_menu (security_realm);

CREATE TABLE IF NOT EXISTS nx_permission_grant (
    grant_id               VARCHAR(32) NOT NULL,
    subject_type           VARCHAR(32) NOT NULL,
    subject_id             VARCHAR(32) NOT NULL,
    resource_id            VARCHAR(32) NOT NULL,
    constraint_definition  TEXT,
    owner_type             VARCHAR(32) NOT NULL,
    owner_id               VARCHAR(32),
    security_realm         VARCHAR(32) NOT NULL,
    created_at             TIMESTAMP,
    updated_at             TIMESTAMP,
    created_by             VARCHAR(64),
    updated_by             VARCHAR(64),
    PRIMARY KEY (grant_id),
    CONSTRAINT uk_nx_permission_grant_subject_resource UNIQUE (owner_type, owner_id, security_realm, subject_type, subject_id, resource_id)
);

CREATE INDEX IF NOT EXISTS idx_nx_permission_grant_subject ON nx_permission_grant (owner_type, owner_id, subject_type, subject_id);

CREATE INDEX IF NOT EXISTS idx_nx_permission_grant_resource ON nx_permission_grant (owner_type, owner_id, resource_id);

CREATE INDEX IF NOT EXISTS idx_nx_permission_grant_realm ON nx_permission_grant (security_realm);

CREATE TABLE IF NOT EXISTS nx_role_binding (
    binding_id    VARCHAR(32) NOT NULL,
    role_id       VARCHAR(32) NOT NULL,
    subject_type  VARCHAR(32) NOT NULL,
    subject_id    VARCHAR(32) NOT NULL,
    owner_type    VARCHAR(32) NOT NULL,
    owner_id      VARCHAR(32),
    security_realm VARCHAR(32) NOT NULL,
    created_at    TIMESTAMP,
    updated_at    TIMESTAMP,
    created_by    VARCHAR(64),
    updated_by    VARCHAR(64),
    PRIMARY KEY (binding_id),
    CONSTRAINT uk_nx_role_binding_subject UNIQUE (owner_type, owner_id, security_realm, role_id, subject_type, subject_id)
);

CREATE INDEX IF NOT EXISTS idx_nx_role_binding_subject ON nx_role_binding (subject_type, subject_id);

CREATE INDEX IF NOT EXISTS idx_nx_role_binding_role ON nx_role_binding (role_id);

CREATE TABLE IF NOT EXISTS nx_role (
    role_id         VARCHAR(32)  NOT NULL,
    role_name       VARCHAR(64)  NOT NULL,
    role_code       VARCHAR(64)  NOT NULL,
    owner_type      VARCHAR(32)  NOT NULL,
    owner_id        VARCHAR(32),
    security_realm  VARCHAR(32)  NOT NULL,
    description     VARCHAR(256),
    status          VARCHAR(32)  NOT NULL,
    sort_order      INTEGER          NOT NULL,
    built_in        BOOLEAN   NOT NULL,
    administrator   BOOLEAN   NOT NULL,
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP,
    created_by      VARCHAR(64),
    updated_by      VARCHAR(64),
    PRIMARY KEY (role_id),
    CONSTRAINT uk_nx_role_owner_code UNIQUE (owner_type, owner_id, security_realm, role_code)
);

CREATE INDEX IF NOT EXISTS idx_nx_role_owner_status ON nx_role (owner_type, owner_id, status);

CREATE INDEX IF NOT EXISTS idx_nx_role_name ON nx_role (role_name);

CREATE INDEX IF NOT EXISTS idx_nx_role_realm ON nx_role (security_realm);
