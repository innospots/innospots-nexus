-- H2 DDL for nexus-core entities.
-- Generated from the corresponding MySQL schema; indexes use H2 syntax.

CREATE TABLE IF NOT EXISTS nx_meta_resource (
    resource_id     VARCHAR(32)   NOT NULL,
    mime_type       VARCHAR(128),
    file_size       BIGINT,
    file_uri        VARCHAR(1024),
    uri_key         VARCHAR(256),
    store_mode      VARCHAR(32),
    resource_name   VARCHAR(256),
    region          VARCHAR(64),
    directory_name  VARCHAR(256),
    module_key      VARCHAR(128),
    module          VARCHAR(64),
    owner_type      VARCHAR(32)   NOT NULL,
    owner_id        VARCHAR(32),
    security_realm  VARCHAR(32)   NOT NULL,
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP,
    created_by      VARCHAR(64),
    updated_by      VARCHAR(64),
    PRIMARY KEY (resource_id)
);

CREATE INDEX IF NOT EXISTS idx_nx_meta_resource_module ON nx_meta_resource (module_key);

CREATE INDEX IF NOT EXISTS idx_nx_meta_resource_uri_key ON nx_meta_resource (uri_key);

CREATE TABLE IF NOT EXISTS nx_service_registry (
    service_registry_id  VARCHAR(32)   NOT NULL,
    service_name         VARCHAR(128),
    instance_id          VARCHAR(128),
    host                 VARCHAR(256),
    port                 INTEGER,
    service_status       VARCHAR(32),
    service_role         VARCHAR(32),
    group_name           VARCHAR(64),
    tags                 VARCHAR(512),
    metrics              VARCHAR(1024),
    created_at           TIMESTAMP,
    updated_at           TIMESTAMP,
    created_by           VARCHAR(64),
    updated_by           VARCHAR(64),
    PRIMARY KEY (service_registry_id),
    CONSTRAINT uk_nx_service_registry_instance UNIQUE (instance_id)
);

CREATE INDEX IF NOT EXISTS idx_nx_service_registry_name_service_status ON nx_service_registry (service_name, service_status);

CREATE TABLE IF NOT EXISTS nx_system_setting (
    setting_id      VARCHAR(32)   NOT NULL,
    setting_domain  VARCHAR(64)   NOT NULL,
    setting_scope   VARCHAR(32)   NOT NULL,
    scope_id        VARCHAR(32)   NOT NULL,
    setting_key     VARCHAR(128)  NOT NULL,
    value_type      VARCHAR(32)   NOT NULL,
    setting_value   VARCHAR(2048) NOT NULL,
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP,
    created_by      VARCHAR(64),
    updated_by      VARCHAR(64),
    PRIMARY KEY (setting_id),
    CONSTRAINT uk_nx_system_setting_scope_key UNIQUE (setting_domain, setting_scope, scope_id, setting_key)
);

CREATE INDEX IF NOT EXISTS idx_nx_system_setting_domain ON nx_system_setting (setting_domain);
