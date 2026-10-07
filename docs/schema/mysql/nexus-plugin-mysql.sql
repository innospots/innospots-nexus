-- MySQL DDL for innospots-nexus-plugin entities.
-- Includes inherited audit columns and the installation state invariant from the module schema.

CREATE TABLE IF NOT EXISTS nx_plugin_installation (
    installation_id      VARCHAR(32)  NOT NULL,
    plugin_id            VARCHAR(256) NOT NULL,
    plugin_version       VARCHAR(64)  NOT NULL,
    source_type          VARCHAR(16)  NOT NULL,
    source_location      VARCHAR(1024),
    presence             VARCHAR(16)  NOT NULL,
    installed            TINYINT(1)   NOT NULL,
    desired_enabled      TINYINT(1)   NOT NULL,
    definition_snapshot  LONGTEXT,
    last_runtime_state   VARCHAR(32),
    last_error           LONGTEXT,
    first_discovered_at  DATETIME(6)  NOT NULL,
    last_discovered_at   DATETIME(6)  NOT NULL,
    installed_at         DATETIME(6),
    enabled_at           DATETIME(6),
    disabled_at          DATETIME(6),
    missing_at           DATETIME(6),
    created_at           DATETIME(6),
    updated_at           DATETIME(6),
    created_by           VARCHAR(64),
    updated_by           VARCHAR(64),
    PRIMARY KEY (installation_id),
    CONSTRAINT uk_nx_plugin_installation_plugin_id UNIQUE (plugin_id),
    CONSTRAINT ck_nx_plugin_installation_enablement CHECK (installed OR NOT desired_enabled),
    KEY idx_nx_plugin_installation_presence (presence),
    KEY idx_nx_plugin_installation_enablement (installed, desired_enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
