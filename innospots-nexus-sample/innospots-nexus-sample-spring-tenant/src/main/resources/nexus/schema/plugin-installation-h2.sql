CREATE TABLE IF NOT EXISTS nx_plugin_installation (
    installation_id varchar(32) NOT NULL PRIMARY KEY,
    plugin_id varchar(256) NOT NULL,
    plugin_version varchar(64) NOT NULL,
    source_type varchar(16) NOT NULL,
    source_location varchar(1024),
    presence varchar(16) NOT NULL,
    installed boolean NOT NULL,
    desired_enabled boolean NOT NULL,
    definition_snapshot clob,
    last_runtime_state varchar(32),
    last_error clob,
    first_discovered_at timestamp NOT NULL,
    last_discovered_at timestamp NOT NULL,
    installed_at timestamp,
    enabled_at timestamp,
    disabled_at timestamp,
    missing_at timestamp,
    created_at timestamp,
    updated_at timestamp,
    created_by varchar(64),
    updated_by varchar(64),
    CONSTRAINT uk_nx_plugin_installation_plugin_id UNIQUE (plugin_id),
    CONSTRAINT ck_nx_plugin_installation_enablement CHECK (installed OR NOT desired_enabled)
);

CREATE INDEX IF NOT EXISTS idx_nx_plugin_installation_presence
    ON nx_plugin_installation (presence);
CREATE INDEX IF NOT EXISTS idx_nx_plugin_installation_enablement
    ON nx_plugin_installation (installed, desired_enabled);
