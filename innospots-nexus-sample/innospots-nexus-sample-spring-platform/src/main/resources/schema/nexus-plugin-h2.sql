-- H2 DDL for nexus-plugin entities.
-- Generated from the corresponding MySQL schema; indexes use H2 syntax.

CREATE TABLE IF NOT EXISTS nx_plugin_installation (
    installation_id      VARCHAR(32)  NOT NULL,
    plugin_id            VARCHAR(256) NOT NULL,
    plugin_version       VARCHAR(64)  NOT NULL,
    source_type          VARCHAR(16)  NOT NULL,
    source_location      VARCHAR(1024),
    presence             VARCHAR(16)  NOT NULL,
    installed            BOOLEAN   NOT NULL,
    desired_enabled      BOOLEAN   NOT NULL,
    definition_snapshot  CLOB,
    last_runtime_state   VARCHAR(32),
    last_error           CLOB,
    first_discovered_at  TIMESTAMP  NOT NULL,
    last_discovered_at   TIMESTAMP  NOT NULL,
    installed_at         TIMESTAMP,
    enabled_at           TIMESTAMP,
    disabled_at          TIMESTAMP,
    missing_at           TIMESTAMP,
    created_at           TIMESTAMP,
    updated_at           TIMESTAMP,
    created_by           VARCHAR(64),
    updated_by           VARCHAR(64),
    PRIMARY KEY (installation_id),
    CONSTRAINT uk_nx_plugin_installation_plugin_id UNIQUE (plugin_id),
    CONSTRAINT ck_nx_plugin_installation_enablement CHECK (installed OR NOT desired_enabled)
);

CREATE INDEX IF NOT EXISTS idx_nx_plugin_installation_presence ON nx_plugin_installation (presence);

CREATE INDEX IF NOT EXISTS idx_nx_plugin_installation_enablement ON nx_plugin_installation (installed, desired_enabled);
