-- MySQL DDL for innospots-nexus-core entities.
-- IDs are application assigned; inherited audit and ownership columns are expanded per entity.

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
    created_at      DATETIME(6),
    updated_at      DATETIME(6),
    created_by      VARCHAR(64),
    updated_by      VARCHAR(64),
    PRIMARY KEY (resource_id),
    KEY idx_nx_meta_resource_module (module_key),
    KEY idx_nx_meta_resource_uri_key (uri_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS nx_service_registry (
    service_registry_id  VARCHAR(32)   NOT NULL,
    service_name         VARCHAR(128),
    instance_id          VARCHAR(128),
    host                 VARCHAR(256),
    port                 INT,
    service_status       VARCHAR(32),
    service_role         VARCHAR(32),
    group_name           VARCHAR(64),
    tags                 VARCHAR(512),
    metrics              VARCHAR(1024),
    created_at           DATETIME(6),
    updated_at           DATETIME(6),
    created_by           VARCHAR(64),
    updated_by           VARCHAR(64),
    PRIMARY KEY (service_registry_id),
    CONSTRAINT uk_nx_service_registry_instance UNIQUE (instance_id),
    KEY idx_nx_service_registry_name_service_status (service_name, service_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
